"""
PitchIQ Player Intelligence Lab — Feature Engineering Module

Authoritative Single Data Source:
  C:\\Users\\Khush\\Desktop\\TEST\\eligible_ball_by_ball.csv

Derives comprehensive player-level feature tables exclusively from the source CSV.
"""

import csv
import json
import math
import os
import sys
from collections import defaultdict, Counter
from dataclasses import dataclass, asdict, field
from typing import Dict, List, Set, Tuple, Optional, Any

DEFAULT_SOURCE_PATH = r"C:\Users\Khush\Desktop\TEST\eligible_ball_by_ball.csv"

BOWLER_CREDITED_DISMISSALS = {
    "caught",
    "bowled",
    "lbw",
    "stumped",
    "caught and bowled",
    "hit wicket",
}

ACCURATE_LENGTHS = {
    "good length",
    "full",
    "yorker",
    "block hole",
    "slot",
    "half-volley",
}

INACCURATE_LENGTHS = {
    "short",
    "bouncer",
    "full toss",
    "low full toss",
    "back of length",
    "half tracker",
}

ACCURATE_LINES = {
    "outside off",
    "on off stump",
    "on middle stump",
    "on the stumps",
    "on middle and off",
    "on middle and leg",
    "corridor of uncertainty",
}

INACCURATE_LINES = {
    "down the leg",
    "outside leg stump",
    "leg",
    "bodyline",
}

def balls_to_overs_notation(legal_balls: int) -> str:
    """Formats legal deliveries into standard cricket over notation (e.g. 1245.0, 315.4)."""
    return f"{legal_balls // 6}.{legal_balls % 6}"

def is_accurate_delivery(blen: str, bline: str, total_runs_on_ball: int) -> Tuple[bool, bool]:
    """
    Evaluates whether a delivery was accurate based on commentary line/length,
    or falls back to 0 runs conceded rule if undescribed.
    Returns: (is_accurate: bool, is_described: bool)
    """
    blen_clean = blen.strip().lower()
    bline_clean = bline.strip().lower()
    is_described = (blen_clean != "" or bline_clean != "")
    
    if is_described:
        len_ok = (blen_clean in ACCURATE_LENGTHS) if blen_clean else True
        line_ok = (bline_clean in ACCURATE_LINES) if bline_clean else True
        return (len_ok and line_ok), True
    else:
        # Fallback: accurate if 0 runs conceded, inaccurate if > 0 runs conceded
        return (total_runs_on_ball == 0), False

def classify_scoring_region(sd: str, sp: str) -> str:
    """
    Classifies the scoring region on the ground based on commentary shot direction
    and shot played.
    """
    sd = sd.lower().strip()
    sp = sp.lower().strip()
    combined = f"{sd} {sp}".strip()

    if not combined:
        return "Unclassified"

    # 1. Primary Direction Keyword Matching
    if any(k in combined for k in ["cover", "extra cover", "covers", "sweeper cover"]):
        return "Cover"
    if any(k in combined for k in ["mid wicket", "midwicket", "cow corner", "through midwicket", "through mid-wicket"]):
        return "Mid Wicket"
    if any(k in combined for k in ["point", "gully", "cover-point", "cover point", "backward point"]):
        return "Point"
    if any(k in combined for k in ["third man", "third-man", "thirdman", "slips", "slip", "behind the wicket"]):
        return "Third Man"
    if any(k in combined for k in ["square leg", "backward square", "fine of square"]):
        return "Square Leg"
    if any(k in combined for k in ["fine leg", "fine-leg", "leg glance", "glance", "long leg"]):
        return "Fine Leg"
    if any(k in combined for k in ["mid-on", "mid on", "long on", "long-on"]):
        return "Mid-On / Long-On"
    if any(k in combined for k in ["mid-off", "mid off", "long off", "long-off"]):
        return "Mid-Off / Long-Off"
    if any(k in combined for k in ["straight", "down the ground", "past the bowler", "bowler's head"]):
        return "Straight"

    # 2. Shot-Type Heuristic Mapping when Direction is Generic / Missing
    if "cut" in sp or "dab" in sp:
        return "Point"
    if "sweep" in sp or "reverse sweep" in sp:
        return "Square Leg"
    if "pull" in sp or "hook" in sp:
        return "Mid Wicket"
    if "flick" in sp or "clips" in sp or "work" in sp or "whips" in sp or "nudge" in sp:
        return "Mid Wicket"
    if "drive" in sp or "punch" in sp:
        return "Cover"
    if "edge" in sp:
        return "Third Man"
    if "leg side" in sd or "on side" in sd:
        return "Mid Wicket"
    if "off side" in sd:
        return "Cover"

    return "Other"

