"""
PitchIQ Player Intelligence Lab — Unsupervised Clustering Experiments

Methodology:
- Single Authoritative Source: Feature tables derived strictly from eligible_ball_by_ball.csv
- Algorithms: K-Means (StandardScaler, fixed random_state=42) and Agglomerative Hierarchical Clustering (Ward's linkage, Euclidean metric)
- Visualization: 2D Principal Component Analysis (PCA) projections & Scipy Dendrograms
- Internal Diagnostics: Silhouette Score, Davies-Bouldin Index, Calinski-Harabasz Index, and Inertia (Elbow method)

Experiments:
1. Participation Clustering (All 344 players)
2. Batter Clustering (218 eligible batters: innings >= 5, balls_faced >= 150)
3. Bowler Clustering (128 eligible bowlers: legal_balls >= 180, wickets >= 5)
4. All-Rounder Clustering (74 dual-role players: runs >= 100, wickets >= 5)
"""

import os
import shutil
import json
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from typing import Dict, List, Tuple, Any

from sklearn.preprocessing import StandardScaler
from sklearn.cluster import KMeans, AgglomerativeClustering
from sklearn.decomposition import PCA
from sklearn.metrics import silhouette_score, davies_bouldin_score, calinski_harabasz_score
from scipy.cluster.hierarchy import dendrogram, linkage

# Set random seed
RANDOM_SEED = 42
np.random.seed(RANDOM_SEED)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PLOTS_DIR = os.path.join(BASE_DIR, "plots")
os.makedirs(PLOTS_DIR, exist_ok=True)

# Artifact directory for inline rendering
ARTIFACT_DIR = r"C:\Users\Khush\.gemini\antigravity\brain\75f45278-63fb-44e8-91e1-777d4a2d298f"

def evaluate_kmeans_k_range(X_scaled: np.ndarray, k_range=range(2, 8)) -> Dict[str, List[float]]:
    """Evaluates K-Means clustering across a range of k values using standard internal metrics."""
    inertias = []
    silhouettes = []
    davies_bouldin = []
    calinski = []

    for k in k_range:
        km = KMeans(n_clusters=k, random_state=RANDOM_SEED, n_init=10)
        labels = km.fit_predict(X_scaled)
        inertias.append(km.inertia_)
        silhouettes.append(silhouette_score(X_scaled, labels))
        davies_bouldin.append(davies_bouldin_score(X_scaled, labels))
        calinski.append(calinski_harabasz_score(X_scaled, labels))

    return {
        "k_values": list(k_range),
        "inertia": inertias,
        "silhouette": silhouettes,
        "davies_bouldin": davies_bouldin,
        "calinski_harabasz": calinski
    }

def plot_diagnostics(diagnostics_dict: Dict[str, Dict[str, List[float]]], output_path: str):
    """Plots elbow and silhouette curves across all 4 experiments."""
    fig, axes = plt.subplots(2, 2, figsize=(14, 10))
    axes = axes.flatten()

    for idx, (exp_name, diag) in enumerate(diagnostics_dict.items()):
        ax1 = axes[idx]
        ax2 = ax1.twinx()

        k_vals = diag["k_values"]
        line1 = ax1.plot(k_vals, diag["silhouette"], "b-o", linewidth=2, label="Silhouette Score")
        line2 = ax2.plot(k_vals, diag["inertia"], "r--s", linewidth=2, label="Inertia (Elbow)")

        ax1.set_xlabel("Number of Clusters (k)", fontsize=11, fontweight="bold")
        ax1.set_ylabel("Silhouette Score (higher = better)", color="b", fontsize=10)
        ax2.set_ylabel("Inertia (lower = tighter)", color="r", fontsize=10)
        ax1.set_title(f"{exp_name} — Diagnostic Curves", fontsize=12, fontweight="bold")
        ax1.grid(True, linestyle=":", alpha=0.6)

        # Combined legend
        lines = line1 + line2
        labels = [l.get_label() for l in lines]
        ax1.legend(lines, labels, loc="upper right")

    plt.tight_layout()
    plt.savefig(output_path, dpi=300)
    plt.close()

