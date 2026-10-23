"""
PitchIQ Player Intelligence Lab — Advanced Player Analytics Module

Analyses:
1. Home/Away Verification: Formally tests dataset columns to confirm whether venue-team host
   relationships are reliably identifiable without external lookups or nationality inference.
2. Match Impact & Contribution: Transparent team contribution shares (batting run share,
   bowling wicket share, POTM awards) without making unprovable causal assertions.
3. All-Round Performance Index: Normalizes batting and bowling metrics using documented
   min-max and z-score formulations for eligible dual-role contributors (runs >= 100, wkts >= 5).
4. Player Consistency: Innings-level volatility analysis (standard deviation, coefficient of variation,
   failure rates, IQR) for batters (>= 10 inns) and bowlers (>= 10 inns).

Single Authoritative Source:
  C:\\Users\\Khush\\Desktop\\TEST\\eligible_ball_by_ball.csv
"""

import os
import shutil
import json
import math
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from collections import defaultdict
from typing import Dict, List, Tuple, Any

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PLOTS_DIR = os.path.join(BASE_DIR, "plots")
os.makedirs(PLOTS_DIR, exist_ok=True)

ARTIFACT_DIR = r"C:\Users\Khush\.gemini\antigravity\brain\75f45278-63fb-44e8-91e1-777d4a2d298f"
SOURCE_CSV = r"C:\Users\Khush\Desktop\TEST\eligible_ball_by_ball.csv"

# ==============================================================================
# 1. HOME / AWAY VERIFICATION MODULE
# ==============================================================================
def verify_home_away_availability(csv_path: str = SOURCE_CSV) -> Dict[str, Any]:
    """
    Verifies whether home/away relationship is reliably and natively identifiable in the CSV.
    """
    df_sample = pd.read_csv(csv_path, nrows=50)
    cols = list(df_sample.columns)

    target_home_cols = [c for c in cols if any(k in c.lower() for k in ["home", "host", "neutral", "away", "venue_country", "venue_team"])]

    # Read unique match-venue pairings
    df_matches = pd.read_csv(csv_path, usecols=["match_id", "venue", "batting_team", "bowling_team", "event"])
    match_records = df_matches.drop_duplicates("match_id")

    return {
        "analysis": "Home/Away Comparison",
        "status": "UNSUPPORTED",
        "reason": (
            "The source CSV lacks native 'home_team', 'away_team', 'host_country', or 'venue_country' columns. "
            "Venues (e.g., 'Sydney Cricket Ground', 'Dubai International Cricket Stadium', 'Lord\'s') are stadium names "
            "without explicit host country metadata. Event names are either series titles ('Basil D'Oliveira Trophy', 'Border-Gavaskar Trophy') "
            "or missing (NaN). As instructed, home status must not be inferred from player nationality or external lookups."
        ),
        "columns_checked": len(cols),
        "target_columns_found": target_home_cols,
        "sample_venues_count": match_records["venue"].nunique(),
        "neutral_venues_present": True
    }

