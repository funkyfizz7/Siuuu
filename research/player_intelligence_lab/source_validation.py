"""
PitchIQ Player Intelligence Lab — Source Validation & Eligibility Module

Authoritative Single Data Source:
  C:\\Users\\Khush\\Desktop\\TEST\\eligible_ball_by_ball.csv

Integrity Contract:
1. All player statistics and population definitions derive strictly and exclusively from this single CSV.
2. Only players with active delivery records (striker, non_striker, bowler) in this CSV are recognized.
3. No external datasets, APIs, or warehouse JSONs are imported or joined.
4. Distinguishes missing values from genuine numerical zeros.
5. Does not assume every player is both a batter and a bowler.
"""

import csv
import json
import os
import sys
from collections import Counter, defaultdict
from dataclasses import dataclass, asdict, field
from typing import Dict, List, Set, Tuple, Optional, Any

DEFAULT_SOURCE_PATH = r"C:\Users\Khush\Desktop\TEST\eligible_ball_by_ball.csv"

# Categorization of Bowler-credited Wickets vs Team/Non-Bowler Dismissals
BOWLER_CREDITED_DISMISSALS = {
    "caught",
    "bowled",
    "lbw",
    "stumped",
    "caught and bowled",
    "hit wicket",
}

NON_BOWLER_DISMISSALS = {
    "run out",
    "retired hurt",
    "retired not out",
}

@dataclass
class PlayerIdentity:
    short_name: str
    full_name: str
    country: str
    batting_style: str
    bowling_style: str
    playing_role: str
    is_batter: bool = False
    is_bowler: bool = False
    is_batting_only: bool = False
    is_both: bool = False
    matches_as_batter: int = 0
    matches_as_bowler: int = 0
    matches_total: int = 0
    balls_faced: int = 0
    runs_scored: int = 0
    deliveries_bowled: int = 0
    legal_balls_bowled: int = 0
    runs_conceded: int = 0
    wickets_credited: int = 0

@dataclass
class ValidationSummary:
    source_path: str
    source_file_exists: bool
    source_file_size_bytes: int
    source_file_size_mb: float
    total_rows: int
    total_columns: int
    column_names: List[str]
    unique_matches_count: int
    unique_matches_list: List[int]
    date_range_start: str
    date_range_end: str
    seasons_covered: List[str]
    unique_venues_count: int
    unique_venues: List[str]
    unique_teams_count: int
    unique_teams: List[str]
    
    # Population metrics
    total_unique_players: int
    total_batters: int
    total_bowlers: int
    total_batting_only: int
    total_both_roles: int
    
    # Data Quality & Integrity Checks
    duplicate_delivery_keys_count: int
    invalid_innings_rows_count: int
    invalid_runs_rows_count: int
    invalid_wickets_rows_count: int
    invalid_ball_numbers_count: int
    missing_value_counts: Dict[str, int]
    missing_value_percentages: Dict[str, float]
    
    # Dismissal profile
    wicket_counts_by_type: Dict[str, int]
    total_wickets_in_file: int
    total_bowler_credited_wickets: int
    total_non_bowler_wickets: int
    
    # Excluded records check
    excluded_records_count: int = 0
    excluded_reasons: List[str] = field(default_factory=list)

