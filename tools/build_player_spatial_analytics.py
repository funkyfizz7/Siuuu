"""
PitchIQ — Commentary-Derived Wagon Wheel and Bowling Heatmap Extraction Pipeline
Processes raw ball-by-ball commentary CSVs into compact, validated player spatial analytics JSON.
"""

import os
import glob
import csv
import json
import re
import argparse
from typing import Dict, Any, Tuple, Optional, List

# Standard Sector definitions and central angles (0° = straight down ground)
SECTOR_ANGLES = {
    "FINE_LEG": 27.5,
    "SQUARE_LEG": 67.5,
    "MID_WICKET": 112.5,
    "MID_ON": 157.5,
    "STRAIGHT": 180.0,
    "MID_OFF": 202.5,
    "COVER": 247.5,
    "POINT": 292.5,
    "THIRD_MAN": 332.5,
}

# Regex for Wagon Wheel Shot Directions
SECTOR_REGEXES = [
    ("THIRD_MAN", re.compile(r"\b(third[- ]man|deep third man|fine third man|fine third|third man area)\b", re.IGNORECASE)),
    ("POINT", re.compile(r"\b(backward point|cover[- ]point|cover point|backward of point|point region|deep point|point boundary|point|gully|first slip|second slip|third slip|through the slips|over the slips|slip cordon|cordon)\b", re.IGNORECASE)),
    ("COVER", re.compile(r"\b(extra[- ]cover|extra cover|deep cover|cover region|sweeper cover|deep extra cover|through covers|through cover|past cover|covers|cover)\b", re.IGNORECASE)),
    ("MID_OFF", re.compile(r"\b(long[- ]off|long off|mid[- ]off|mid off|deep mid off|past mid-off|past mid off)\b", re.IGNORECASE)),
    ("STRAIGHT", re.compile(r"\b(straight down the ground|down the ground|past the bowler|bowler's head|straight drive|back past the bowler|straight to long-off and long-on)\b", re.IGNORECASE)),
    ("MID_ON", re.compile(r"\b(long[- ]on|long on|mid[- ]on|mid on|deep mid on|past mid-on|past mid on)\b", re.IGNORECASE)),
    ("MID_WICKET", re.compile(r"\b(deep mid[- ]?wicket|wide midwicket|mid[- ]?wicket|cow corner|through midwicket|through mid-wicket)\b", re.IGNORECASE)),
    ("SQUARE_LEG", re.compile(r"\b(backward square leg|backward square|deep square leg|deep square|square leg|forward square leg|wide of square)\b", re.IGNORECASE)),
    ("FINE_LEG", re.compile(r"\b(deep fine leg|short fine leg|fine leg|leg slip|leg gully|glanced fine|clipped fine)\b", re.IGNORECASE)),
]

# Non-direction false positive filter
RE_FALSE_POSITIVES = re.compile(
    r"\b(at that point|point in time|covers the line|covers his stumps|slip of the tongue|from the point of|good point|at this point|turning point)\b",
    re.IGNORECASE
)

# Bowling Length classification
RE_FULL_TOSS = re.compile(r"\bfull[- ]toss\b", re.IGNORECASE)
RE_FULL = re.compile(r"\b(yorker|in the blockhole|blockhole|half[- ]volley|half volley|overpitched|fuller|fullish|full)\b", re.IGNORECASE)
RE_GOOD = re.compile(r"\b(good length|on a good length)\b", re.IGNORECASE)
RE_SHORT = re.compile(r"\b(back of a length|short of a length|short of length|shortish|bouncer|sharp bouncer|bumper|half tracker|short)\b", re.IGNORECASE)

# Bowling Line classification
RE_LINE_OFF = re.compile(r"\b(wide outside off|outside off|wide of off|outside the off|fourth stump|fifth stump|in the channel|channel outside off|wide outside the off|channel)\b", re.IGNORECASE)
RE_LINE_STUMPS = re.compile(r"\b(around off|off stump|middle and off|middle stump|at the stumps|stump to stump|on the stumps|straight)\b", re.IGNORECASE)
RE_LINE_LEG = re.compile(r"\b(middle and leg|leg stump|down leg|down the leg|on the pads|at the pads|straying on the pads|sliding down leg|on the thigh pad|on the hips|clipped off the pads)\b", re.IGNORECASE)