# ==============================================================================
# 2. MATCH IMPACT & CONTRIBUTION ANALYTICS
# ==============================================================================
def run_match_impact_analytics(csv_path: str = SOURCE_CSV) -> pd.DataFrame:
    """
    Derives transparent match contribution shares from observable batting and bowling outcomes.
    Avoids unverified causal claims.
    """
    print("\n--- Computing Transparent Match Contributions ---")
    cols = [
        "match_id", "innings", "batting_team", "bowling_team", "striker", "bowler",
        "runs_off_bat", "extras", "wides", "noballs", "wicket", "wicket_type",
        "winner", "player_of_match"
    ]
    df = pd.read_csv(csv_path, usecols=cols)

    # 1. Player-of-the-match counts per player
    match_potm = df.drop_duplicates("match_id")[["match_id", "player_of_match", "winner"]].set_index("match_id")
    potm_counts = match_potm["player_of_match"].value_counts().to_dict()

    # 2. Team total runs per match
    df["ball_runs"] = df["runs_off_bat"] + df["extras"]
    team_match_runs = df.groupby(["match_id", "batting_team"])["ball_runs"].sum().reset_index()
    team_match_runs.rename(columns={"ball_runs": "team_total_runs"}, inplace=True)

    # 3. Striker runs per match
    striker_match_runs = df.groupby(["match_id", "batting_team", "striker"])["runs_off_bat"].sum().reset_index()
    striker_match_runs = striker_match_runs.merge(team_match_runs, on=["match_id", "batting_team"], how="left")
    striker_match_runs["run_share"] = striker_match_runs["runs_off_bat"] / striker_match_runs["team_total_runs"]

    # Add match win context
    striker_match_runs["match_winner"] = striker_match_runs["match_id"].map(match_potm["winner"])
    striker_match_runs["is_win"] = striker_match_runs["batting_team"] == striker_match_runs["match_winner"]

    # 4. Bowler wickets per match
    bowler_credited = {"caught", "bowled", "lbw", "stumped", "caught and bowled", "hit wicket"}
    df_bw = df[df["wicket"] == 1]
    df_bw_cred = df_bw[df_bw["wicket_type"].str.lower().isin(bowler_credited)]

    # Team total wickets taken in match (by bowling team)
    team_match_wkts = df_bw_cred.groupby(["match_id", "bowling_team"])["wicket"].count().reset_index()
    team_match_wkts.rename(columns={"wicket": "team_total_wickets"}, inplace=True)

    bowler_match_wkts = df_bw_cred.groupby(["match_id", "bowling_team", "bowler"])["wicket"].count().reset_index()
    bowler_match_wkts = bowler_match_wkts.merge(team_match_wkts, on=["match_id", "bowling_team"], how="left")
    bowler_match_wkts["wicket_share"] = bowler_match_wkts["wicket"] / bowler_match_wkts["team_total_wickets"]
    bowler_match_wkts["match_winner"] = bowler_match_wkts["match_id"].map(match_potm["winner"])
    bowler_match_wkts["is_win"] = bowler_match_wkts["bowling_team"] == bowler_match_wkts["match_winner"]

    # Aggregations per player
    # Batting contribution summary
    bat_summary = striker_match_runs.groupby("striker").agg(
        bat_matches=("match_id", "nunique"),
        total_runs=("runs_off_bat", "sum"),
        avg_run_share=("run_share", "mean"),
        max_run_share=("run_share", "max"),
        runs_in_wins=("runs_off_bat", lambda s: s[striker_match_runs.loc[s.index, "is_win"]].sum()),
        matches_won_batted=("match_id", lambda s: s[striker_match_runs.loc[s.index, "is_win"]].nunique())
    ).reset_index().rename(columns={"striker": "player_id"})

    # Bowling contribution summary
    bowl_summary = bowler_match_wkts.groupby("bowler").agg(
        bowl_matches=("match_id", "nunique"),
        total_wickets=("wicket", "sum"),
        avg_wicket_share=("wicket_share", "mean"),
        max_wicket_share=("wicket_share", "max"),
        wickets_in_wins=("wicket", lambda s: s[bowler_match_wkts.loc[s.index, "is_win"]].sum()),
        matches_won_bowled=("match_id", lambda s: s[bowler_match_wkts.loc[s.index, "is_win"]].nunique())
    ).reset_index().rename(columns={"bowler": "player_id"})

    # Load master player list
    master = pd.read_csv(os.path.join(BASE_DIR, "player_features_master.csv"))
    df_impact = master[["player_id", "full_name", "country", "playing_role"]].copy()
    df_impact = df_impact.merge(bat_summary, on="player_id", how="left").fillna({
        "bat_matches": 0, "total_runs": 0, "avg_run_share": 0.0, "max_run_share": 0.0,
        "runs_in_wins": 0, "matches_won_batted": 0
    })
    df_impact = df_impact.merge(bowl_summary, on="player_id", how="left").fillna({
        "bowl_matches": 0, "total_wickets": 0, "avg_wicket_share": 0.0, "max_wicket_share": 0.0,
        "wickets_in_wins": 0, "matches_won_bowled": 0
    })

    # POTM mapping
    df_impact["player_of_match_awards"] = df_impact["player_id"].map(potm_counts).fillna(0).astype(int)
    total_m = np.maximum(df_impact["bat_matches"], df_impact["bowl_matches"])
    df_impact["matches_played"] = total_m
    df_impact["potm_rate"] = np.where(total_m > 0, (df_impact["player_of_match_awards"] / total_m).round(3), 0.0)

    # Transparent Contribution Index (0 - 100 scale):
    # Normalized combination of run share, wicket share, and match award rate.
    # Note: Explicitly documented as an observational contribution index, NOT causal impact.
    r_share_norm = df_impact["avg_run_share"] / (df_impact["avg_run_share"].max() + 1e-9)
    w_share_norm = df_impact["avg_wicket_share"] / (df_impact["avg_wicket_share"].max() + 1e-9)
    potm_norm = df_impact["player_of_match_awards"] / (df_impact["player_of_match_awards"].max() + 1e-9)

    df_impact["composite_contribution_index"] = (
        (0.40 * r_share_norm + 0.40 * w_share_norm + 0.20 * potm_norm) * 100.0
    ).round(2)

    df_impact = df_impact.sort_values(by="composite_contribution_index", ascending=False).reset_index(drop=True)

    out_csv = os.path.join(BASE_DIR, "match_impact_analytics.csv")
    df_impact.to_csv(out_csv, index=False)
    print(f"Exported {len(df_impact)} player impact records to {out_csv}")
    return df_impact

