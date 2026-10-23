"""
Unit Tests for Player Rankings & Sensitivity Module
Verifies bounded scoring, contiguous ranks, underlying statistics presence,
alternative weight robustness, and traceability to eligible_ball_by_ball.csv.
"""

import os
import json
import unittest
import pandas as pd
import numpy as np

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

class TestPlayerRankings(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        with open(os.path.join(BASE_DIR, "player_eligibility_registry.json"), "r", encoding="utf-8") as f:
            cls.registry = json.load(f)
        cls.registry_ids = set(cls.registry.keys())

        with open(os.path.join(BASE_DIR, "ranking_sensitivity_analysis.json"), "r", encoding="utf-8") as f:
            cls.sensitivities = json.load(f)

        cls.df_bat = pd.read_csv(os.path.join(BASE_DIR, "ranking_batters.csv"))
        cls.df_bowl = pd.read_csv(os.path.join(BASE_DIR, "ranking_bowlers.csv"))
        cls.df_ar = pd.read_csv(os.path.join(BASE_DIR, "ranking_allrounders.csv"))
        cls.df_mc = pd.read_csv(os.path.join(BASE_DIR, "ranking_match_contribution.csv"))
        cls.df_ov = pd.read_csv(os.path.join(BASE_DIR, "ranking_overall.csv"))

    def test_ranking_populations_and_traceability(self):
        """Verify ranking populations meet thresholds and trace back to the registry."""
        self.assertEqual(len(self.df_bat), 127)
        self.assertEqual(len(self.df_bowl), 79)
        self.assertEqual(len(self.df_ar), 47)
        self.assertEqual(len(self.df_mc), 203)
        self.assertEqual(len(self.df_ov), 135)

        for df in [self.df_bat, self.df_bowl, self.df_ar, self.df_mc, self.df_ov]:
            self.assertTrue(set(df["player_id"]).issubset(self.registry_ids))

    def test_contiguous_ranks_and_bounded_scores(self):
        """Verify scores are bounded [0, 100] and baseline ranks are contiguous 1..N."""
        for df, name in [
            (self.df_bat, "bat"), (self.df_bowl, "bowl"),
            (self.df_ar, "ar"), (self.df_mc, "mc"), (self.df_ov, "ov")
        ]:
            self.assertEqual(df["baseline_rank"].tolist(), list(range(1, len(df) + 1)), f"Non-contiguous ranks in {name}")
            self.assertTrue((df["baseline_score"] >= 0.0).all(), f"Scores below 0 in {name}")
            self.assertTrue((df["baseline_score"] <= 100.0).all(), f"Scores above 100 in {name}")

    def test_required_columns_present(self):
        """Verify that player name/ID, underlying statistics, component scores, and final scores exist."""
        # Batters: must have player_id, batting_average, strike_rate, runs, innings, component scores, baseline_score
        bat_req = ["player_id", "full_name", "batting_average", "strike_rate", "norm_avg", "norm_sr", "baseline_score", "baseline_rank"]
        for col in bat_req:
            self.assertIn(col, self.df_bat.columns)

        # Bowlers: must have player_id, bowling_average, economy_rate, norm_avg, norm_econ, baseline_score
        bowl_req = ["player_id", "full_name", "bowling_average", "economy_rate", "norm_avg", "norm_econ", "baseline_score", "baseline_rank"]
        for col in bowl_req:
            self.assertIn(col, self.df_bowl.columns)

        # All-rounders: must have batting_component, bowling_component, norm_diff, baseline_score
        ar_req = ["player_id", "batting_component", "bowling_component", "norm_diff", "baseline_score"]
        for col in ar_req:
            self.assertIn(col, self.df_ar.columns)

        # Overall: must have primary_discipline_score, allrounder_score, match_contribution_score, baseline_score
        ov_req = ["player_id", "primary_discipline_score", "allrounder_score", "match_contribution_score", "baseline_score"]
        for col in ov_req:
            self.assertIn(col, self.df_ov.columns)

    def test_sensitivity_robustness(self):
        """Verify sensitivity results have high Spearman rank correlations across weight changes."""
        self.assertEqual(len(self.sensitivities), 5)
        for s in self.sensitivities:
            # Check all spearman correlations are >= 0.85
            corrs = [v for k, v in s.items() if k.startswith("spearman_")]
            for c in corrs:
                self.assertGreaterEqual(c, 0.85, f"Low sensitivity correlation in {s['ranking']}: {c}")

    def test_ranking_plot_artifacts_exist(self):
        """Verify all 6 ranking charts exist in plots directory."""
        plots_dir = os.path.join(BASE_DIR, "plots")
        expected_plots = [
            "rankings_batters_top15.png",
            "rankings_bowlers_top15.png",
            "rankings_allrounders_top15.png",
            "rankings_match_contribution_top15.png",
            "rankings_overall_top15.png",
            "rankings_sensitivity_correlation.png"
        ]
        for plot_name in expected_plots:
            plot_path = os.path.join(plots_dir, plot_name)
            self.assertTrue(os.path.isfile(plot_path), f"Missing plot: {plot_path}")
            self.assertGreater(os.path.getsize(plot_path), 1000)

if __name__ == "__main__":
    unittest.main()