def classify_shot_direction(commentary: str) -> Tuple[str, Optional[float], str]:
    """
    Classifies shot direction from commentary into a standard sector and polar angle.
    Returns: (sector_id, angle_deg, provenance)
    """
    if not commentary:
        return "UNKNOWN", None, "UNKNOWN"
        
    cleaned = RE_FALSE_POSITIVES.sub("", commentary)
    
    for sector_name, regex in SECTOR_REGEXES:
        if regex.search(cleaned):
            return sector_name, SECTOR_ANGLES[sector_name], "COMMENTARY_EXPLICIT"
            
    return "UNKNOWN", None, "UNKNOWN"

def classify_bowling_length(commentary: str) -> Tuple[str, str]:
    """
    Classifies bowling length into FULL_TOSS, FULL, GOOD, SHORT, or UNKNOWN.
    Returns: (length_category, provenance)
    """
    if not commentary:
        return "UNKNOWN", "UNKNOWN"
        
    if RE_FULL_TOSS.search(commentary):
        return "FULL_TOSS", "COMMENTARY_EXPLICIT"
    if RE_FULL.search(commentary):
        return "FULL", "COMMENTARY_EXPLICIT"
    if RE_GOOD.search(commentary):
        return "GOOD", "COMMENTARY_EXPLICIT"
    if RE_SHORT.search(commentary):
        return "SHORT", "COMMENTARY_EXPLICIT"
        
    return "UNKNOWN", "UNKNOWN"

def classify_bowling_line(commentary: str) -> Tuple[str, str]:
    """
    Classifies bowling lateral line into OUTSIDE_OFF, OFF_MIDDLE_STUMPS, LEG_SIDE, or UNKNOWN.
    Returns: (line_category, provenance)
    """
    if not commentary:
        return "UNKNOWN", "UNKNOWN"
        
    if RE_LINE_OFF.search(commentary):
        return "OUTSIDE_OFF", "COMMENTARY_EXPLICIT"
    if RE_LINE_STUMPS.search(commentary):
        return "OFF_MIDDLE_STUMPS", "COMMENTARY_EXPLICIT"
    if RE_LINE_LEG.search(commentary):
        return "LEG_SIDE", "COMMENTARY_EXPLICIT"
        
    return "UNKNOWN", "UNKNOWN"

def parse_innings_label(innings_str: str) -> int:
    """Parses '1st innings' -> 1, '2nd innings' -> 2, etc."""
    m = re.search(r"(\d+)", innings_str or "")
    if m:
        return int(m.group(1))
    return 1

def compute_batter_runs(total_runs: int, wides: int, noballs: int, byes: int, legbyes: int) -> int:
    """Computes runs off the bat for a delivery."""
    if wides > 0 or byes > 0 or legbyes > 0:
        return 0
    if noballs > 0:
        return max(0, total_runs - noballs)
    return max(0, total_runs)