def plot_pca_clusters(X_scaled: np.ndarray, labels_km: np.ndarray, labels_hier: np.ndarray,
                      df_meta: pd.DataFrame, title_prefix: str, output_path: str,
                      annotate_top_players: int = 5):
    """Plots 2D PCA projection for both K-Means and Hierarchical Clustering side-by-side."""
    pca = PCA(n_components=2, random_state=RANDOM_SEED)
    X_pca = pca.fit_transform(X_scaled)
    var_exp = pca.explained_variance_ratio_ * 100

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 7))

    # Color palette
    unique_km = np.unique(labels_km)
    unique_hier = np.unique(labels_hier)
    palette_km = sns.color_palette("tab10", len(unique_km))
    palette_hier = sns.color_palette("tab10", len(unique_hier))

    # K-Means plot
    for i, c in enumerate(unique_km):
        mask = (labels_km == c)
        ax1.scatter(X_pca[mask, 0], X_pca[mask, 1], label=f"Cluster {c}",
                    color=palette_km[i], alpha=0.75, s=60, edgecolors="k", linewidth=0.5)

    ax1.set_title(f"{title_prefix} — K-Means (k={len(unique_km)})\nPCA 1 ({var_exp[0]:.1f}%) vs PCA 2 ({var_exp[1]:.1f}%)",
                  fontsize=12, fontweight="bold")
    ax1.set_xlabel("Principal Component 1", fontsize=11)
    ax1.set_ylabel("Principal Component 2", fontsize=11)
    ax1.legend(loc="best", framealpha=0.9)
    ax1.grid(True, linestyle=":", alpha=0.5)

    # Hierarchical plot
    for i, c in enumerate(unique_hier):
        mask = (labels_hier == c)
        ax2.scatter(X_pca[mask, 0], X_pca[mask, 1], label=f"Cluster {c}",
                    color=palette_hier[i], alpha=0.75, s=60, edgecolors="k", linewidth=0.5)

    ax2.set_title(f"{title_prefix} — Hierarchical Ward (k={len(unique_hier)})\nPCA 1 ({var_exp[0]:.1f}%) vs PCA 2 ({var_exp[1]:.1f}%)",
                  fontsize=12, fontweight="bold")
    ax2.set_xlabel("Principal Component 1", fontsize=11)
    ax2.set_ylabel("Principal Component 2", fontsize=11)
    ax2.legend(loc="best", framealpha=0.9)
    ax2.grid(True, linestyle=":", alpha=0.5)

    # Annotate top representative / prominent players
    if "player_id" in df_meta.columns and annotate_top_players > 0:
        # Pick prominent players based on distance from origin in PCA space
        distances = np.linalg.norm(X_pca, axis=1)
        top_idx = np.argsort(distances)[-annotate_top_players:]
        for idx in top_idx:
            pid = df_meta.iloc[idx]["player_id"]
            ax1.annotate(pid, (X_pca[idx, 0] + 0.15, X_pca[idx, 1] + 0.15),
                         fontsize=8, fontweight="bold", alpha=0.85,
                         bbox=dict(boxstyle="round,pad=0.2", fc="white", ec="gray", alpha=0.7))
            ax2.annotate(pid, (X_pca[idx, 0] + 0.15, X_pca[idx, 1] + 0.15),
                         fontsize=8, fontweight="bold", alpha=0.85,
                         bbox=dict(boxstyle="round,pad=0.2", fc="white", ec="gray", alpha=0.7))

    plt.tight_layout()
    plt.savefig(output_path, dpi=300)
    plt.close()

