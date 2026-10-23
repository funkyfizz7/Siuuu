"""
PitchIQ Player Intelligence Lab — Multi-Dimensional Player Rankings & Sensitivity Module

Authoritative Single Source:
  C:\\Users\\Khush\\Desktop\\TEST\\eligible_ball_by_ball.csv

Rankings Generated:
1. Best-Performing Batters (Innings >= 15, Balls Faced >= 500)
2. Best-Performing Bowlers (Legal Balls >= 600, Wickets >= 15)
3. All-Rounder Rankings (Runs >= 200, Wickets >= 10)
4. Match-Contribution Rankings (Matches >= 5)
5. Overall Player Rankings (Matches >= 10)

Key Features:
- Documented, transparent directional min-max normalization.
- Sensitivity analysis across reasonable alternative weighting schemes (Spearman rank correlation).
- Complete display of player metadata, underlying statistics, component scores, and final scores.
- Non-definitive terminology: ranks top player within eligible dataset under stated method.
"""

import os
import shutil
import json
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from scipy.stats import spearmanr
from typing import Dict, List, Tuple, Any

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PLOTS_DIR = os.path.join(BASE_DIR, "plots")
os.makedirs(PLOTS_DIR, exist_ok=True)

ARTIFACT_DIR = r"C:\Users\Khush\.gemini\antigravity\brain\75f45278-63fb-44e8-91e1-777d4a2d298f"

def min_max_higher(series: pd.Series) -> pd.Series:
    """Min-max scaling where higher is better (0 to 100)."""
    s_min, s_max = series.min(), series.max()
    if s_max == s_min: return pd.Series(50.0, index=series.index)
    return ((series - s_min) / (s_max - s_min)) * 100.0

def min_max_lower(series: pd.Series) -> pd.Series:
    """Min-max scaling where lower is better (0 to 100)."""
    s_min, s_max = series.min(), series.max()
    if s_max == s_min: return pd.Series(50.0, index=series.index)
    return ((s_max - series) / (s_max - s_min)) * 100.0

# ==============================================================================
# 1. BATTER RANKING PIPELINE
# ==============================================================================
def run_batting_rankings() -> Tuple[pd.DataFrame, Dict[str, Any]]:
    print("\n--- Generating Batting Rankings ---")
    bat = pd.read_csv(os.path.join(BASE_DIR, "batting_features.csv"))

    # Eligibility Threshold: Innings >= 15 and Balls Faced >= 500
    eligible = bat[(bat["innings"] >= 15) & (bat["balls_faced"] >= 500) & (bat["batting_average"].notna())].copy()

    # Feature Engineering
    eligible["runs_per_innings"] = (eligible["runs"] / eligible["innings"]).round(2)
    eligible["milestone_rate"] = (((eligible["hundreds"] * 2.0) + eligible["fifties"]) / eligible["innings"]).round(3)

    # Normalization (0 - 100)
    eligible["norm_avg"] = min_max_higher(eligible["batting_average"]).round(2)
    eligible["norm_sr"] = min_max_higher(eligible["strike_rate"]).round(2)
    eligible["norm_rpi"] = min_max_higher(eligible["runs_per_innings"]).round(2)
    eligible["norm_milestone"] = min_max_higher(eligible["milestone_rate"]).round(2)
    eligible["norm_boundary"] = min_max_higher(eligible["boundary_run_pct"]).round(2)

    # Baseline Weights: Avg (40%), Strike Rate (20%), Runs/Inn (20%), Milestones (10%), Boundary % (10%)
    eligible["baseline_score"] = (
        0.40 * eligible["norm_avg"] +
        0.20 * eligible["norm_sr"] +
        0.20 * eligible["norm_rpi"] +
        0.10 * eligible["norm_milestone"] +
        0.10 * eligible["norm_boundary"]
    ).round(2)

    # Alternative 1: Efficiency-Heavy (Avg 55%, SR 25%, RPI 10%, Milestones 10%)
    eligible["alt_efficiency_score"] = (
        0.55 * eligible["norm_avg"] +
        0.25 * eligible["norm_sr"] +
        0.10 * eligible["norm_rpi"] +
        0.10 * eligible["norm_milestone"]
    ).round(2)

    # Alternative 2: Volume & Milestone Dominance (RPI 35%, Milestones 25%, Avg 25%, SR 10%, Boundary 5%)
    eligible["alt_volume_score"] = (
        0.25 * eligible["norm_avg"] +
        0.10 * eligible["norm_sr"] +
        0.35 * eligible["norm_rpi"] +
        0.25 * eligible["norm_milestone"] +
        0.05 * eligible["norm_boundary"]
    ).round(2)

    # Sort by baseline
    eligible = eligible.sort_values(by="baseline_score", ascending=False).reset_index(drop=True)
    eligible["baseline_rank"] = range(1, len(eligible) + 1)

    eligible["alt_efficiency_rank"] = eligible["alt_efficiency_score"].rank(ascending=False, method="min").astype(int)
    eligible["alt_volume_rank"] = eligible["alt_volume_score"].rank(ascending=False, method="min").astype(int)

    # Sensitivity
    rho_eff, _ = spearmanr(eligible["baseline_rank"], eligible["alt_efficiency_rank"])
    rho_vol, _ = spearmanr(eligible["baseline_rank"], eligible["alt_volume_rank"])

    sensitivity = {
        "ranking": "Best-Performing Batters",
        "population": len(eligible),
        "threshold": "innings >= 15 and balls_faced >= 500",
        "spearman_efficiency": round(rho_eff, 4),
        "spearman_volume": round(rho_vol, 4),
        "top_3_baseline": eligible.iloc[:3]["player_id"].tolist(),
        "top_3_efficiency": eligible.sort_values("alt_efficiency_rank").iloc[:3]["player_id"].tolist(),
        "top_3_volume": eligible.sort_values("alt_volume_rank").iloc[:3]["player_id"].tolist()
    }

    out_csv = os.path.join(BASE_DIR, "ranking_batters.csv")
    eligible.to_csv(out_csv, index=False)
    print(f"Exported {len(eligible)} ranked batters to {out_csv}")
    return eligible, sensitivity