class PlayerResolver:
    """
    Resolves commentary player names and ESPN IDs to PitchIQ warehouse player IDs.
    Uses dim_players.json, fact_player_match.csv, and match-specific JSONs.
    """
    def __init__(self, warehouse_dir: str):
        self.warehouse_dir = warehouse_dir
        self.fpm_by_match: Dict[str, Dict[int, str]] = {}
        self.dim_players_by_id: Dict[int, Dict[str, Any]] = {}
        self.espn_to_pid: Dict[str, int] = {}
        self.dim_name_to_pid: Dict[str, int] = {}

        # 1. Load dim_players.json
        dim_path = os.path.join(warehouse_dir, "dim_players.json")
        if os.path.exists(dim_path):
            try:
                with open(dim_path, "r", encoding="utf-8") as fp:
                    players = json.load(fp)
                    for p in players:
                        pid = p.get("player_id")
                        if pid is None:
                            continue
                        self.dim_players_by_id[pid] = p
                        espn_id = p.get("espn_player_id")
                        if espn_id:
                            self.espn_to_pid[str(espn_id)] = pid
                        pname = (p.get("player_name") or "").strip().lower()
                        sname = (p.get("short_name") or "").strip().lower()
                        if pname:
                            self.dim_name_to_pid[pname] = pid
                        if sname:
                            self.dim_name_to_pid[sname] = pid
            except Exception as e:
                print(f"Warning loading dim_players.json: {e}")

        # 2. Load fact_player_match.csv
        fpm_path = os.path.join(warehouse_dir, "fact_player_match.csv")
        if os.path.exists(fpm_path):
            try:
                with open(fpm_path, "r", encoding="utf-8") as fp:
                    reader = csv.DictReader(fp)
                    for row in reader:
                        mid = row.get("match_id")
                        if not mid:
                            continue
                        if mid not in self.fpm_by_match:
                            self.fpm_by_match[mid] = {}
                        pid_val = row.get("player_id")
                        pname = row.get("player_name", "")
                        if pid_val and pid_val.isdigit():
                            self.fpm_by_match[mid][int(pid_val)] = pname
            except Exception as e:
                print(f"Warning loading fact_player_match.csv: {e}")

    def get_match_resolver(self, match_id: int):
        match_players: Dict[int, set] = {}

        # 1. From fact_player_match
        for pid, name in self.fpm_by_match.get(str(match_id), {}).items():
            if pid not in match_players:
                match_players[pid] = set()
            match_players[pid].add(name.strip().lower())

        # 2. From match_{match_id}.json
        m_path = os.path.join(self.warehouse_dir, "matches", f"match_{match_id}.json")
        if os.path.exists(m_path):
            try:
                with open(m_path, "r", encoding="utf-8") as fp:
                    m_data = json.load(fp)
                r_tab = m_data.get("ratingsTab", {})
                for p in r_tab.get("team1Players", []) + r_tab.get("team2Players", []):
                    pid = p.get("playerId")
                    pname = p.get("playerName", "")
                    if pid and pname:
                        pid = int(pid)
                        if pid not in match_players: match_players[pid] = set()
                        match_players[pid].add(pname.strip().lower())

                sc_tab = m_data.get("scorecardTab", {})
                for sc in sc_tab.get("scorecards", []):
                    for b in sc.get("batters", []):
                        pid = b.get("playerId")
                        pname = b.get("name", "")
                        if pid and pname:
                            pid = int(pid)
                            if pid not in match_players: match_players[pid] = set()
                            match_players[pid].add(pname.strip().lower())
                    for bw in sc.get("bowlers", []):
                        pid = bw.get("playerId")
                        pname = bw.get("name", "")
                        if pid and pname:
                            pid = int(pid)
                            if pid not in match_players: match_players[pid] = set()
                            match_players[pid].add(pname.strip().lower())
            except Exception:
                pass

        def resolve(comm_name: str, espn_id_str: str) -> Tuple[int, str]:
            # Direct ESPN ID mapping if available
            if espn_id_str and espn_id_str in self.espn_to_pid:
                pid = self.espn_to_pid[espn_id_str]
                p_dim = self.dim_players_by_id.get(pid, {})
                return pid, p_dim.get("player_name") or comm_name

            c_clean = comm_name.strip().lower()
            if not c_clean:
                fallback_id = int(espn_id_str) if espn_id_str.isdigit() else 0
                return fallback_id, comm_name

            c_surname = c_clean.split()[-1]
            c_initial = c_clean[0]

            # 1. Exact match in match_players
            for pid, aliases in match_players.items():
                if c_clean in aliases:
                    p_name = self.fpm_by_match.get(str(match_id), {}).get(pid) or comm_name
                    return pid, p_name

            # 2. Substring / contains match in match_players
            for pid, aliases in match_players.items():
                for a in aliases:
                    if (c_clean in a or a in c_clean) and len(c_clean) > 3:
                        p_name = self.fpm_by_match.get(str(match_id), {}).get(pid) or comm_name
                        return pid, p_name

            # 3. Surname match in match_players
            candidates = []
            for pid, aliases in match_players.items():
                for a in aliases:
                    a_surname = a.split()[-1]
                    if a_surname == c_surname:
                        candidates.append((pid, a))
                        break

            if len(candidates) == 1:
                pid = candidates[0][0]
                p_name = self.fpm_by_match.get(str(match_id), {}).get(pid) or comm_name
                return pid, p_name
            elif len(candidates) > 1:
                for pid, a in candidates:
                    if a[0] == c_initial:
                        p_name = self.fpm_by_match.get(str(match_id), {}).get(pid) or comm_name
                        return pid, p_name
                pid = candidates[0][0]
                p_name = self.fpm_by_match.get(str(match_id), {}).get(pid) or comm_name
                return pid, p_name

            # 4. Known aliases
            if "kamrul" in c_clean:
                for pid, aliases in match_players.items():
                    for a in aliases:
                        if "rabbi" in a or "kamrul" in a:
                            return pid, comm_name
            if "mehidy" in c_clean or "mehedi" in c_clean:
                for pid, aliases in match_players.items():
                    for a in aliases:
                        if "miraz" in a or "mehidy" in a or "mehedi" in a:
                            return pid, comm_name

            # 5. Global dim_players match
            if c_clean in self.dim_name_to_pid:
                pid = self.dim_name_to_pid[c_clean]
                return pid, comm_name

            fallback_id = int(espn_id_str) if espn_id_str.isdigit() else 0
            return fallback_id, comm_name

        return resolve