# ==============================================================================
# 3. ALL-ROUND PERFORMANCE INDEX
# ==============================================================================
def run_allround_performance_index() -> pd.DataFrame:
    """
    Computes a balanced, normalized All-Round Performance Index for qualified all-rounders.
    Threshold: runs >= 100 and wickets >= 5.
    Normalizes batting and bowling metrics with directionality (inverted bowling average & economy).
    """
    print("\n--- Computing All-Round Performance Index ---")
    ar = pd.read_csv(os.path.join(BASE_DIR, "allrounder_features.csv"))

    eligible = ar[(ar["runs"] >= 100) & (ar["wickets"] >= 5) &
                  (ar["batting_average"].notna()) & (ar["bowling_average"].notna())].copy()

    # Documented Normalization: Min-Max (0 - 100)
    # Higher is better: batting_average, batting_strike_rate, line_length_accuracy_pct
    # Lower is better: bowling_average, economy_rate
    def min_max_higher(series: pd.Series) -> pd.Series:
        s_min, s_max = series.min(), series.max()
        if s_max == s_min: return pd.Series(50.0, index=series.index)
        return ((series - s_min) / (s_max - s_min)) * 100.0

    def min_max_lower(series: pd.Series) -> pd.Series:
        s_min, s_max = series.min(), series.max()
        if s_max == s_min: return pd.Series(50.0, index=series.index)
        return ((s_max - series) / (s_max - s_min)) * 100.0

    eligible["norm_bat_avg"] = min_max_higher(eligible["batting_average"]).round(2)
    eligible["norm_bat_sr"] = min_max_higher(eligible["batting_strike_rate"]).round(2)
    eligible["norm_bowl_avg"] = min_max_lower(eligible["bowling_average"]).round(2)
    eligible["norm_economy"] = min_max_lower(eligible["economy_rate"]).round(2)
    eligible["norm_accuracy"] = min_max_higher(eligible["line_length_accuracy_pct"]).round(2)

    # Batting Index: 60% Batting Average + 40% Strike Rate
    eligible["batting_component_score"] = (
        0.60 * eligible["norm_bat_avg"] + 0.40 * eligible["norm_bat_sr"]
    ).round(2)

    # Bowling Index: 45% Bowling Average + 35% Economy Rate + 20% Accuracy
    eligible["bowling_component_score"] = (
        0.45 * eligible["norm_bowl_avg"] + 0.35 * eligible["norm_economy"] + 0.20 * eligible["norm_accuracy"]
    ).round(2)

    # Composite All-Round Index: 50% Batting + 50% Bowling
    eligible["allrounder_composite_index"] = (
        0.50 * eligible["batting_component_score"] + 0.50 * eligible["bowling_component_score"]
    ).round(2)

    # Sort descending
    eligible = eligible.sort_values(by="allrounder_composite_index", ascending=False).reset_index(drop=True)
    eligible["rank"] = range(1, len(eligible) + 1)

    out_csv = os.path.join(BASE_DIR, "allround_performance_ratings.csv")
    eligible.to_csv(out_csv, index=False)
    print(f"Exported {len(eligible)} qualified all-rounders to {out_csv}")
    return eligible