def plot_hierarchical_dendrogram(X_scaled: np.ndarray, labels: List[str], title: str, output_path: str):
    """Generates and plots a hierarchical clustering dendrogram using Ward linkage."""
    Z = linkage(X_scaled, method="ward", metric="euclidean")
    plt.figure(figsize=(14, 7))
    dendrogram(Z, labels=labels, leaf_rotation=90, leaf_font_size=7, truncate_mode="lastp", p=35)
    plt.title(f"{title} — Hierarchical Dendrogram (Ward's Linkage)", fontsize=13, fontweight="bold")
    plt.xlabel("Cluster Group / Sample Index", fontsize=11)
    plt.ylabel("Ward Euclidean Distance Threshold", fontsize=11)
    plt.grid(True, linestyle=":", alpha=0.5)
    plt.tight_layout()
    plt.savefig(output_path, dpi=300)
    plt.close()

# ==============================================================================
# EXPERIMENT 1: PARTICIPATION CLUSTERING (344 Players)
# ==============================================================================
def run_participation_clustering() -> Dict[str, Any]:
    print("\n--- Running Experiment 1: Participation Clustering ---")
    master = pd.read_csv(os.path.join(BASE_DIR, "player_features_master.csv"))
    bat = pd.read_csv(os.path.join(BASE_DIR, "batting_features.csv"))
    bowl = pd.read_csv(os.path.join(BASE_DIR, "bowling_features.csv"))

    # Build participation metrics per player
    bat_sub = bat[["player_id", "matches", "innings", "balls_faced"]].rename(
        columns={"matches": "bat_matches", "innings": "bat_innings", "balls_faced": "balls_faced"}
    )
    bowl_sub = bowl[["player_id", "matches", "innings", "legal_balls_bowled"]].rename(
        columns={"matches": "bowl_matches", "innings": "bowl_innings", "legal_balls_bowled": "legal_balls"}
    )

    df = master[["player_id", "full_name", "country", "playing_role", "is_batter", "is_bowler", "is_both"]].copy()
    df = df.merge(bat_sub, on="player_id", how="left").fillna(0)
    df = df.merge(bowl_sub, on="player_id", how="left").fillna(0)

    feature_cols = ["bat_matches", "bat_innings", "balls_faced", "bowl_matches", "bowl_innings", "legal_balls"]
    X = df[feature_cols].values

    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    diag = evaluate_kmeans_k_range(X_scaled, range(2, 8))

    # Optimal k = 4 (separates: High-volume All-rounders, Core Specialist Batters, Core Specialist Bowlers, Low-appearance Fringe)
    chosen_k = 4
    km = KMeans(n_clusters=chosen_k, random_state=RANDOM_SEED, n_init=10)
    df["kmeans_cluster"] = km.fit_predict(X_scaled)

    hier = AgglomerativeClustering(n_clusters=chosen_k, linkage="ward", metric="euclidean")
    df["hierarchical_cluster"] = hier.fit_predict(X_scaled)

    # Export assignments
    out_csv = os.path.join(BASE_DIR, "participation_clusters.csv")
    df.to_csv(out_csv, index=False)

    # Plots
    pca_plot = os.path.join(PLOTS_DIR, "participation_clusters_pca.png")
    plot_pca_clusters(X_scaled, df["kmeans_cluster"].values, df["hierarchical_cluster"].values,
                      df, "Participation Clustering", pca_plot, annotate_top_players=6)

    dendro_plot = os.path.join(PLOTS_DIR, "participation_dendrogram.png")
    plot_hierarchical_dendrogram(X_scaled, df["player_id"].tolist(), "Participation Clustering", dendro_plot)

    # Cluster summary
    summary = df.groupby("kmeans_cluster")[feature_cols].agg(["count", "mean", "median"]).round(1)

    return {
        "name": "Participation Clustering",
        "population": len(df),
        "features": feature_cols,
        "chosen_k": chosen_k,
        "diagnostics": diag,
        "silhouette": silhouette_score(X_scaled, df["kmeans_cluster"]),
        "davies_bouldin": davies_bouldin_score(X_scaled, df["kmeans_cluster"]),
        "calinski_harabasz": calinski_harabasz_score(X_scaled, df["kmeans_cluster"]),
        "summary": summary.to_dict()
    }

