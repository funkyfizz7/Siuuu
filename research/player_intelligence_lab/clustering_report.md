# PitchIQ Player Intelligence Lab — Unsupervised Clustering Experiments Report

This document reports the unsupervised clustering experiments performed exclusively on feature tables derived from `C:\Users\Khush\Desktop\TEST\eligible_ball_by_ball.csv`.

## 1. Methodology & Model Setup
- **Algorithms**:
  - K-Means with standardized numerical features (`StandardScaler`, fixed `random_state=42`, `n_init=10`).
  - Agglomerative Hierarchical Clustering (`linkage='ward'`, `metric='euclidean'`).
- **Dimensionality Reduction & Visualization**: 2D Principal Component Analysis (PCA) projections and Scipy dendrograms.
- **Diagnostic Metrics**: Evaluated over $k \in [2, 7]$ using Silhouette score, Davies-Bouldin index, Calinski-Harabasz score, and Inertia (Elbow method).
- **Rule of Non-Ranking**: Cluster labels denote topological playing profiles and tactical behaviours, never player rankings or qualitative superiority.

---

## 2. Summary of Experiments & Diagnostics

| Experiment | Population | Threshold Applied | Selected $k$ | Silhouette Score | Davies-Bouldin Index | Calinski-Harabasz Index |
|---|---|---|---|---|---|---|
| **1. Participation** | 344 | Census (All participating players) | **4** | **0.4983** | **0.8799** | **452.65** |
| **2. Batters** | 218 | Innings $\ge 5$, Balls Faced $\ge 150$, Valid Avg | **4** | **0.2739** | **1.1799** | **93.65** |
| **3. Bowlers** | 128 | Legal Balls $\ge 180$ (30 ov), Wickets $\ge 5$, Valid Avg | **3** | **0.2799** | **1.1995** | **53.92** |
| **4. All-Rounders** | 74 | Runs $\ge 100$, Wickets $\ge 5$, Valid Avg both | **3** | **0.2028** | **1.5629** | **20.04** |

---

## 3. High-Resolution Visual Plot Locations

All 9 generated PNG charts are stored in:
`research/player_intelligence_lab/plots/`

1. **Diagnostic Curves (Elbow & Silhouette)**: [`clustering_diagnostics.png`](file:///C:/Users/Khush/Siuu/research/player_intelligence_lab/plots/clustering_diagnostics.png)
2. **Participation Clustering PCA**: [`participation_clusters_pca.png`](file:///C:/Users/Khush/Siuu/research/player_intelligence_lab/plots/participation_clusters_pca.png)
3. **Participation Dendrogram**: [`participation_dendrogram.png`](file:///C:/Users/Khush/Siuu/research/player_intelligence_lab/plots/participation_dendrogram.png)
4. **Batter Clustering PCA**: [`batter_clusters_pca.png`](file:///C:/Users/Khush/Siuu/research/player_intelligence_lab/plots/batter_clusters_pca.png)
5. **Batter Dendrogram**: [`batter_dendrogram.png`](file:///C:/Users/Khush/Siuu/research/player_intelligence_lab/plots/batter_dendrogram.png)
6. **Bowler Clustering PCA**: [`bowler_clusters_pca.png`](file:///C:/Users/Khush/Siuu/research/player_intelligence_lab/plots/bowler_clusters_pca.png)
7. **Bowler Dendrogram**: [`bowler_dendrogram.png`](file:///C:/Users/Khush/Siuu/research/player_intelligence_lab/plots/bowler_dendrogram.png)
8. **All-Rounder Clustering PCA**: [`allrounder_clusters_pca.png`](file:///C:/Users/Khush/Siuu/research/player_intelligence_lab/plots/allrounder_clusters_pca.png)
9. **All-Rounder Dendrogram**: [`allrounder_dendrogram.png`](file:///C:/Users/Khush/Siuu/research/player_intelligence_lab/plots/allrounder_dendrogram.png)

---

## 4. Cluster Profile Interpretation

### 4.1 Participation Clusters ($k = 4$, $N = 344$)
- **Cluster 0 ($n = 75$)**: Regular Multi-Discipline Contributors (moderate batting and bowling involvement).
- **Cluster 1 ($n = 37$)**: Workhorse Bowlers & Heavy All-Rounders (high bowling workload: avg 5,321 legal balls).
- **Cluster 2 ($n = 39$)**: Core Specialist Batters (high batting volume: avg 3,755 balls faced, 29 matches).
- **Cluster 3 ($n = 193$)**: Low-Appearance Fringe / Transitional Players (avg ~3 matches).

### 4.2 Batter Clusters ($k = 4$, $N = 218$)
- **Cluster 0 ($n = 49$)**: High-Tempo Stroke-Makers (Strike Rate ~66, Boundary % ~57%, Balls/Inn ~43).
- **Cluster 1 ($n = 82$)**: Established Anchors & Top-Order Accumulators (Batting Avg ~38, Dot % ~76%, Balls/Inn ~74).
- **Cluster 2 ($n = 57$)**: Lower-Order Resistance & Transition Batters (Batting Avg ~17, Balls/Inn ~32).
- **Cluster 3 ($n = 30$)**: Defensive Tail & Heavy Survivalists (Batting Avg ~11, Strike Rate ~33, Dot % ~82%).

### 4.3 Bowler Clusters ($k = 3$, $N = 128$)
- **Cluster 0 ($n = 54$)**: High-Accuracy Metronomic Strike Bowlers (Avg 26.58, Econ 2.78, Accuracy 69.4%).
- **Cluster 1 ($n = 16$)**: Low-Penetration / Expensive Support Bowlers (Avg 57.48, Strike Rate 105.4).
- **Cluster 2 ($n = 58$)**: Aggressive / Higher-Variant Pace & Spin Attackers (Avg 31.90, Econ 3.50, Strike Rate 55.6).

### 4.4 All-Rounder Clusters ($k = 3$, $N = 74$)
- **Cluster 0 ($n = 36$)**: Bowling All-Rounders / Attacking Tail (Batting Avg ~20, Bowling Avg ~34, Differential -14.11).
- **Cluster 1 ($n = 26$)**: Defensive & Accurate Bowling All-Rounders (Batting Avg ~15, Bowling Avg ~30, Accuracy 71.3%, Differential -15.21).
- **Cluster 2 ($n = 12$)**: Genuine Match-Winning All-Rounders (Batting Avg 45.60, Bowling Avg 31.62, Differential +13.97).