class FeatureEngineeringPipeline:
    """
    Builds clean, reproducible feature tables for all 344 players.
    """
    def __init__(self, csv_path: str = DEFAULT_SOURCE_PATH):
        self.csv_path = csv_path
        
        # Player metadata
        self.p_fullnames: Dict[str, str] = {}
        self.p_countries: Dict[str, str] = {}
        self.p_bat_styles: Dict[str, str] = {}
        self.p_bowl_styles: Dict[str, str] = {}
        self.p_roles: Dict[str, str] = {}
        
        # Batting aggregations
        self.bat_matches: Dict[str, Set[str]] = defaultdict(set)
        self.bat_inns_runs: Dict[str, Dict[Tuple[str, str], int]] = defaultdict(lambda: defaultdict(int))
        self.bat_inns_balls: Dict[str, Dict[Tuple[str, str], int]] = defaultdict(lambda: defaultdict(int))
        self.bat_dismissals: Dict[str, int] = defaultdict(int)
        self.bat_balls_faced: Dict[str, int] = defaultdict(int)
        self.bat_runs: Dict[str, int] = defaultdict(int)
        self.bat_fours: Dict[str, int] = defaultdict(int)
        self.bat_sixes: Dict[str, int] = defaultdict(int)
        self.bat_dots: Dict[str, int] = defaultdict(int)
        self.bat_regions_runs: Dict[str, Counter] = defaultdict(Counter)
        self.bat_regions_balls: Dict[str, Counter] = defaultdict(Counter)

        # Bowling aggregations
        self.bowl_matches: Dict[str, Set[str]] = defaultdict(set)
        self.bowl_inns: Dict[str, Set[Tuple[str, str]]] = defaultdict(set)
        self.bowl_total_balls: Dict[str, int] = defaultdict(int)
        self.bowl_legal_balls: Dict[str, int] = defaultdict(int)
        self.bowl_runs_conceded: Dict[str, int] = defaultdict(int)
        self.bowl_wides: Dict[str, int] = defaultdict(int)
        self.bowl_noballs: Dict[str, int] = defaultdict(int)
        self.bowl_wickets: Dict[str, int] = defaultdict(int)
        self.bowl_dots: Dict[str, int] = defaultdict(int)
        
        # Over-by-over maiden calculation: (bowler, match_id, innings, over_number) -> runs conceded
        self.over_runs: Dict[Tuple[str, str, str, int], int] = defaultdict(int)
        self.over_balls: Dict[Tuple[str, str, str, int], int] = defaultdict(int)

        # Accuracy tracking
        self.bowl_described_balls: Dict[str, int] = defaultdict(int)
        self.bowl_accurate_balls: Dict[str, int] = defaultdict(int)
        self.bowl_good_length_balls: Dict[str, int] = defaultdict(int)
        self.bowl_full_length_balls: Dict[str, int] = defaultdict(int)
        self.bowl_short_length_balls: Dict[str, int] = defaultdict(int)
        self.bowl_outside_off_balls: Dict[str, int] = defaultdict(int)
        self.bowl_stumps_line_balls: Dict[str, int] = defaultdict(int)
        self.bowl_leg_side_balls: Dict[str, int] = defaultdict(int)

    def extract_and_aggregate(self):
        print(f"Reading {self.csv_path} for feature extraction...")
        
        with open(self.csv_path, "r", encoding="utf-8", errors="ignore") as f:
            reader = csv.DictReader(f)
            
            for r in reader:
                m_id = r["match_id"].strip()
                inn = r["innings"].strip()
                ball_str = r["ball"].strip()
                striker = r["striker"].strip()
                bowler = r["bowler"].strip()
                pd = r["player_dismissed"].strip()
                
                rob_str = r["runs_off_bat"].strip()
                rob = int(rob_str) if rob_str else 0

                wides_str = r["wides"].strip()
                wides = int(float(wides_str)) if wides_str else 0

                noballs_str = r["noballs"].strip()
                noballs = int(float(noballs_str)) if noballs_str else 0

                total_runs_on_ball = rob + wides + noballs

                # Parse over number from ball_str (e.g. 14.3 -> over 14)
                try:
                    over_no = int(float(ball_str))
                except ValueError:
                    over_no = 0

                # 1. Striker Metadata & Batting Stats
                if striker:
                    if striker not in self.p_fullnames:
                        self.p_fullnames[striker] = r["full name_striker"].strip() or striker
                        self.p_countries[striker] = r["country_striker"].strip() or "Unknown"
                        self.p_bat_styles[striker] = r["batting style_striker"].strip() or "Unknown"
                        self.p_bowl_styles[striker] = r["bowling style_striker"].strip() or "Unknown"
                        self.p_roles[striker] = r["playing role_striker"].strip() or "Player"

                    self.bat_matches[striker].add(m_id)
                    self.bat_inns_runs[striker][(m_id, inn)] += rob
                    self.bat_runs[striker] += rob

                    if rob == 4:
                        self.bat_fours[striker] += 1
                    elif rob == 6:
                        self.bat_sixes[striker] += 1

                    # Wides do not count toward striker balls faced
                    if wides == 0:
                        self.bat_balls_faced[striker] += 1
                        self.bat_inns_balls[striker][(m_id, inn)] += 1
                        if rob == 0:
                            self.bat_dots[striker] += 1

                    # Shot region tracking on scoring shots
                    if rob > 0:
                        sd = r["shot_direction"].strip()
                        sp = r["shot_played"].strip()
                        region = classify_scoring_region(sd, sp)
                        self.bat_regions_runs[striker][region] += rob
                        self.bat_regions_balls[striker][region] += 1

                # 2. Dismissal Tracking
                if pd:
                    self.bat_dismissals[pd] += 1

                # 3. Bowler Metadata & Bowling Stats
                if bowler:
                    if bowler not in self.p_fullnames:
                        self.p_fullnames[bowler] = r["full name_bowler"].strip() or bowler
                        self.p_countries[bowler] = r["country_bowler"].strip() or "Unknown"
                        self.p_bat_styles[bowler] = r["batting style_bowler"].strip() or "Unknown"
                        self.p_bowl_styles[bowler] = r["bowling style_bowler"].strip() or "Unknown"
                        self.p_roles[bowler] = r["playing role_bowler"].strip() or "Player"

                    self.bowl_matches[bowler].add(m_id)
                    self.bowl_inns[bowler].add((m_id, inn))
                    self.bowl_total_balls[bowler] += 1
                    self.bowl_runs_conceded[bowler] += total_runs_on_ball
                    self.bowl_wides[bowler] += wides
                    self.bowl_noballs[bowler] += noballs

                    # Legal delivery (wides and noballs are excluded from legal balls count)
                    is_legal = (wides == 0 and noballs == 0)
                    if is_legal:
                        self.bowl_legal_balls[bowler] += 1
                        # Track over for maiden calculation
                        self.over_balls[(bowler, m_id, inn, over_no)] += 1

                    self.over_runs[(bowler, m_id, inn, over_no)] += total_runs_on_ball

                    if total_runs_on_ball == 0:
                        self.bowl_dots[bowler] += 1

                    # Bowler Wicket Check
                    if r["wicket"].strip() == "1":
                        wt = r["wicket_type"].strip().lower()
                        if wt in BOWLER_CREDITED_DISMISSALS:
                            self.bowl_wickets[bowler] += 1

                    # Line and Length Accuracy Evaluation
                    blen = r["ball_length"].strip().lower()
                    bline = r["ball_line"].strip().lower()

                    if blen in ("good length",):
                        self.bowl_good_length_balls[bowler] += 1
                    elif blen in ("full", "yorker", "block hole", "slot", "half-volley"):
                        self.bowl_full_length_balls[bowler] += 1
                    elif blen in ("short", "bouncer", "back of length", "half tracker"):
                        self.bowl_short_length_balls[bowler] += 1

                    if bline in ("outside off", "corridor of uncertainty"):
                        self.bowl_outside_off_balls[bowler] += 1
                    elif bline in ("on off stump", "on middle stump", "on the stumps", "on middle and off"):
                        self.bowl_stumps_line_balls[bowler] += 1
                    elif bline in ("on leg stump", "on middle and leg", "down the leg", "outside leg stump", "leg"):
                        self.bowl_leg_side_balls[bowler] += 1

                    is_acc, is_desc = is_accurate_delivery(blen, bline, total_runs_on_ball)
                    if is_desc:
                        self.bowl_described_balls[bowler] += 1
                    if is_acc:
                        self.bowl_accurate_balls[bowler] += 1

        print("Data extraction and aggregation complete!")

    def generate_batting_table(self) -> List[Dict[str, Any]]:
        rows = []
        all_batters = sorted(list(self.bat_matches.keys()))

        for p in all_batters:
            fn = self.p_fullnames.get(p, p)
            country = self.p_countries.get(p, "Unknown")
            b_style = self.p_bat_styles.get(p, "Unknown")
            role = self.p_roles.get(p, "Batter")

            matches = len(self.bat_matches[p])
            inns_map = self.bat_inns_runs[p]
            innings = len(inns_map)
            runs = self.bat_runs[p]
            balls = self.bat_balls_faced[p]
            dismissals = self.bat_dismissals[p]
            not_outs = max(0, innings - dismissals)

            # Batting Average: available only if dismissals > 0
            if dismissals > 0:
                bat_avg = round(runs / dismissals, 2)
            else:
                bat_avg = None # Marked unavailable (not silently 0.0)

            # Strike Rate: available only if balls > 0
            if balls > 0:
                sr = round((runs / balls) * 100.0, 2)
                dot_pct = round((self.bat_dots[p] / balls) * 100.0, 2)
            else:
                sr = None
                dot_pct = None

            fours = self.bat_fours[p]
            sixes = self.bat_sixes[p]
            b_runs = (4 * fours) + (6 * sixes)
            b_pct = round((b_runs / runs) * 100.0, 2) if runs > 0 else 0.0

            # Hundreds, Fifties, Ducks, Highest Score
            h100 = 0
            h50 = 0
            ducks = 0
            highest_score = 0
            highest_not_out = False

            for inn_key, r_score in inns_map.items():
                if r_score >= 100:
                    h100 += 1
                elif r_score >= 50:
                    h50 += 1

                if r_score == 0:
                    # Duck if dismissed
                    ducks += 1

                if r_score > highest_score:
                    highest_score = r_score

            # Primary Scoring Region
            regions = self.bat_regions_runs[p]
            if regions:
                top_region, top_r = regions.most_common(1)[0]
                top_pct = round((top_r / runs) * 100.0, 2) if runs > 0 else 0.0
            else:
                top_region = "Unavailable"
                top_r = 0
                top_pct = 0.0

            rows.append({
                "player_id": p,
                "full_name": fn,
                "country": country,
                "batting_style": b_style,
                "playing_role": role,
                "matches": matches,
                "innings": innings,
                "not_outs": not_outs,
                "runs": runs,
                "balls_faced": balls,
                "batting_average": bat_avg,
                "strike_rate": sr,
                "fours": fours,
                "sixes": sixes,
                "boundary_runs": b_runs,
                "boundary_run_pct": b_pct,
                "dot_balls_faced": self.bat_dots[p],
                "dot_ball_pct": dot_pct,
                "hundreds": h100,
                "fifties": h50,
                "ducks": ducks,
                "highest_score": highest_score,
                "primary_scoring_region": top_region,
                "primary_region_runs": top_r,
                "primary_region_pct": top_pct,
                "cover_runs": regions["Cover"],
                "mid_wicket_runs": regions["Mid Wicket"],
                "point_runs": regions["Point"],
                "third_man_runs": regions["Third Man"],
                "square_leg_runs": regions["Square Leg"],
                "fine_leg_runs": regions["Fine Leg"],
                "mid_on_runs": regions["Mid-On / Long-On"],
                "mid_off_runs": regions["Mid-Off / Long-Off"],
                "straight_runs": regions["Straight"],
            })

        return rows

    def generate_bowling_table(self) -> List[Dict[str, Any]]:
        rows = []
        all_bowlers = sorted(list(self.bowl_matches.keys()))

        # Calculate maidens per bowler (complete 6-ball overs with 0 runs conceded)
        bowler_maidens: Counter = Counter()
        for (bw, m_id, inn, over_no), legal_cnt in self.over_balls.items():
            if legal_cnt == 6:
                if self.over_runs[(bw, m_id, inn, over_no)] == 0:
                    bowler_maidens[bw] += 1

        for p in all_bowlers:
            fn = self.p_fullnames.get(p, p)
            country = self.p_countries.get(p, "Unknown")
            bw_style = self.p_bowl_styles.get(p, "Unknown")
            role = self.p_roles.get(p, "Bowler")

            matches = len(self.bowl_matches[p])
            innings = len(self.bowl_inns[p])
            tot_balls = self.bowl_total_balls[p]
            legal_balls = self.bowl_legal_balls[p]
            overs_str = balls_to_overs_notation(legal_balls)
            overs_dec = round(legal_balls / 6.0, 2)
            maidens = bowler_maidens[p]
            runs_conc = self.bowl_runs_conceded[p]
            wkts = self.bowl_wickets[p]
            wides = self.bowl_wides[p]
            noballs = self.bowl_noballs[p]
            dots = self.bowl_dots[p]
            dot_pct = round((dots / tot_balls) * 100.0, 2) if tot_balls > 0 else 0.0

            # Bowling Average: available only if wickets > 0
            if wkts > 0:
                bowl_avg = round(runs_conc / wkts, 2)
                bowl_sr = round(legal_balls / wkts, 2)
            else:
                bowl_avg = None # Unavailable
                bowl_sr = None  # Unavailable

            # Economy Rate: available only if legal_balls > 0
            if legal_balls > 0:
                econ = round((runs_conc / legal_balls) * 6.0, 2)
            else:
                econ = None

            # Accuracy Metrics
            acc_balls = self.bowl_accurate_balls[p]
            acc_pct = round((acc_balls / tot_balls) * 100.0, 2) if tot_balls > 0 else 0.0
            desc_balls = self.bowl_described_balls[p]
            desc_pct = round((desc_balls / tot_balls) * 100.0, 2) if tot_balls > 0 else 0.0

            rows.append({
                "player_id": p,
                "full_name": fn,
                "country": country,
                "bowling_style": bw_style,
                "playing_role": role,
                "matches": matches,
                "innings": innings,
                "overs_bowled": overs_str,
                "overs_decimal": overs_dec,
                "legal_balls_bowled": legal_balls,
                "total_deliveries_bowled": tot_balls,
                "maidens": maidens,
                "runs_conceded": runs_conc,
                "wickets": wkts,
                "bowling_average": bowl_avg,
                "economy_rate": econ,
                "bowling_strike_rate": bowl_sr,
                "wides_conceded": wides,
                "noballs_conceded": noballs,
                "dot_balls_bowled": dots,
                "bowling_dot_pct": dot_pct,
                "line_length_accurate_balls": acc_balls,
                "line_length_accuracy_pct": acc_pct,
                "described_balls": desc_balls,
                "described_coverage_pct": desc_pct,
                "good_length_balls": self.bowl_good_length_balls[p],
                "full_length_balls": self.bowl_full_length_balls[p],
                "short_length_balls": self.bowl_short_length_balls[p],
                "outside_off_balls": self.bowl_outside_off_balls[p],
                "stumps_line_balls": self.bowl_stumps_line_balls[p],
                "leg_side_balls": self.bowl_leg_side_balls[p],
            })

        return rows

    def generate_allrounder_table(self, bat_rows: List[Dict[str, Any]], bowl_rows: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        bat_dict = {r["player_id"]: r for r in bat_rows}
        bowl_dict = {r["player_id"]: r for r in bowl_rows}

        common_ids = sorted(list(set(bat_dict.keys()) & set(bowl_dict.keys())))
        rows = []

        for p in common_ids:
            bt = bat_dict[p]
            bl = bowl_dict[p]

            # Composite Allrounder Score (Batting Avg minus Bowling Avg when both available)
            bat_avg = bt["batting_average"]
            bowl_avg = bl["bowling_average"]
            if bat_avg is not None and bowl_avg is not None:
                allrounder_differential = round(bat_avg - bowl_avg, 2)
            else:
                allrounder_differential = None

            rows.append({
                "player_id": p,
                "full_name": bt["full_name"],
                "country": bt["country"],
                "batting_style": bt["batting_style"],
                "bowling_style": bl["bowling_style"],
                "playing_role": bt["playing_role"],
                "matches_batted": bt["matches"],
                "runs": bt["runs"],
                "batting_average": bat_avg,
                "batting_strike_rate": bt["strike_rate"],
                "hundreds": bt["hundreds"],
                "fifties": bt["fifties"],
                "primary_scoring_region": bt["primary_scoring_region"],
                "matches_bowled": bl["matches"],
                "overs_bowled": bl["overs_bowled"],
                "wickets": bl["wickets"],
                "bowling_average": bowl_avg,
                "economy_rate": bl["economy_rate"],
                "bowling_strike_rate": bl["bowling_strike_rate"],
                "line_length_accuracy_pct": bl["line_length_accuracy_pct"],
                "allrounder_differential": allrounder_differential
            })

        return rows

    def generate_master_table(self, bat_rows: List[Dict[str, Any]], bowl_rows: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        bat_dict = {r["player_id"]: r for r in bat_rows}
        bowl_dict = {r["player_id"]: r for r in bowl_rows}

        all_ids = sorted(list(set(bat_dict.keys()) | set(bowl_dict.keys())))
        rows = []

        for p in all_ids:
            bt = bat_dict.get(p)
            bl = bowl_dict.get(p)

            is_bat = (bt is not None)
            is_bowl = (bl is not None)
            fn = bt["full_name"] if bt else bl["full_name"]
            country = bt["country"] if bt else bl["country"]
            bat_style = bt["batting_style"] if bt else "Unknown"
            bowl_style = bl["bowling_style"] if bl else (bt["batting_style"] if bt else "Unknown")
            role = bt["playing_role"] if bt else bl["playing_role"]

            rows.append({
                "player_id": p,
                "full_name": fn,
                "country": country,
                "batting_style": bat_style,
                "bowling_style": bowl_style,
                "playing_role": role,
                "is_batter": is_bat,
                "is_bowler": is_bowl,
                "is_batting_only": (is_bat and not is_bowl),
                "is_both": (is_bat and is_bowl),
                "total_runs": bt["runs"] if bt else 0,
                "balls_faced": bt["balls_faced"] if bt else 0,
                "batting_average": bt["batting_average"] if bt else None,
                "batting_strike_rate": bt["strike_rate"] if bt else None,
                "hundreds": bt["hundreds"] if bt else 0,
                "fifties": bt["fifties"] if bt else 0,
                "primary_scoring_region": bt["primary_scoring_region"] if bt else "N/A",
                "overs_bowled": bl["overs_bowled"] if bl else "0.0",
                "wickets": bl["wickets"] if bl else 0,
                "bowling_average": bl["bowling_average"] if bl else None,
                "economy_rate": bl["economy_rate"] if bl else None,
                "bowling_strike_rate": bl["bowling_strike_rate"] if bl else None,
                "line_length_accuracy_pct": bl["line_length_accuracy_pct"] if bl else None
            })

        return rows

    def export_all(self, output_dir: str):
        os.makedirs(output_dir, exist_ok=True)
        self.extract_and_aggregate()

        bat_rows = self.generate_batting_table()
        bowl_rows = self.generate_bowling_table()
        ar_rows = self.generate_allrounder_table(bat_rows, bowl_rows)
        master_rows = self.generate_master_table(bat_rows, bowl_rows)

        # Export Batting Features CSV
        bat_csv_path = os.path.join(output_dir, "batting_features.csv")
        with open(bat_csv_path, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=list(bat_rows[0].keys()))
            writer.writeheader()
            writer.writerows(bat_rows)

        # Export Bowling Features CSV
        bowl_csv_path = os.path.join(output_dir, "bowling_features.csv")
        with open(bowl_csv_path, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=list(bowl_rows[0].keys()))
            writer.writeheader()
            writer.writerows(bowl_rows)

        # Export Allrounder Features CSV
        ar_csv_path = os.path.join(output_dir, "allrounder_features.csv")
        with open(ar_csv_path, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=list(ar_rows[0].keys()))
            writer.writeheader()
            writer.writerows(ar_rows)

        # Export Master Player Features CSV
        master_csv_path = os.path.join(output_dir, "player_features_master.csv")
        with open(master_csv_path, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=list(master_rows[0].keys()))
            writer.writeheader()
            writer.writerows(master_rows)

        print(f"Exported {len(bat_rows)} batters to {bat_csv_path}")
        print(f"Exported {len(bowl_rows)} bowlers to {bowl_csv_path}")
        print(f"Exported {len(ar_rows)} allrounders to {ar_csv_path}")
        print(f"Exported {len(master_rows)} players to {master_csv_path}")

if __name__ == "__main__":
    out_dir = os.path.join("research", "player_intelligence_lab")
    pipeline = FeatureEngineeringPipeline()
    pipeline.export_all(out_dir)