def process_match_commentary(
    file_path: str,
    match_id: int,
    resolver_func: Optional[Any] = None
) -> Dict[str, Any]:
    """
    Reads a single match commentary CSV and produces compact structured spatial analytics.
    """
    batting_analytics: Dict[str, Any] = {}
    bowling_analytics: Dict[str, Any] = {}
    
    seen_delivery_keys: Dict[Tuple[int, str, str], int] = {}
    unique_deliveries_count = 0
    duplicate_deliveries_count = 0
    
    with open(file_path, "r", encoding="utf-8", errors="replace") as fp:
        reader = csv.DictReader(fp)
        
        for row in reader:
            inn_str = row.get("Innings", "")
            inn_num = parse_innings_label(inn_str)
            over_num_str = row.get("Over_number", "")
            over_ball_str = row.get("Over_ball", "")
            
            if not over_num_str or not over_ball_str:
                continue
                
            raw_key = (inn_num, over_num_str, over_ball_str)
            if raw_key in seen_delivery_keys:
                seen_delivery_keys[raw_key] += 1
                dedup_idx = seen_delivery_keys[raw_key]
                delivery_key = f"{inn_num}_{over_num_str}_{over_ball_str}_{dedup_idx}"
                duplicate_deliveries_count += 1
            else:
                seen_delivery_keys[raw_key] = 1
                delivery_key = f"{inn_num}_{over_num_str}_{over_ball_str}"
                unique_deliveries_count += 1
                
            comm = row.get("Commentary", "")
            tot_runs = int(row.get("Total_Runs_on_delivery") or 0)
            wides = int(row.get("Over_wide") or 0)
            noballs = int(row.get("Over_no_Ball") or 0)
            byes = int(row.get("Over_byes") or 0)
            legbyes = int(row.get("Over_leg_byes") or 0)
            batter_runs = compute_batter_runs(tot_runs, wides, noballs, byes, legbyes)
            
            # 1. Batting Analytics
            batter_id_str = row.get("Batsman_id", "").strip()
            batter_name = row.get("Batsman_name", "").strip()
            
            if batter_id_str and batter_id_str.lower() not in ("false", "none", "null") and batter_name.lower() not in ("false", "none", "null"):
                if resolver_func:
                    b_id, b_name = resolver_func(batter_name, batter_id_str)
                else:
                    b_id = int(batter_id_str) if batter_id_str.isdigit() else 0
                    b_name = batter_name

                b_key = str(b_id)
                if b_key not in batting_analytics:
                    batting_analytics[b_key] = {
                        "batterId": b_id,
                        "espnId": int(batter_id_str) if batter_id_str.isdigit() else None,
                        "batterName": b_name,
                        "innings": {}
                    }
                b_record = batting_analytics[b_key]
                inn_key = str(inn_num)
                if inn_key not in b_record["innings"]:
                    b_record["innings"][inn_key] = {
                        "totalBalls": 0,
                        "totalRuns": 0,
                        "scoringBalls": 0,
                        "directlyClassifiedBalls": 0,
                        "unknownDirectionBalls": 0,
                        "directlyClassifiedBoundaries": 0,
                        "totalBoundaries": 0,
                        "sectors": {
                            s: {
                                "shotsCount": 0,
                                "runsTotal": 0,
                                "dots": 0,
                                "singles": 0,
                                "doubles": 0,
                                "triples": 0,
                                "fours": 0,
                                "sixes": 0
                            } for s in SECTOR_ANGLES.keys()
                        },
                        "deliveries": []
                    }
                b_inn = b_record["innings"][inn_key]
                b_inn["totalBalls"] += 1
                b_inn["totalRuns"] += batter_runs
                
                sector, angle, prov = classify_shot_direction(comm)
                
                is_scoring = batter_runs > 0
                is_boundary = batter_runs >= 4
                
                if is_scoring:
                    b_inn["scoringBalls"] += 1
                if is_boundary:
                    b_inn["totalBoundaries"] += 1
                    
                if sector != "UNKNOWN":
                    b_inn["directlyClassifiedBalls"] += 1
                    if is_boundary:
                        b_inn["directlyClassifiedBoundaries"] += 1
                    sec_stat = b_inn["sectors"][sector]
                    sec_stat["shotsCount"] += 1
                    sec_stat["runsTotal"] += batter_runs
                    if batter_runs == 0:
                        sec_stat["dots"] += 1
                    elif batter_runs == 1:
                        sec_stat["singles"] += 1
                    elif batter_runs == 2:
                        sec_stat["doubles"] += 1
                    elif batter_runs == 3:
                        sec_stat["triples"] += 1
                    elif batter_runs == 4 or batter_runs == 5:
                        sec_stat["fours"] += 1
                    elif batter_runs >= 6:
                        sec_stat["sixes"] += 1
                else:
                    b_inn["unknownDirectionBalls"] += 1
                    
                jitter = ((hash(delivery_key) % 13) - 6) * 1.0 if angle is not None else 0.0
                effective_angle = (angle + jitter) % 360.0 if angle is not None else None
                
                b_inn["deliveries"].append({
                    "key": delivery_key,
                    "over": int(over_num_str) if over_num_str.isdigit() else 0,
                    "ball": int(over_ball_str) if over_ball_str.isdigit() else 0,
                    "runs": batter_runs,
                    "totRuns": tot_runs,
                    "sector": sector,
                    "angle": round(effective_angle, 1) if effective_angle is not None else None,
                    "prov": prov
                })
                
            # 2. Bowling Analytics
            bowler_id_str = row.get("Bowler_id", "").strip()
            bowler_name = row.get("Bowler_name", "").strip()
            
            if bowler_id_str and bowler_id_str.lower() not in ("false", "none", "null") and bowler_name.lower() not in ("false", "none", "null"):
                if resolver_func:
                    bowl_id, bowl_name = resolver_func(bowler_name, bowler_id_str)
                else:
                    bowl_id = int(bowler_id_str) if bowler_id_str.isdigit() else 0
                    bowl_name = bowler_name

                bowl_key = str(bowl_id)
                if bowl_key not in bowling_analytics:
                    bowling_analytics[bowl_key] = {
                        "bowlerId": bowl_id,
                        "espnId": int(bowler_id_str) if bowler_id_str.isdigit() else None,
                        "bowlerName": bowl_name,
                        "innings": {}
                    }
                bowl_record = bowling_analytics[bowl_key]
                inn_key = str(inn_num)
                if inn_key not in bowl_record["innings"]:
                    bowl_record["innings"][inn_key] = {
                        "totalBalls": 0,
                        "classifiedLengthCount": 0,
                        "classifiedLineCount": 0,
                        "bothLengthAndLineCount": 0,
                        "fullTossCount": 0,
                        "unknownLengthCount": 0,
                        "unknownLineCount": 0,
                        "lengthLineMatrix": {
                            "FULL": {"OUTSIDE_OFF": 0, "OFF_MIDDLE_STUMPS": 0, "LEG_SIDE": 0, "UNKNOWN_LINE": 0},
                            "GOOD": {"OUTSIDE_OFF": 0, "OFF_MIDDLE_STUMPS": 0, "LEG_SIDE": 0, "UNKNOWN_LINE": 0},
                            "SHORT": {"OUTSIDE_OFF": 0, "OFF_MIDDLE_STUMPS": 0, "LEG_SIDE": 0, "UNKNOWN_LINE": 0}
                        },
                        "lengthTotals": {"FULL": 0, "GOOD": 0, "SHORT": 0},
                        "lineTotals": {"OUTSIDE_OFF": 0, "OFF_MIDDLE_STUMPS": 0, "LEG_SIDE": 0}
                    }
                bowl_inn = bowl_record["innings"][inn_key]
                bowl_inn["totalBalls"] += 1
                
                b_length, len_prov = classify_bowling_length(comm)
                b_line, line_prov = classify_bowling_line(comm)
                
                if b_length == "FULL_TOSS":
                    bowl_inn["fullTossCount"] += 1
                elif b_length in ("FULL", "GOOD", "SHORT"):
                    bowl_inn["classifiedLengthCount"] += 1
                    bowl_inn["lengthTotals"][b_length] += 1
                else:
                    bowl_inn["unknownLengthCount"] += 1
                    
                if b_line in ("OUTSIDE_OFF", "OFF_MIDDLE_STUMPS", "LEG_SIDE"):
                    bowl_inn["classifiedLineCount"] += 1
                    bowl_inn["lineTotals"][b_line] += 1
                else:
                    bowl_inn["unknownLineCount"] += 1
                    
                if b_length in ("FULL", "GOOD", "SHORT"):
                    line_key = b_line if b_line != "UNKNOWN" else "UNKNOWN_LINE"
                    bowl_inn["lengthLineMatrix"][b_length][line_key] += 1
                    if b_line != "UNKNOWN":
                        bowl_inn["bothLengthAndLineCount"] += 1

    return {
        "schemaVersion": "1.0.0",
        "matchId": match_id,
        "metadata": {
            "totalDeliveries": unique_deliveries_count + duplicate_deliveries_count,
            "uniqueDeliveries": unique_deliveries_count,
            "duplicateDeliveriesDisambiguated": duplicate_deliveries_count
        },
        "battingAnalytics": batting_analytics,
        "bowlingAnalytics": bowling_analytics
    }