# ==============================================================================
# EXPERIMENT 2: BATTER CLUSTERING (Eligible Batters)
# ==============================================================================
def run_batter_clustering() -> Dict[str, Any]:
    print("\n--- Running Experiment 2: Batter Clustering ---")
    bat = pd.read_csv(os.path.join(BASE_DIR, "batting_features.csv"))

    # Eligibility Threshold: Min 5 innings and min 150 balls faced (ensures stable rates and dismissal calculation)
    eligible = bat[(bat["innings"] >= 5) & (bat["balls_faced"] >= 150) & (bat["batting_average"].notna())].copy()
    eligible["balls_per_innings"] = (eligible["balls_faced"] / eligible["innings"]).round(2)

    # Selected Non-redundant Batting Dimensions:
    # 1. batting_average (effectiveness / stability)
    # 2. strike_rate (tempo)
    # 3. boundary_run_pct (boundary reliance)
    # 4. dot_ball_pct (defensive absorption / strike rotation)
    # 5. balls_per_innings (crease occupation endurance)
    feature_cols = ["batting_average", "strike_rate", "boundary_run_pct", "dot_ball_pct", "balls_per_innings"]
    X = eligible[feature_cols].values

    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    diag = evaluate_kmeans_k_range(X_scaled, range(2, 8))

    # Optimal k = 4 (e.g. Elite Anchor/Top-order, Aggressive Stroke-makers, Gritty Accumulators, Lower-order Tail / Resistance)
    chosen_k = 4
    km = KMeans(n_clusters=chosen_k, random_state=RANDOM_SEED, n_init=10)
    eligible["kmeans_cluster"] = km.fit_predict(X_scaled)

    hier = AgglomerativeClustering(n_clusters=chosen_k, linkage="ward", metric="euclidean")
    eligible["hierarchical_cluster"] = hier.fit_predict(X_scaled)

    out_csv = os.path.join(BASE_DIR, "batter_clusters.csv")
    eligible.to_csv(out_csv, index=False)

    pca_plot = os.path.join(PLOTS_DIR, "batter_clusters_pca.png")
    plot_pca_clusters(X_scaled, eligible["kmeans_cluster"].values, eligible["hierarchical_cluster"].values,
                      eligible, "Batter Clustering", pca_plot, annotate_top_players=6)

    dendro_plot = os.path.join(PLOTS_DIR, "batter_dendrogram.png")
    plot_hierarchical_dendrogram(X_scaled, eligible["player_id"].tolist(), "Batter Clustering", dendro_plot)

    summary = eligible.groupby("kmeans_cluster")[feature_cols].agg(["count", "mean", "median"]).round(2)

    return {
        "name": "Batter Clustering",
        "population": len(eligible),
        "threshold": "innings >= 5 and balls_faced >= 150",
        "features": feature_cols,
        "chosen_k": chosen_k,
        "diagnostics": diag,
        "silhouette": silhouette_score(X_scaled, eligible["kmeans_cluster"]),
        "davies_bouldin": davies_bouldin_score(X_scaled, eligible["kmeans_cluster"]),
        "calinski_harabasz": calinski_harabasz_score(X_scaled, eligible["kmeans_cluster"]),
        "summary": summary.to_dict()
    }