# ==============================================================================
# 2. BOWLER RANKING PIPELINE
# ==============================================================================
def run_bowling_rankings() -> Tuple[pd.DataFrame, Dict[str, Any]]:
    print("\n--- Generating Bowling Rankings ---")
    bowl = pd.read_csv(os.path.join(BASE_DIR, "bowling_features.csv"))

    # Eligibility Threshold: Legal balls >= 600 (100 overs) and Wickets >= 15
    eligible = bowl[(bowl["legal_balls_bowled"] >= 600) & (bowl["wickets"] >= 15) & (bowl["bowling_average"].notna())].copy()

    # Normalization (0 - 100)
    eligible["norm_avg"] = min_max_lower(eligible["bowling_average"]).round(2)
    eligible["norm_econ"] = min_max_lower(eligible["economy_rate"]).round(2)
    eligible["norm_sr"] = min_max_lower(eligible["bowling_strike_rate"]).round(2)
    eligible["norm_dots"] = min_max_higher(eligible["bowling_dot_pct"]).round(2)
    eligible["norm_acc"] = min_max_higher(eligible["line_length_accuracy_pct"]).round(2)

    # Baseline Weights: Avg (35%), Econ (25%), Strike Rate (20%), Dot % (10%), Accuracy (10%)
    eligible["baseline_score"] = (
        0.35 * eligible["norm_avg"] +
        0.25 * eligible["norm_econ"] +
        0.20 * eligible["norm_sr"] +
        0.10 * eligible["norm_dots"] +
        0.10 * eligible["norm_acc"]
    ).round(2)

    # Alternative 1: Strike/Potency-Heavy (SR 40%, Avg 35%, Econ 15%, Dots 5%, Acc 5%)
    eligible["alt_strike_score"] = (
        0.35 * eligible["norm_avg"] +
        0.15 * eligible["norm_econ"] +
        0.40 * eligible["norm_sr"] +
        0.05 * eligible["norm_dots"] +
        0.05 * eligible["norm_acc"]
    ).round(2)

    # Alternative 2: Economy/Control-Heavy (Econ 40%, Acc 25%, Dots 15%, Avg 15%, SR 5%)
    eligible["alt_control_score"] = (
        0.15 * eligible["norm_avg"] +
        0.40 * eligible["norm_econ"] +
        0.05 * eligible["norm_sr"] +
        0.15 * eligible["norm_dots"] +
        0.25 * eligible["norm_acc"]
    ).round(2)

    # Sort
    eligible = eligible.sort_values(by="baseline_score", ascending=False).reset_index(drop=True)
    eligible["baseline_rank"] = range(1, len(eligible) + 1)
    eligible["alt_strike_rank"] = eligible["alt_strike_score"].rank(ascending=False, method="min").astype(int)
    eligible["alt_control_rank"] = eligible["alt_control_score"].rank(ascending=False, method="min").astype(int)

    rho_strike, _ = spearmanr(eligible["baseline_rank"], eligible["alt_strike_rank"])
    rho_control, _ = spearmanr(eligible["baseline_rank"], eligible["alt_control_rank"])

    sensitivity = {
        "ranking": "Best-Performing Bowlers",
        "population": len(eligible),
        "threshold": "legal_balls_bowled >= 600 and wickets >= 15",
        "spearman_strike": round(rho_strike, 4),
        "spearman_control": round(rho_control, 4),
        "top_3_baseline": eligible.iloc[:3]["player_id"].tolist(),
        "top_3_strike": eligible.sort_values("alt_strike_rank").iloc[:3]["player_id"].tolist(),
        "top_3_control": eligible.sort_values("alt_control_rank").iloc[:3]["player_id"].tolist()
    }

    out_csv = os.path.join(BASE_DIR, "ranking_bowlers.csv")
    eligible.to_csv(out_csv, index=False)
    print(f"Exported {len(eligible)} ranked bowlers to {out_csv}")
    return eligible, sensitivity