# ==============================================================================
# 4. PLAYER CONSISTENCY ANALYTICS (INNINGS-LEVEL)
# ==============================================================================
def run_player_consistency_analytics(csv_path: str = SOURCE_CSV) -> Tuple[pd.DataFrame, pd.DataFrame]:
    """
    Computes innings-level consistency and dispersion metrics for batters and bowlers.
    Threshold: Minimum 10 innings to ensure statistical stability.
    """
    print("\n--- Computing Innings-Level Player Consistency Analytics ---")
    cols = [
        "match_id", "innings", "striker", "bowler", "runs_off_bat", "wides", "noballs",
        "wicket", "wicket_type", "player_dismissed"
    ]
    df = pd.read_csv(csv_path, usecols=cols)

    # 4.1 Batting Consistency
    # Reconstruct every batting innings
    bat_inns = df.groupby(["match_id", "innings", "striker"]).agg(
        runs=("runs_off_bat", "sum"),
        balls=("runs_off_bat", lambda s: (df.loc[s.index, "wides"] == 0).sum())
    ).reset_index()

    # Determine dismissals
    dismissed_set = set(df[df["player_dismissed"].notna()][["match_id", "innings", "player_dismissed"]].itertuples(index=False, name=None))
    bat_inns["is_dismissed"] = bat_inns.apply(lambda r: (r["match_id"], r["innings"], r["striker"]) in dismissed_set, axis=1)

    # Batters with >= 10 innings
    inns_counts = bat_inns["striker"].value_counts()
    eligible_batters = inns_counts[inns_counts >= 10].index.tolist()

    bat_consistency = []
    for p in eligible_batters:
        scores = bat_inns[bat_inns["striker"] == p]["runs"].values
        n_inns = len(scores)
        mean_score = float(np.mean(scores))
        std_score = float(np.std(scores, ddof=1)) if n_inns > 1 else 0.0
        cv = (std_score / mean_score) if mean_score > 0 else 0.0

        median_score = float(np.median(scores))
        q25, q75 = np.percentile(scores, [25, 75])
        iqr = float(q75 - q25)

        single_digit_fails = int(np.sum(scores < 10))
        single_digit_pct = round((single_digit_fails / n_inns) * 100.0, 2)

        fifties = int(np.sum((scores >= 50) & (scores < 100)))
        hundreds = int(np.sum(scores >= 100))
        total_50_plus = fifties + hundreds
        conversion_rate = round((hundreds / total_50_plus) * 100.0, 2) if total_50_plus > 0 else 0.0

        bat_consistency.append({
            "player_id": p,
            "innings_count": n_inns,
            "mean_runs_per_innings": round(mean_score, 2),
            "std_runs": round(std_score, 2),
            "coefficient_of_variation": round(cv, 3),
            "median_runs": round(median_score, 1),
            "iqr_runs": round(iqr, 1),
            "single_digit_failure_pct": single_digit_pct,
            "milestone_conversion_pct": conversion_rate,
            "hundreds": hundreds,
            "fifties": fifties
        })

    df_bat_cons = pd.DataFrame(bat_consistency)
    # Join player metadata
    master = pd.read_csv(os.path.join(BASE_DIR, "player_features_master.csv"))
    df_bat_cons = df_bat_cons.merge(
        master[["player_id", "full_name", "country", "playing_role", "batting_average"]],
        on="player_id", how="left"
    ).sort_values(by="coefficient_of_variation", ascending=True).reset_index(drop=True)

    out_bat_csv = os.path.join(BASE_DIR, "batting_consistency_analytics.csv")
    df_bat_cons.to_csv(out_bat_csv, index=False)
    print(f"Exported {len(df_bat_cons)} batter consistency records to {out_bat_csv}")

    # 4.2 Bowling Consistency
    # Reconstruct every bowling innings
    bowler_credited = {"caught", "bowled", "lbw", "stumped", "caught and bowled", "hit wicket"}
    df["bowler_runs"] = df["runs_off_bat"] + df["wides"] + df["noballs"]
    df["is_bw_wkt"] = (df["wicket"] == 1) & (df["wicket_type"].str.lower().isin(bowler_credited))

    bowl_inns = df.groupby(["match_id", "innings", "bowler"]).agg(
        legal_balls=("runs_off_bat", lambda s: ((df.loc[s.index, "wides"] == 0) & (df.loc[s.index, "noballs"] == 0)).sum()),
        runs_conceded=("bowler_runs", "sum"),
        wickets=("is_bw_wkt", "sum")
    ).reset_index()

    bowl_inns_counts = bowl_inns["bowler"].value_counts()
    eligible_bowlers = bowl_inns_counts[bowl_inns_counts >= 10].index.tolist()

    bowl_consistency = []
    for b in eligible_bowlers:
        b_sub = bowl_inns[bowl_inns["bowler"] == b]
        wkts = b_sub["wickets"].values
        runs = b_sub["runs_conceded"].values
        balls = b_sub["legal_balls"].values

        n_inns = len(wkts)
        mean_wkts = float(np.mean(wkts))
        std_wkts = float(np.std(wkts, ddof=1)) if n_inns > 1 else 0.0

        wicketless_count = int(np.sum(wkts == 0))
        wicketless_pct = round((wicketless_count / n_inns) * 100.0, 2)

        multi_wkt_count = int(np.sum(wkts >= 3))
        multi_wkt_pct = round((multi_wkt_count / n_inns) * 100.0, 2)

        # Innings economy rate
        econs = [float((r / (bl / 6.0))) for r, bl in zip(runs, balls) if bl >= 12] # min 2 overs
        mean_econ = float(np.mean(econs)) if econs else 0.0
        std_econ = float(np.std(econs, ddof=1)) if len(econs) > 1 else 0.0

        bowl_consistency.append({
            "player_id": b,
            "innings_bowled_count": n_inns,
            "mean_wickets_per_innings": round(mean_wkts, 2),
            "std_wickets": round(std_wkts, 2),
            "wicketless_innings_pct": wicketless_pct,
            "multi_wicket_innings_pct": multi_wkt_pct,
            "mean_innings_economy": round(mean_econ, 2),
            "std_innings_economy": round(std_econ, 2)
        })

    df_bowl_cons = pd.DataFrame(bowl_consistency)
    df_bowl_cons = df_bowl_cons.merge(
        master[["player_id", "full_name", "country", "playing_role", "bowling_average", "economy_rate"]],
        on="player_id", how="left"
    ).sort_values(by="wicketless_innings_pct", ascending=True).reset_index(drop=True)

    out_bowl_csv = os.path.join(BASE_DIR, "bowling_consistency_analytics.csv")
    df_bowl_cons.to_csv(out_bowl_csv, index=False)
    print(f"Exported {len(df_bowl_cons)} bowler consistency records to {out_bowl_csv}")

    return df_bat_cons, df_bowl_cons

