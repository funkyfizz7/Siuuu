"""
Unit Tests for Clustering Experiments Module
Verifies reproducibility, dataset traceability, cluster diagnostics, and absence of data leakage.
"""

import os
import json
import unittest
import pandas as pd
import numpy as np

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

class TestClusteringExperiments(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        with open(os.path.join(BASE_DIR, "player_eligibility_registry.json"), "r", encoding="utf-8") as f:
            cls.registry = json.load(f)
        cls.registry_ids = set(cls.registry.keys())

        with open(os.path.join(BASE_DIR, "clustering_summary.json"), "r", encoding="utf-8") as f:
            cls.summary = json.load(f)

    def test_summary_and_diagnostics_structure(self):
        """Verify diagnostics scores are within mathematical bounds."""
        self.assertEqual(self.summary["random_seed"], 42)
        experiments = self.summary["experiments"]
        self.assertEqual(len(experiments), 4)

        for exp in experiments:
            sil = exp["silhouette_score"]
            self.assertGreaterEqual(sil, -1.0)
            self.assertLessEqual(sil, 1.0)
            self.assertGreater(exp["population"], 0)
            self.assertIn("chosen_k", exp)

    def test_participation_clusters_traceability(self):
        """Verify participation clusters cover exactly all 344 players without leakage."""
        df = pd.read_csv(os.path.join(BASE_DIR, "participation_clusters.csv"))
        self.assertEqual(len(df), 344)
        self.assertEqual(set(df["player_id"]), self.registry_ids)
        self.assertFalse(df[["kmeans_cluster", "hierarchical_cluster"]].isna().any().any())
        self.assertEqual(sorted(df["kmeans_cluster"].unique()), [0, 1, 2, 3])

    def test_batter_clusters_traceability_and_bounds(self):
        """Verify eligible batters satisfy participation threshold and trace to registry."""
        df = pd.read_csv(os.path.join(BASE_DIR, "batter_clusters.csv"))
        self.assertEqual(len(df), 218)
        self.assertTrue(set(df["player_id"]).issubset(self.registry_ids))
        self.assertTrue((df["innings"] >= 5).all())
        self.assertTrue((df["balls_faced"] >= 150).all())
        self.assertFalse(df["batting_average"].isna().any())
        self.assertEqual(sorted(df["kmeans_cluster"].unique()), [0, 1, 2, 3])

    def test_bowler_clusters_traceability_and_bounds(self):
        """Verify eligible bowlers satisfy minimum overs and trace to registry."""
        df = pd.read_csv(os.path.join(BASE_DIR, "bowler_clusters.csv"))
        self.assertEqual(len(df), 128)
        self.assertTrue(set(df["player_id"]).issubset(self.registry_ids))
        self.assertTrue((df["legal_balls_bowled"] >= 180).all())
        self.assertTrue((df["wickets"] >= 5).all())
        self.assertFalse(df["bowling_average"].isna().any())
        self.assertEqual(sorted(df["kmeans_cluster"].unique()), [0, 1, 2])

    def test_allrounder_clusters_traceability(self):
        """Verify dual-role all-rounders satisfy both batting and bowling requirements."""
        df = pd.read_csv(os.path.join(BASE_DIR, "allrounder_clusters.csv"))
        self.assertEqual(len(df), 74)
        self.assertTrue(set(df["player_id"]).issubset(self.registry_ids))
        self.assertTrue((df["runs"] >= 100).all())
        self.assertTrue((df["wickets"] >= 5).all())
        self.assertFalse(df["allrounder_differential"].isna().any())
        self.assertEqual(sorted(df["kmeans_cluster"].unique()), [0, 1, 2])

    def test_plot_artifacts_exist(self):
        """Verify all generated plots exist on disk."""
        plots_dir = os.path.join(BASE_DIR, "plots")
        expected_plots = [
            "participation_clusters_pca.png",
            "participation_dendrogram.png",
            "batter_clusters_pca.png",
            "batter_dendrogram.png",
            "bowler_clusters_pca.png",
            "bowler_dendrogram.png",
            "allrounder_clusters_pca.png",
            "allrounder_dendrogram.png",
            "clustering_diagnostics.png"
        ]
        for plot_name in expected_plots:
            plot_path = os.path.join(plots_dir, plot_name)
            self.assertTrue(os.path.isfile(plot_path), f"Missing plot: {plot_path}")
            self.assertGreater(os.path.getsize(plot_path), 1000)

if __name__ == "__main__":
    unittest.main()