# ==============================================================================
# 3. ALL-ROUNDER RANKING PIPELINE
# ==============================================================================
def run_allrounder_rankings() -> Tuple[pd.DataFrame, Dict[str, Any]]:
    print("\n--- Generating All-Rounder Rankings ---")
    ar = pd.read_csv(os.path.join(BASE_DIR, "allrounder_features.csv"))

    # Eligibility Threshold: Runs >= 200 and Wickets >= 10
    eligible = ar[(ar["runs"] >= 200) & (ar["wickets"] >= 10) &
                  (ar["batting_average"].notna()) & (ar["bowling_average"].notna())].copy()

    # Normalized component scores
    eligible["norm_bat_avg"] = min_max_higher(eligible["batting_average"]).round(2)
    eligible["norm_bat_sr"] = min_max_higher(eligible["batting_strike_rate"]).round(2)
    eligible["norm_bowl_avg"] = min_max_lower(eligible["bowling_average"]).round(2)
    eligible["norm_econ"] = min_max_lower(eligible["economy_rate"]).round(2)
    eligible["norm_diff"] = min_max_higher(eligible["allrounder_differential"]).round(2)

    # Component summaries
    eligible["batting_component"] = (0.65 * eligible["norm_bat_avg"] + 0.35 * eligible["norm_bat_sr"]).round(2)
    eligible["bowling_component"] = (0.55 * eligible["norm_bowl_avg"] + 0.45 * eligible["norm_econ"]).round(2)

    # Baseline: Balanced (Batting 45%, Bowling 45%, Differential 10%)
    eligible["baseline_score"] = (
        0.45 * eligible["batting_component"] +
        0.45 * eligible["bowling_component"] +
        0.10 * eligible["norm_diff"]
    ).round(2)

    # Alternative 1: Batting-Biased All-Rounder (Batting 60%, Bowling 30%, Diff 10%)
    eligible["alt_bat_biased_score"] = (
        0.60 * eligible["batting_component"] +
        0.30 * eligible["bowling_component"] +
        0.10 * eligible["norm_diff"]
    ).round(2)

    # Alternative 2: Bowling-Biased All-Rounder (Bowling 60%, Batting 30%, Diff 10%)
    eligible["alt_bowl_biased_score"] = (
        0.30 * eligible["batting_component"] +
        0.60 * eligible["bowling_component"] +
        0.10 * eligible["norm_diff"]
    ).round(2)

    eligible = eligible.sort_values(by="baseline_score", ascending=False).reset_index(drop=True)
    eligible["baseline_rank"] = range(1, len(eligible) + 1)
    eligible["alt_bat_rank"] = eligible["alt_bat_biased_score"].rank(ascending=False, method="min").astype(int)
    eligible["alt_bowl_rank"] = eligible["alt_bowl_biased_score"].rank(ascending=False, method="min").astype(int)

    rho_bat, _ = spearmanr(eligible["baseline_rank"], eligible["alt_bat_rank"])
    rho_bowl, _ = spearmanr(eligible["baseline_rank"], eligible["alt_bowl_rank"])

    sensitivity = {
        "ranking": "All-Rounder Rankings",
        "population": len(eligible),
        "threshold": "runs >= 200 and wickets >= 10",
        "spearman_bat_biased": round(rho_bat, 4),
        "spearman_bowl_biased": round(rho_bowl, 4),
        "top_3_baseline": eligible.iloc[:3]["player_id"].tolist(),
        "top_3_bat_biased": eligible.sort_values("alt_bat_rank").iloc[:3]["player_id"].tolist(),
        "top_3_bowl_biased": eligible.sort_values("alt_bowl_rank").iloc[:3]["player_id"].tolist()
    }

    out_csv = os.path.join(BASE_DIR, "ranking_allrounders.csv")
    eligible.to_csv(out_csv, index=False)
    print(f"Exported {len(eligible)} ranked all-rounders to {out_csv}")
    return eligible, sensitivity