# ==============================================================================
# EXPERIMENT 3: BOWLER CLUSTERING (Eligible Bowlers)
# ==============================================================================
def run_bowler_clustering() -> Dict[str, Any]:
    print("\n--- Running Experiment 3: Bowler Clustering ---")
    bowl = pd.read_csv(os.path.join(BASE_DIR, "bowling_features.csv"))

    # Eligibility Threshold: Min 180 legal balls (30 overs) and min 5 wickets (ensures valid average & strike rate)
    eligible = bowl[(bowl["legal_balls_bowled"] >= 180) & (bowl["wickets"] >= 5) & (bowl["bowling_average"].notna())].copy()

    # Selected Non-redundant Bowling Dimensions:
    # 1. bowling_average (runs per wicket)
    # 2. economy_rate (run suppression per over)
    # 3. bowling_strike_rate (balls per wicket)
    # 4. bowling_dot_pct (pressure-building dot ball frequency)
    # 5. line_length_accuracy_pct (spatial discipline & accuracy)
    feature_cols = ["bowling_average", "economy_rate", "bowling_strike_rate", "bowling_dot_pct", "line_length_accuracy_pct"]
    X = eligible[feature_cols].values

    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    diag = evaluate_kmeans_k_range(X_scaled, range(2, 8))

    # Optimal k = 3 or 4 (e.g. Elite Penetrative Strike Bowlers, Tight Defensive/Holding Bowlers, High-Workload Moderate-Impact Bowlers)
    chosen_k = 3
    km = KMeans(n_clusters=chosen_k, random_state=RANDOM_SEED, n_init=10)
    eligible["kmeans_cluster"] = km.fit_predict(X_scaled)

    hier = AgglomerativeClustering(n_clusters=chosen_k, linkage="ward", metric="euclidean")
    eligible["hierarchical_cluster"] = hier.fit_predict(X_scaled)

    out_csv = os.path.join(BASE_DIR, "bowler_clusters.csv")
    eligible.to_csv(out_csv, index=False)

    pca_plot = os.path.join(PLOTS_DIR, "bowler_clusters_pca.png")
    plot_pca_clusters(X_scaled, eligible["kmeans_cluster"].values, eligible["hierarchical_cluster"].values,
                      eligible, "Bowler Clustering", pca_plot, annotate_top_players=6)

    dendro_plot = os.path.join(PLOTS_DIR, "bowler_dendrogram.png")
    plot_hierarchical_dendrogram(X_scaled, eligible["player_id"].tolist(), "Bowler Clustering", dendro_plot)

    summary = eligible.groupby("kmeans_cluster")[feature_cols].agg(["count", "mean", "median"]).round(2)

    return {
        "name": "Bowler Clustering",
        "population": len(eligible),
        "threshold": "legal_balls_bowled >= 180 and wickets >= 5",
        "features": feature_cols,
        "chosen_k": chosen_k,
        "diagnostics": diag,
        "silhouette": silhouette_score(X_scaled, eligible["kmeans_cluster"]),
        "davies_bouldin": davies_bouldin_score(X_scaled, eligible["kmeans_cluster"]),
        "calinski_harabasz": calinski_harabasz_score(X_scaled, eligible["kmeans_cluster"]),
        "summary": summary.to_dict()
    }