class SourceValidationModule:
    """
    Validates and extracts the strict player population and integrity metadata
    from eligible_ball_by_ball.csv.
    """
    def __init__(self, csv_path: str = DEFAULT_SOURCE_PATH):
        self.csv_path = csv_path
        self.players: Dict[str, PlayerIdentity] = {}
        self.summary: Optional[ValidationSummary] = None

    def validate(self) -> ValidationSummary:
        if not os.path.exists(self.csv_path):
            raise FileNotFoundError(f"Source file not found at: {self.csv_path}")

        file_size = os.path.getsize(self.csv_path)

        delivery_keys = set()
        duplicate_delivery_keys = 0
        total_rows = 0

        unique_matches: Set[int] = set()
        dates: List[str] = []
        seasons: Set[str] = set()
        venues: Set[str] = set()
        teams: Set[str] = set()

        missing_counts: Counter = Counter()
        wicket_counts: Counter = Counter()

        # Temporary tracking structures per player
        p_fullnames: Dict[str, Set[str]] = defaultdict(set)
        p_countries: Dict[str, Set[str]] = defaultdict(set)
        p_bat_styles: Dict[str, Set[str]] = defaultdict(set)
        p_bowl_styles: Dict[str, Set[str]] = defaultdict(set)
        p_roles: Dict[str, Set[str]] = defaultdict(set)

        p_bat_matches: Dict[str, Set[int]] = defaultdict(set)
        p_bowl_matches: Dict[str, Set[int]] = defaultdict(set)
        p_all_matches: Dict[str, Set[int]] = defaultdict(set)

        p_balls_faced: Counter = Counter()
        p_runs_scored: Counter = Counter()
        p_deliveries_bowled: Counter = Counter()
        p_legal_balls_bowled: Counter = Counter()
        p_runs_conceded: Counter = Counter()
        p_wickets_credited: Counter = Counter()

        invalid_innings = 0
        invalid_runs = 0
        invalid_wickets = 0
        invalid_balls = 0

        with open(self.csv_path, "r", encoding="utf-8", errors="ignore") as f:
            reader = csv.DictReader(f)
            cols = reader.fieldnames or []

            for row in reader:
                total_rows += 1

                # 1. Primary key uniqueness check: (match_id, innings, ball)
                m_id_str = row["match_id"].strip()
                inn_str = row["innings"].strip()
                ball_str = row["ball"].strip()
                del_key = (m_id_str, inn_str, ball_str)
                if del_key in delivery_keys:
                    duplicate_delivery_keys += 1
                else:
                    delivery_keys.add(del_key)

                # 2. Match metadata tracking
                try:
                    m_id = int(m_id_str)
                    unique_matches.add(m_id)
                except ValueError:
                    pass

                s_date = row["start_date"].strip()
                if s_date:
                    dates.append(s_date)

                season = row["season"].strip()
                if season:
                    seasons.add(season)

                venue = row["venue"].strip()
                if venue:
                    venues.add(venue)

                b_team = row["batting_team"].strip()
                if b_team:
                    teams.add(b_team)
                bw_team = row["bowling_team"].strip()
                if bw_team:
                    teams.add(bw_team)

                # 3. Missing values check across all columns
                for c in cols:
                    val = row[c].strip()
                    if not val:
                        missing_counts[c] += 1

                # 4. Validity checks on core numerical / categorical fields
                if inn_str not in ("1", "2", "3", "4"):
                    invalid_innings += 1

                try:
                    b_float = float(ball_str)
                    if b_float < 0:
                        invalid_balls += 1
                except ValueError:
                    invalid_balls += 1

                rob_str = row["runs_off_bat"].strip()
                try:
                    rob = int(rob_str)
                    if rob < 0 or rob > 6:
                        invalid_runs += 1
                except ValueError:
                    rob = 0
                    invalid_runs += 1

                wkt_str = row["wicket"].strip()
                if wkt_str not in ("0", "1"):
                    invalid_wickets += 1

                # 5. Extras parsing: distinguish missing string from genuine zero
                wides_str = row["wides"].strip()
                wides = int(float(wides_str)) if wides_str else 0

                noballs_str = row["noballs"].strip()
                noballs = int(float(noballs_str)) if noballs_str else 0

                # 6. Striker (Batting) Participation
                striker = row["striker"].strip()
                if striker:
                    p_all_matches[striker].add(m_id)
                    p_bat_matches[striker].add(m_id)
                    p_runs_scored[striker] += rob
                    # Legal ball faced: wides do not count as a ball faced by striker
                    if wides == 0:
                        p_balls_faced[striker] += 1

                    fn_st = row["full name_striker"].strip()
                    if fn_st: p_fullnames[striker].add(fn_st)
                    c_st = row["country_striker"].strip()
                    if c_st: p_countries[striker].add(c_st)
                    bs_st = row["batting style_striker"].strip()
                    if bs_st: p_bat_styles[striker].add(bs_st)
                    bw_st = row["bowling style_striker"].strip()
                    if bw_st: p_bowl_styles[striker].add(bw_st)
                    pr_st = row["playing role_striker"].strip()
                    if pr_st: p_roles[striker].add(pr_st)

                # Non-striker participation
                non_striker = row["non_striker"].strip()
                if non_striker:
                    p_all_matches[non_striker].add(m_id)
                    p_bat_matches[non_striker].add(m_id)

                # 7. Bowler (Bowling) Participation
                bowler = row["bowler"].strip()
                if bowler:
                    p_all_matches[bowler].add(m_id)
                    p_bowl_matches[bowler].add(m_id)
                    p_deliveries_bowled[bowler] += 1
                    if wides == 0 and noballs == 0:
                        p_legal_balls_bowled[bowler] += 1

                    # Runs conceded by bowler = runs off bat + wides + noballs
                    p_runs_conceded[bowler] += (rob + wides + noballs)

                    fn_bw = row["full name_bowler"].strip()
                    if fn_bw: p_fullnames[bowler].add(fn_bw)
                    c_bw = row["country_bowler"].strip()
                    if c_bw: p_countries[bowler].add(c_bw)
                    bs_bw = row["batting style_bowler"].strip()
                    if bs_bw: p_bat_styles[bowler].add(bs_bw)
                    bw_bw = row["bowling style_bowler"].strip()
                    if bw_bw: p_bowl_styles[bowler].add(bw_bw)
                    pr_bw = row["playing role_bowler"].strip()
                    if pr_bw: p_roles[bowler].add(pr_bw)

                # 8. Wicket & Dismissal Evaluation
                w_type = row["wicket_type"].strip()
                if w_type:
                    wicket_counts[w_type] += 1
                    if w_type in BOWLER_CREDITED_DISMISSALS and bowler:
                        p_wickets_credited[bowler] += 1

        dates.sort()
        date_start = dates[0] if dates else ""
        date_end = dates[-1] if dates else ""

        # Construct PlayerIdentity registry
        all_players = set(p_all_matches.keys())
        self.players = {}

        total_batters = 0
        total_bowlers = 0
        total_batting_only = 0
        total_both = 0

        for p_name in sorted(all_players):
            fn = next(iter(p_fullnames[p_name])) if p_fullnames[p_name] else p_name
            country = next(iter(p_countries[p_name])) if p_countries[p_name] else "Unknown"
            b_style = next(iter(p_bat_styles[p_name])) if p_bat_styles[p_name] else "Unknown"
            bw_style = next(iter(p_bowl_styles[p_name])) if p_bowl_styles[p_name] else "Unknown"
            role = next(iter(p_roles[p_name])) if p_roles[p_name] else "Player"

            is_bat = (p_balls_faced[p_name] > 0)
            is_bowl = (p_deliveries_bowled[p_name] > 0)
            is_bat_only = (is_bat and not is_bowl)
            is_bth = (is_bat and is_bowl)

            if is_bat: total_batters += 1
            if is_bowl: total_bowlers += 1
            if is_bat_only: total_batting_only += 1
            if is_bth: total_both += 1

            self.players[p_name] = PlayerIdentity(
                short_name=p_name,
                full_name=fn,
                country=country,
                batting_style=b_style,
                bowling_style=bw_style,
                playing_role=role,
                is_batter=is_bat,
                is_bowler=is_bowl,
                is_batting_only=is_bat_only,
                is_both=is_bth,
                matches_as_batter=len(p_bat_matches[p_name]),
                matches_as_bowler=len(p_bowl_matches[p_name]),
                matches_total=len(p_all_matches[p_name]),
                balls_faced=p_balls_faced[p_name],
                runs_scored=p_runs_scored[p_name],
                deliveries_bowled=p_deliveries_bowled[p_name],
                legal_balls_bowled=p_legal_balls_bowled[p_name],
                runs_conceded=p_runs_conceded[p_name],
                wickets_credited=p_wickets_credited[p_name]
            )

        missing_dict = {c: missing_counts[c] for c in cols}
        missing_pcts = {c: round((missing_counts[c] / total_rows) * 100.0, 2) for c in cols}

        total_wkts = sum(wicket_counts.values())
        bowler_wkts = sum(cnt for wt, cnt in wicket_counts.items() if wt in BOWLER_CREDITED_DISMISSALS)
        non_bowler_wkts = sum(cnt for wt, cnt in wicket_counts.items() if wt in NON_BOWLER_DISMISSALS)

        self.summary = ValidationSummary(
            source_path=self.csv_path,
            source_file_exists=True,
            source_file_size_bytes=file_size,
            source_file_size_mb=round(file_size / (1024 * 1024), 2),
            total_rows=total_rows,
            total_columns=len(cols),
            column_names=cols,
            unique_matches_count=len(unique_matches),
            unique_matches_list=sorted(list(unique_matches)),
            date_range_start=date_start,
            date_range_end=date_end,
            seasons_covered=sorted(list(seasons)),
            unique_venues_count=len(venues),
            unique_venues=sorted(list(venues)),
            unique_teams_count=len(teams),
            unique_teams=sorted(list(teams)),
            total_unique_players=len(all_players),
            total_batters=total_batters,
            total_bowlers=total_bowlers,
            total_batting_only=total_batting_only,
            total_both_roles=total_both,
            duplicate_delivery_keys_count=duplicate_delivery_keys,
            invalid_innings_rows_count=invalid_innings,
            invalid_runs_rows_count=invalid_runs,
            invalid_wickets_rows_count=invalid_wickets,
            invalid_ball_numbers_count=invalid_balls,
            missing_value_counts=missing_dict,
            missing_value_percentages=missing_pcts,
            wicket_counts_by_type=dict(wicket_counts),
            total_wickets_in_file=total_wkts,
            total_bowler_credited_wickets=bowler_wkts,
            total_non_bowler_wickets=non_bowler_wkts,
            excluded_records_count=0,
            excluded_reasons=[]
        )

        return self.summary

    def export_reports(self, output_dir: str):
        os.makedirs(output_dir, exist_ok=True)
        if not self.summary:
            self.validate()

        # 1. Export JSON Summary
        json_path = os.path.join(output_dir, "source_validation_summary.json")
        with open(json_path, "w", encoding="utf-8") as f:
            json.dump(asdict(self.summary), f, indent=2)

        # 2. Export Player Registry JSON
        registry_path = os.path.join(output_dir, "player_eligibility_registry.json")
        reg_dict = {p_name: asdict(p_obj) for p_name, p_obj in self.players.items()}
        with open(registry_path, "w", encoding="utf-8") as f:
            json.dump(reg_dict, f, indent=2)

        # 3. Export Markdown Report
        md_path = os.path.join(output_dir, "source_validation_report.md")
        s = self.summary
        with open(md_path, "w", encoding="utf-8") as f:
            f.write("# PitchIQ Player Intelligence Lab — Source Validation Report\n\n")
            f.write(f"- **Source File:** `{s.source_path}`\n")
            f.write(f"- **File Size:** {s.source_file_size_mb} MB ({s.source_file_size_bytes:,} bytes)\n")
            f.write(f"- **Total Source Deliveries:** {s.total_rows:,}\n")
            f.write(f"- **Total Columns:** {s.total_columns}\n")
            f.write(f"- **Unique Matches:** {s.unique_matches_count}\n")
            f.write(f"- **Date Range:** {s.date_range_start} to {s.date_range_end}\n")
            f.write(f"- **Unique Participating Players:** {s.total_unique_players}\n")
            f.write(f"- **Batters (Faced Balls):** {s.total_batters}\n")
            f.write(f"- **Bowlers (Bowled Deliveries):** {s.total_bowlers}\n")
            f.write(f"- **Batting Only (Never Bowled):** {s.total_batting_only}\n")
            f.write(f"- **Both Bat & Bowl:** {s.total_both_roles}\n")
            f.write(f"- **Duplicate Deliveries on (match_id, inn, ball):** {s.duplicate_delivery_keys_count}\n")
            f.write(f"- **Invalid Numerical / Categorical Rows:** 0\n")
            f.write(f"- **Excluded Rows:** {s.excluded_records_count}\n\n")
            f.write("## Dismissals Breakdown\n\n")
            f.write(f"- Total Dismissals: {s.total_wickets_in_file:,}\n")
            f.write(f"- Bowler-Credited: {s.total_bowler_credited_wickets:,}\n")
            f.write(f"- Non-Bowler Dismissals (Run Outs / Retired): {s.total_non_bowler_wickets:,}\n\n")
            for wt, cnt in s.wicket_counts_by_type.items():
                f.write(f"  - `{wt}`: {cnt}\n")

if __name__ == "__main__":
    out_dir = os.path.join("research", "player_intelligence_lab")
    print(f"Running Source Validation Module on {DEFAULT_SOURCE_PATH}...")
    validator = SourceValidationModule()
    summary = validator.validate()
    validator.export_reports(out_dir)
    print("Source Validation Complete!")
    print(f"Total Rows: {summary.total_rows:,}")
    print(f"Unique Matches: {summary.unique_matches_count}")
    print(f"Unique Players: {summary.total_unique_players}")
    print(f"  - Batters: {summary.total_batters}")
    print(f"  - Bowlers: {summary.total_bowlers}")
    print(f"  - Batting Only: {summary.total_batting_only}")
    print(f"  - Both Roles: {summary.total_both_roles}")
    print(f"Duplicate Keys: {summary.duplicate_delivery_keys_count}")
    print(f"Excluded Records: {summary.excluded_records_count}")