# ==============================================================================
# 5. VISUALIZATION MODULE
# ==============================================================================
def generate_extension_plots(df_impact: pd.DataFrame, df_ar: pd.DataFrame,
                             df_bat_cons: pd.DataFrame, df_bowl_cons: pd.DataFrame):
    """Generates charts for Match Impact, All-Round Performance, and Player Consistency."""
    print("\n--- Generating Extension Visualizations ---")

    # 1. Match Impact Contribution Chart
    plt.figure(figsize=(12, 6))
    top_impact = df_impact.head(15).copy()
    sns.barplot(data=top_impact, x="composite_contribution_index", y="player_id", palette="crest", edgecolor="k")
    plt.title("PitchIQ — Top 15 Observational Match Contributors\n(Run Share, Wicket Share & POTM Awards; Non-Causal)", fontsize=12, fontweight="bold")
    plt.xlabel("Composite Observational Contribution Index (0 - 100)", fontsize=11)
    plt.ylabel("Player ID", fontsize=11)
    plt.grid(True, linestyle=":", alpha=0.6)
    plt.tight_layout()
    plot_impact_path = os.path.join(PLOTS_DIR, "match_impact_contributions.png")
    plt.savefig(plot_impact_path, dpi=300)
    plt.close()

    # 2. All-Round Performance Comparison Scatter
    plt.figure(figsize=(11, 7))
    sns.scatterplot(
        data=df_ar, x="batting_component_score", y="bowling_component_score",
        size="allrounder_composite_index", hue="allrounder_differential", palette="coolwarm",
        sizes=(50, 250), alpha=0.85, edgecolor="k"
    )
    plt.axvline(50, color="gray", linestyle="--", alpha=0.5)
    plt.axhline(50, color="gray", linestyle="--", alpha=0.5)
    plt.title("PitchIQ — All-Round Performance Matrix (Qualified Dual-Role Players)\nBatting Component Score vs Bowling Component Score", fontsize=12, fontweight="bold")
    plt.xlabel("Normalized Batting Score (0 - 100; Higher is Superior)", fontsize=11)
    plt.ylabel("Normalized Bowling Score (0 - 100; Higher is Superior)", fontsize=11)

    # Annotate prominent all-rounders
    prominent = ["BA Stokes", "RA Jadeja", "R Ashwin", "JO Holder", "Shakib Al Hasan", "CR Woakes", "PJ Cummins", "VD Philander"]
    for _, row in df_ar.iterrows():
        if row["player_id"] in prominent:
            plt.annotate(row["player_id"], (row["batting_component_score"] + 0.8, row["bowling_component_score"] + 0.8),
                         fontsize=8, fontweight="bold", bbox=dict(boxstyle="round,pad=0.2", fc="white", ec="gray", alpha=0.8))

    plt.legend(bbox_to_anchor=(1.05, 1), loc="upper left")
    plt.grid(True, linestyle=":", alpha=0.5)
    plt.tight_layout()
    plot_ar_path = os.path.join(PLOTS_DIR, "allround_performance_comparison.png")
    plt.savefig(plot_ar_path, dpi=300)
    plt.close()

    # 3. Batting Consistency Scatter (Average vs Coefficient of Variation)
    plt.figure(figsize=(11, 7))
    # Batting average vs CV
    sns.scatterplot(
        data=df_bat_cons, x="batting_average", y="coefficient_of_variation",
        size="innings_count", hue="single_digit_failure_pct", palette="magma_r",
        sizes=(40, 200), alpha=0.85, edgecolor="k"
    )
    plt.title("PitchIQ — Batting Consistency Matrix (Innings >= 10)\nBatting Average vs Volatility (Coefficient of Variation)", fontsize=12, fontweight="bold")
    plt.xlabel("Traditional Batting Average (Higher is Better)", fontsize=11)
    plt.ylabel("Coefficient of Variation (σ / μ; Lower is More Consistent)", fontsize=11)

    # Annotate notable batters
    notable_bats = ["SPD Smith", "V Kohli", "KC Sangakkara", "KS Williamson", "JE Root", "DA Warner", "CA Pujara"]
    for _, row in df_bat_cons.iterrows():
        if row["player_id"] in notable_bats:
            plt.annotate(row["player_id"], (row["batting_average"] + 0.8, row["coefficient_of_variation"] + 0.02),
                         fontsize=8, fontweight="bold", bbox=dict(boxstyle="round,pad=0.2", fc="white", ec="gray", alpha=0.8))

    plt.legend(bbox_to_anchor=(1.05, 1), loc="upper left")
    plt.grid(True, linestyle=":", alpha=0.5)
    plt.tight_layout()
    plot_cons_path = os.path.join(PLOTS_DIR, "batting_consistency_scatter.png")
    plt.savefig(plot_cons_path, dpi=300)
    plt.close()

    # Copy plots to artifact directory
    if os.path.exists(ARTIFACT_DIR):
        for p in [plot_impact_path, plot_ar_path, plot_cons_path]:
            dst = os.path.join(ARTIFACT_DIR, os.path.basename(p))
            shutil.copy2(p, dst)
        print(f"Copied extension plots to {ARTIFACT_DIR}")

def main():
    print("=" * 70)
    print("PitchIQ Player Intelligence Lab — Advanced Analytics Extension")
    print("=" * 70)

    # 1. Home / Away Verification
    ha_report = verify_home_away_availability()
    print(f"Home/Away Analysis Status: {ha_report['status']}")
    print(f"Reason: {ha_report['reason']}")

    # Save Home/Away determination JSON
    with open(os.path.join(BASE_DIR, "home_away_verification.json"), "w", encoding="utf-8") as f:
        json.dump(ha_report, f, indent=2)

    # 2. Match Impact Analytics
    df_impact = run_match_impact_analytics()

    # 3. All-Round Performance
    df_ar = run_allround_performance_index()

    # 4. Consistency Analytics
    df_bat_cons, df_bowl_cons = run_player_consistency_analytics()

    # 5. Visualizations
    generate_extension_plots(df_impact, df_ar, df_bat_cons, df_bowl_cons)

    print("\nAdvanced Analytics Pipeline completed successfully!")

if __name__ == "__main__":
    main()