def main():
    parser = argparse.ArgumentParser(description="Build PitchIQ Player Spatial Analytics")
    parser.add_argument("--commentary-dir", default=r"C:\Users\Khush\Desktop\TEST\Test_Commentary", help="Path to commentary CSV directory")
    parser.add_argument("--warehouse-dir", default=r"C:\Users\Khush\Siuu\app\src\main\assets\warehouse", help="Path to warehouse directory")
    parser.add_argument("--output-dir", default=r"C:\Users\Khush\Siuu\app\src\main\assets\warehouse\player_spatial", help="Path to output spatial JSON directory")
    parser.add_argument("--match-id", type=int, default=None, help="Process single match ID")
    parser.add_argument("--limit", type=int, default=None, help="Limit number of matches processed")
    args = parser.parse_args()
    
    os.makedirs(args.output_dir, exist_ok=True)
    
    warehouse_matches_dir = os.path.join(args.warehouse_dir, "matches")
    warehouse_files = glob.glob(os.path.join(warehouse_matches_dir, "match_*.json"))
    warehouse_ids = set()
    for wf in warehouse_files:
        base = os.path.basename(wf)
        m = re.match(r"^match_(\d+)\.json$", base)
        if m:
            warehouse_ids.add(int(m.group(1)))
            
    print(f"Loaded {len(warehouse_ids)} warehouse match IDs.")
    
    comm_files = glob.glob(os.path.join(args.commentary_dir, "*_COMMENTARY.csv"))
    print(f"Found {len(comm_files)} commentary CSV files in {args.commentary_dir}.")
    
    matched_files = []
    for cf in comm_files:
        base = os.path.basename(cf)
        m = re.match(r"^(\d+)_COMMENTARY\.csv$", base, re.IGNORECASE)
        if m:
            mid = int(m.group(1))
            if mid in warehouse_ids:
                if args.match_id is None or args.match_id == mid:
                    matched_files.append((mid, cf))
                    
    matched_files.sort(key=lambda x: x[0])
    if args.limit:
        matched_files = matched_files[:args.limit]
        
    print(f"Matched {len(matched_files)} files to warehouse matches to process.")
    
    resolver = PlayerResolver(args.warehouse_dir)

    total_deliveries_global = 0
    unique_deliveries_global = 0
    duplicate_deliveries_global = 0
    scoring_balls_global = 0
    classified_scoring_shots_global = 0
    boundaries_global = 0
    classified_boundaries_global = 0
    classified_length_global = 0
    classified_line_global = 0
    both_length_line_global = 0
    full_toss_global = 0
    unknown_length_global = 0
    unknown_line_global = 0
    total_output_bytes = 0
    
    for i, (mid, cfile) in enumerate(matched_files):
        match_resolver = resolver.get_match_resolver(mid)
        data = process_match_commentary(cfile, mid, resolver_func=match_resolver)
        out_path = os.path.join(args.output_dir, f"match_{mid}_spatial.json")
        with open(out_path, "w", encoding="utf-8") as out_fp:
            json.dump(data, out_fp, separators=(',', ':'))
            
        file_bytes = os.path.getsize(out_path)
        total_output_bytes += file_bytes
        
        meta = data["metadata"]
        total_deliveries_global += meta["totalDeliveries"]
        unique_deliveries_global += meta["uniqueDeliveries"]
        duplicate_deliveries_global += meta["duplicateDeliveriesDisambiguated"]
        
        for p_id, b_data in data["battingAnalytics"].items():
            for inn_id, inn_data in b_data["innings"].items():
                scoring_balls_global += inn_data["scoringBalls"]
                classified_scoring_shots_global += inn_data["directlyClassifiedBalls"]
                boundaries_global += inn_data["totalBoundaries"]
                classified_boundaries_global += inn_data["directlyClassifiedBoundaries"]
                
        for p_id, bowl_data in data["bowlingAnalytics"].items():
            for inn_id, inn_data in bowl_data["innings"].items():
                classified_length_global += inn_data["classifiedLengthCount"]
                classified_line_global += inn_data["classifiedLineCount"]
                both_length_line_global += inn_data["bothLengthAndLineCount"]
                full_toss_global += inn_data["fullTossCount"]
                unknown_length_global += inn_data["unknownLengthCount"]
                unknown_line_global += inn_data["unknownLineCount"]
                
        if (i + 1) % 25 == 0 or (i + 1) == len(matched_files):
            print(f"Processed {i+1}/{len(matched_files)} matches...")

    print("\n" + "=" * 60)
    print("PIPELINE PROCESSING COMPLETE — FULL REPORT")
    print("=" * 60)
    print(f"Total Matches Processed: {len(matched_files)}")
    print(f"Total Deliveries Scanned: {total_deliveries_global}")
    print(f"Unique Deliveries: {unique_deliveries_global}")
    print(f"Disambiguated Deliveries: {duplicate_deliveries_global}")
    print(f"\n--- WAGON WHEEL METRICS ---")
    print(f"Total Scoring Balls (>0 runs): {scoring_balls_global}")
    print(f"Directly Classified Scoring Shots: {classified_scoring_shots_global} ({classified_scoring_shots_global * 100.0 / max(1, scoring_balls_global):.2f}%)")
    print(f"Total Boundaries (>=4 runs): {boundaries_global}")
    print(f"Directly Classified Boundaries: {classified_boundaries_global} ({classified_boundaries_global * 100.0 / max(1, boundaries_global):.2f}%)")
    print(f"\n--- BOWLING HEATMAP METRICS ---")
    print(f"Total Bowling Deliveries: {total_deliveries_global}")
    print(f"Classified Length (Full/Good/Short): {classified_length_global} ({classified_length_global * 100.0 / max(1, total_deliveries_global):.2f}%)")
    print(f"Classified Line (Off/Stumps/Leg): {classified_line_global} ({classified_line_global * 100.0 / max(1, total_deliveries_global):.2f}%)")
    print(f"Fully Classified Both Length & Line: {both_length_line_global} ({both_length_line_global * 100.0 / max(1, total_deliveries_global):.2f}%)")
    print(f"Full Tosses (tracked separately): {full_toss_global} ({full_toss_global * 100.0 / max(1, total_deliveries_global):.2f}%)")
    print(f"Unknown Length Count: {unknown_length_global} ({unknown_length_global * 100.0 / max(1, total_deliveries_global):.2f}%)")
    print(f"Unknown Line Count: {unknown_line_global} ({unknown_line_global * 100.0 / max(1, total_deliveries_global):.2f}%)")
    print(f"\n--- ASSET STORAGE METRICS ---")
    print(f"Total Companion JSON Files Generated: {len(matched_files)}")
    print(f"Output Directory: {args.output_dir}")
    print(f"Total Storage Size: {total_output_bytes / (1024 * 1024):.2f} MB")
    print(f"Average File Size per Match: {total_output_bytes / (1024 * max(1, len(matched_files))):.1f} KB")

if __name__ == "__main__":
    main()