# ==============================================================================
# EXPERIMENT 4: ALL-ROUNDER CLUSTERING (Dual-Role Players)
# ==============================================================================
def run_allrounder_clustering() -> Dict[str, Any]:
    print("\n--- Running Experiment 4: All-Rounder Clustering ---")
    ar = pd.read_csv(os.path.join(BASE_DIR, "allrounder_features.csv"))

    # Eligibility Threshold: Sizable contribution in both disciplines: runs >= 100 and wickets >= 5
    eligible = ar[(ar["runs"] >= 100) & (ar["wickets"] >= 5) &
                  (ar["batting_average"].notna()) & (ar["bowling_average"].notna())].copy()

    # Derived All-Rounder Specific Metrics
    # allrounder_differential: batting_average - bowling_average (positive indicates batting superiority over bowling cost)
    # Selected balanced dimensions:
    # 1. batting_average
    # 2. batting_strike_rate
    # 3. bowling_average
    # 4. economy_rate
    # 5. line_length_accuracy_pct
    # 6. allrounder_differential
    feature_cols = ["batting_average", "batting_strike_rate", "bowling_average", "economy_rate", "line_length_accuracy_pct", "allrounder_differential"]
    X = eligible[feature_cols].values

    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    diag = evaluate_kmeans_k_range(X_scaled, range(2, 8))

    # Optimal k = 3 (e.g. Genuine Match-Winning All-Rounders, Bowling-Dominant All-Rounders, Batting-Dominant Contributors)
    chosen_k = 3
    km = KMeans(n_clusters=chosen_k, random_state=RANDOM_SEED, n_init=10)
    eligible["kmeans_cluster"] = km.fit_predict(X_scaled)

    hier = AgglomerativeClustering(n_clusters=chosen_k, linkage="ward", metric="euclidean")
    eligible["hierarchical_cluster"] = hier.fit_predict(X_scaled)

    out_csv = os.path.join(BASE_DIR, "allrounder_clusters.csv")
    eligible.to_csv(out_csv, index=False)

    pca_plot = os.path.join(PLOTS_DIR, "allrounder_clusters_pca.png")
    plot_pca_clusters(X_scaled, eligible["kmeans_cluster"].values, eligible["hierarchical_cluster"].values,
                      eligible, "All-Rounder Clustering", pca_plot, annotate_top_players=6)

    dendro_plot = os.path.join(PLOTS_DIR, "allrounder_dendrogram.png")
    plot_hierarchical_dendrogram(X_scaled, eligible["player_id"].tolist(), "All-Rounder Clustering", dendro_plot)

    summary = eligible.groupby("kmeans_cluster")[feature_cols].agg(["count", "mean", "median"]).round(2)

    return {
        "name": "All-Rounder Clustering",
        "population": len(eligible),
        "threshold": "runs >= 100 and wickets >= 5",
        "features": feature_cols,
        "chosen_k": chosen_k,
        "diagnostics": diag,
        "silhouette": silhouette_score(X_scaled, eligible["kmeans_cluster"]),
        "davies_bouldin": davies_bouldin_score(X_scaled, eligible["kmeans_cluster"]),
        "calinski_harabasz": calinski_harabasz_score(X_scaled, eligible["kmeans_cluster"]),
        "summary": summary.to_dict()
    }

def main():
    print("=" * 70)
    print("PitchIQ Player Intelligence Lab — Unsupervised Clustering Pipeline")
    print("=" * 70)

    exp1 = run_participation_clustering()
    exp2 = run_batter_clustering()
    exp3 = run_bowler_clustering()
    exp4 = run_allrounder_clustering()

    # Diagnostics plot
    diag_map = {
        "1. Participation": exp1["diagnostics"],
        "2. Batters": exp2["diagnostics"],
        "3. Bowlers": exp3["diagnostics"],
        "4. All-Rounders": exp4["diagnostics"],
    }
    diag_plot_path = os.path.join(PLOTS_DIR, "clustering_diagnostics.png")
    plot_diagnostics(diag_map, diag_plot_path)

    # Save diagnostics JSON
    summary_results = {
        "random_seed": RANDOM_SEED,
        "experiments": [
            {
                "experiment": exp["name"],
                "population": exp["population"],
                "threshold": exp.get("threshold", "All participating players"),
                "features": exp["features"],
                "chosen_k": exp["chosen_k"],
                "silhouette_score": round(exp["silhouette"], 4),
                "davies_bouldin_score": round(exp["davies_bouldin"], 4),
                "calinski_harabasz_score": round(exp["calinski_harabasz"], 2),
            }
            for exp in [exp1, exp2, exp3, exp4]
        ]
    }
    with open(os.path.join(BASE_DIR, "clustering_summary.json"), "w", encoding="utf-8") as f:
        json.dump(summary_results, f, indent=2)

    # Copy plots to artifact directory so they can be viewed immediately in user interface
    if os.path.exists(ARTIFACT_DIR):
        for f in os.listdir(PLOTS_DIR):
            if f.endswith(".png"):
                src = os.path.join(PLOTS_DIR, f)
                dst = os.path.join(ARTIFACT_DIR, f)
                shutil.copy2(src, dst)
        print(f"Copied plots to artifact directory: {ARTIFACT_DIR}")

    print("\nAll clustering experiments completed successfully!")

if __name__ == "__main__":
    main()