# ==============================================================================
# 4. MATCH-CONTRIBUTION RANKING PIPELINE
# ==============================================================================
def run_match_contribution_rankings() -> Tuple[pd.DataFrame, Dict[str, Any]]:
    print("\n--- Generating Match-Contribution Rankings ---")
    impact = pd.read_csv(os.path.join(BASE_DIR, "match_impact_analytics.csv"))

    # Threshold: Matches played >= 5
    eligible = impact[impact["matches_played"] >= 5].copy()

    # Normalization
    eligible["norm_run_share"] = min_max_higher(eligible["avg_run_share"]).round(2)
    eligible["norm_wkt_share"] = min_max_higher(eligible["avg_wicket_share"]).round(2)
    eligible["norm_potm_rate"] = min_max_higher(eligible["potm_rate"]).round(2)

    # Win-Context Metric
    eligible["win_contribution_volume"] = eligible["runs_in_wins"] + (eligible["wickets_in_wins"] * 20.0)
    eligible["norm_win_volume"] = min_max_higher(eligible["win_contribution_volume"]).round(2)

    # Baseline Weights: Run Share (35%), Wicket Share (35%), POTM Rate (20%), Win Volume (10%)
    eligible["baseline_score"] = (
        0.35 * eligible["norm_run_share"] +
        0.35 * eligible["norm_wkt_share"] +
        0.20 * eligible["norm_potm_rate"] +
        0.10 * eligible["norm_win_volume"]
    ).round(2)

    # Alternative 1: Award/Impact-Heavy (POTM Rate 45%, Run Share 25%, Wkt Share 25%, Win Vol 5%)
    eligible["alt_potm_heavy_score"] = (
        0.25 * eligible["norm_run_share"] +
        0.25 * eligible["norm_wkt_share"] +
        0.45 * eligible["norm_potm_rate"] +
        0.05 * eligible["norm_win_volume"]
    ).round(2)

    # Alternative 2: Pure Share (Run Share 50%, Wicket Share 50%)
    eligible["alt_pure_share_score"] = (
        0.50 * eligible["norm_run_share"] +
        0.50 * eligible["norm_wkt_share"]
    ).round(2)

    eligible = eligible.sort_values(by="baseline_score", ascending=False).reset_index(drop=True)
    eligible["baseline_rank"] = range(1, len(eligible) + 1)
    eligible["alt_potm_rank"] = eligible["alt_potm_heavy_score"].rank(ascending=False, method="min").astype(int)
    eligible["alt_share_rank"] = eligible["alt_pure_share_score"].rank(ascending=False, method="min").astype(int)

    rho_potm, _ = spearmanr(eligible["baseline_rank"], eligible["alt_potm_rank"])
    rho_share, _ = spearmanr(eligible["baseline_rank"], eligible["alt_share_rank"])

    sensitivity = {
        "ranking": "Match-Contribution Rankings",
        "population": len(eligible),
        "threshold": "matches_played >= 5",
        "spearman_potm_heavy": round(rho_potm, 4),
        "spearman_pure_share": round(rho_share, 4),
        "top_3_baseline": eligible.iloc[:3]["player_id"].tolist(),
        "top_3_potm_heavy": eligible.sort_values("alt_potm_rank").iloc[:3]["player_id"].tolist(),
        "top_3_pure_share": eligible.sort_values("alt_share_rank").iloc[:3]["player_id"].tolist()
    }

    out_csv = os.path.join(BASE_DIR, "ranking_match_contribution.csv")
    eligible.to_csv(out_csv, index=False)
    print(f"Exported {len(eligible)} ranked match contributors to {out_csv}")
    return eligible, sensitivity

# ==============================================================================
# 5. OVERALL PLAYER RANKING PIPELINE
# ==============================================================================
def run_overall_rankings(df_bat: pd.DataFrame, df_bowl: pd.DataFrame,
                         df_ar: pd.DataFrame, df_mc: pd.DataFrame) -> Tuple[pd.DataFrame, Dict[str, Any]]:
    print("\n--- Generating Overall Player Rankings ---")
    master = pd.read_csv(os.path.join(BASE_DIR, "player_features_master.csv"))

    # Map primary scores
    bat_score_map = df_bat.set_index("player_id")["baseline_score"].to_dict()
    bowl_score_map = df_bowl.set_index("player_id")["baseline_score"].to_dict()
    ar_score_map = df_ar.set_index("player_id")["baseline_score"].to_dict()
    mc_score_map = df_mc.set_index("player_id")["baseline_score"].to_dict()

    # Eligibility: Matches played >= 10 in either discipline
    eligible = master.copy()
    eligible["matches_played"] = df_mc.set_index("player_id")["matches_played"].reindex(eligible["player_id"]).fillna(0).values
    eligible = eligible[eligible["matches_played"] >= 10].copy()

    # Assign component ratings (defaulting gracefully to 0 if not qualified in discipline)
    eligible["batting_score"] = eligible["player_id"].map(bat_score_map).fillna(0.0).round(2)
    eligible["bowling_score"] = eligible["player_id"].map(bowl_score_map).fillna(0.0).round(2)
    eligible["allrounder_score"] = eligible["player_id"].map(ar_score_map).fillna(0.0).round(2)
    eligible["match_contribution_score"] = eligible["player_id"].map(mc_score_map).fillna(0.0).round(2)

    # Primary Specialist Metric: best of batting or bowling score
    eligible["primary_discipline_score"] = np.maximum(eligible["batting_score"], eligible["bowling_score"])

    # Baseline Overall Formulation:
    # 50% Primary Discipline + 20% All-Round Capability + 30% Match Contribution
    eligible["baseline_score"] = (
        0.50 * eligible["primary_discipline_score"] +
        0.20 * eligible["allrounder_score"] +
        0.30 * eligible["match_contribution_score"]
    ).round(2)

    # Alternative 1: Specialist-Heavy (Primary Discipline 70%, Match Contribution 30%, All-Round 0%)
    eligible["alt_specialist_score"] = (
        0.70 * eligible["primary_discipline_score"] +
        0.30 * eligible["match_contribution_score"]
    ).round(2)

    # Alternative 2: Contribution-Heavy (Match Contribution 50%, Primary Discipline 35%, All-Round 15%)
    eligible["alt_contribution_score"] = (
        0.35 * eligible["primary_discipline_score"] +
        0.15 * eligible["allrounder_score"] +
        0.50 * eligible["match_contribution_score"]
    ).round(2)

    eligible = eligible.sort_values(by="baseline_score", ascending=False).reset_index(drop=True)
    eligible["baseline_rank"] = range(1, len(eligible) + 1)
    eligible["alt_specialist_rank"] = eligible["alt_specialist_score"].rank(ascending=False, method="min").astype(int)
    eligible["alt_contribution_rank"] = eligible["alt_contribution_score"].rank(ascending=False, method="min").astype(int)

    rho_spec, _ = spearmanr(eligible["baseline_rank"], eligible["alt_specialist_rank"])
    rho_cont, _ = spearmanr(eligible["baseline_rank"], eligible["alt_contribution_rank"])

    sensitivity = {
        "ranking": "Overall Player Rankings",
        "population": len(eligible),
        "threshold": "matches_played >= 10",
        "spearman_specialist": round(rho_spec, 4),
        "spearman_contribution": round(rho_cont, 4),
        "top_3_baseline": eligible.iloc[:3]["player_id"].tolist(),
        "top_3_specialist": eligible.sort_values("alt_specialist_rank").iloc[:3]["player_id"].tolist(),
        "top_3_contribution": eligible.sort_values("alt_contribution_rank").iloc[:3]["player_id"].tolist()
    }

    out_csv = os.path.join(BASE_DIR, "ranking_overall.csv")
    eligible.to_csv(out_csv, index=False)
    print(f"Exported {len(eligible)} ranked overall players to {out_csv}")
    return eligible, sensitivity

# ==============================================================================
# 6. VISUALIZATION MODULE
# ==============================================================================
def generate_ranking_plots(df_bat: pd.DataFrame, df_bowl: pd.DataFrame,
                           df_ar: pd.DataFrame, df_mc: pd.DataFrame,
                           df_ov: pd.DataFrame, sensitivities: List[Dict[str, Any]]):
    print("\n--- Generating Ranking Charts ---")

    def plot_top15_bar(df: pd.DataFrame, score_col: str, title: str, filename: str, palette: str):
        plt.figure(figsize=(12, 6))
        top15 = df.head(15).copy()
        sns.barplot(data=top15, x=score_col, y="player_id", hue="player_id", palette=palette, edgecolor="k", legend=False)
        plt.title(f"{title}\n(Top-Ranked Players Within Eligible Dataset Under Stated Method)", fontsize=12, fontweight="bold")
        plt.xlabel("Score (0 - 100 Scale)", fontsize=11)
        plt.ylabel("Player ID", fontsize=11)
        plt.grid(True, linestyle=":", alpha=0.6)
        plt.tight_layout()
        path = os.path.join(PLOTS_DIR, filename)
        plt.savefig(path, dpi=300)
        plt.close()
        if os.path.exists(ARTIFACT_DIR):
            shutil.copy2(path, os.path.join(ARTIFACT_DIR, filename))

    plot_top15_bar(df_bat, "baseline_score", "PitchIQ — Top 15 Batters (Innings >= 15, Balls >= 500)", "rankings_batters_top15.png", "mako")
    plot_top15_bar(df_bowl, "baseline_score", "PitchIQ — Top 15 Bowlers (Balls >= 600, Wickets >= 15)", "rankings_bowlers_top15.png", "rocket")
    plot_top15_bar(df_ar, "baseline_score", "PitchIQ — Top 15 All-Rounders (Runs >= 200, Wickets >= 10)", "rankings_allrounders_top15.png", "viridis")
    plot_top15_bar(df_mc, "baseline_score", "PitchIQ — Top 15 Match Contributors (Matches >= 5)", "rankings_match_contribution_top15.png", "crest")
    plot_top15_bar(df_ov, "baseline_score", "PitchIQ — Top 15 Overall Players (Matches >= 10)", "rankings_overall_top15.png", "flare")

    # Sensitivity correlation summary plot
    plt.figure(figsize=(10, 5))
    names = [s["ranking"].replace(" Rankings", "").replace("Best-Performing ", "") for s in sensitivities]
    rhos1 = [list(s.values())[3] for s in sensitivities]
    rhos2 = [list(s.values())[4] for s in sensitivities]

    x = np.arange(len(names))
    width = 0.35
    plt.bar(x - width/2, rhos1, width, label="Alternative Scheme A", color="#2b5c8f", edgecolor="k")
    plt.bar(x + width/2, rhos2, width, label="Alternative Scheme B", color="#e27c38", edgecolor="k")

    plt.ylabel("Spearman Rank Correlation (ρ)", fontsize=11)
    plt.title("PitchIQ — Ranking Sensitivity Analysis Across Alternative Weighting Schemes", fontsize=12, fontweight="bold")
    plt.xticks(x, names, rotation=15, ha="right", fontsize=10)
    plt.ylim(0.7, 1.05)
    plt.axhline(1.0, color="gray", linestyle=":", alpha=0.5)
    plt.legend(loc="lower right")
    plt.grid(True, linestyle=":", alpha=0.5)
    plt.tight_layout()
    corr_path = os.path.join(PLOTS_DIR, "rankings_sensitivity_correlation.png")
    plt.savefig(corr_path, dpi=300)
    plt.close()
    if os.path.exists(ARTIFACT_DIR):
        shutil.copy2(corr_path, os.path.join(ARTIFACT_DIR, "rankings_sensitivity_correlation.png"))

def main():
    print("=" * 70)
    print("PitchIQ Player Intelligence Lab — Multi-Dimensional Ranking Pipeline")
    print("=" * 70)

    df_bat, sens_bat = run_batting_rankings()
    df_bowl, sens_bowl = run_bowling_rankings()
    df_ar, sens_ar = run_allrounder_rankings()
    df_mc, sens_mc = run_match_contribution_rankings()
    df_ov, sens_ov = run_overall_rankings(df_bat, df_bowl, df_ar, df_mc)

    sensitivities = [sens_bat, sens_bowl, sens_ar, sens_mc, sens_ov]

    # Save sensitivity analysis JSON
    with open(os.path.join(BASE_DIR, "ranking_sensitivity_analysis.json"), "w", encoding="utf-8") as f:
        json.dump(sensitivities, f, indent=2)

    # Plots
    generate_ranking_plots(df_bat, df_bowl, df_ar, df_mc, df_ov, sensitivities)

    print("\nAll player rankings and sensitivity analyses completed successfully!")

if __name__ == "__main__":
    main()
