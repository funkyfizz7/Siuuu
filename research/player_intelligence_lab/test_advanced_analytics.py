"""
Unit Tests for Advanced Analytics Module
Verifies Home/Away determination, Match Impact calculation, All-Round normalization,
and Player Consistency metrics against eligible_ball_by_ball.csv.
"""

import os
import json
import unittest
import pandas as pd
import numpy as np

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

class TestAdvancedAnalytics(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        with open(os.path.join(BASE_DIR, "player_eligibility_registry.json"), "r", encoding="utf-8") as f:
            cls.registry = json.load(f)
        cls.registry_ids = set(cls.registry.keys())

        with open(os.path.join(BASE_DIR, "home_away_verification.json"), "r", encoding="utf-8") as f:
            cls.ha_info = json.load(f)

        cls.df_impact = pd.read_csv(os.path.join(BASE_DIR, "match_impact_analytics.csv"))
        cls.df_ar = pd.read_csv(os.path.join(BASE_DIR, "allround_performance_ratings.csv"))
        cls.df_bat_cons = pd.read_csv(os.path.join(BASE_DIR, "batting_consistency_analytics.csv"))
        cls.df_bowl_cons = pd.read_csv(os.path.join(BASE_DIR, "bowling_consistency_analytics.csv"))

    def test_home_away_is_unsupported(self):
        """Verify that Home/Away analysis is marked UNSUPPORTED without outside data."""
        self.assertEqual(self.ha_info["status"], "UNSUPPORTED")
        self.assertIn("lacks native", self.ha_info["reason"])
        self.assertEqual(len(self.ha_info["target_columns_found"]), 0)

    def test_match_impact_traceability_and_bounds(self):
        """Verify Match Impact covers all 344 players with non-causal bounded scores."""
        self.assertEqual(len(self.df_impact), 344)
        self.assertEqual(set(self.df_impact["player_id"]), self.registry_ids)
        self.assertTrue((self.df_impact["composite_contribution_index"] >= 0.0).all())
        self.assertTrue((self.df_impact["composite_contribution_index"] <= 100.0).all())
        # Total POTM awards across players must match total matches (166)
        self.assertEqual(self.df_impact["player_of_match_awards"].sum(), 166)

    def test_allround_ratings_thresholds_and_normalization(self):
        """Verify qualified all-rounders meet dual-thresholds and normalized ranges."""
        self.assertEqual(len(self.df_ar), 74)
        self.assertTrue(set(self.df_ar["player_id"]).issubset(self.registry_ids))
        self.assertTrue((self.df_ar["runs"] >= 100).all())
        self.assertTrue((self.df_ar["wickets"] >= 5).all())
        self.assertTrue((self.df_ar["allrounder_composite_index"] >= 0.0).all())
        self.assertTrue((self.df_ar["allrounder_composite_index"] <= 100.0).all())

        # Verify differential formula
        diffs = (self.df_ar["batting_average"] - self.df_ar["bowling_average"]).round(2)
        np.testing.assert_allclose(self.df_ar["allrounder_differential"].round(2), diffs, atol=0.05)

    def test_batting_consistency_bounds(self):
        """Verify batting consistency metrics for batters with >= 10 innings."""
        self.assertGreaterEqual(len(self.df_bat_cons), 180)
        self.assertTrue((self.df_bat_cons["innings_count"] >= 10).all())
        self.assertTrue(set(self.df_bat_cons["player_id"]).issubset(self.registry_ids))
        self.assertTrue((self.df_bat_cons["coefficient_of_variation"] >= 0.0).all())
        self.assertTrue((self.df_bat_cons["single_digit_failure_pct"] >= 0.0).all())
        self.assertTrue((self.df_bat_cons["single_digit_failure_pct"] <= 100.0).all())

    def test_bowling_consistency_bounds(self):
        """Verify bowling consistency metrics for bowlers with >= 10 innings."""
        self.assertGreaterEqual(len(self.df_bowl_cons), 90)
        self.assertTrue((self.df_bowl_cons["innings_bowled_count"] >= 10).all())
        self.assertTrue(set(self.df_bowl_cons["player_id"]).issubset(self.registry_ids))
        self.assertTrue((self.df_bowl_cons["wicketless_innings_pct"] >= 0.0).all())
        self.assertTrue((self.df_bowl_cons["wicketless_innings_pct"] <= 100.0).all())

    def test_extension_plots_exist(self):
        """Verify all extension plot files exist and are populated."""
        plots_dir = os.path.join(BASE_DIR, "plots")
        for p in ["match_impact_contributions.png", "allround_performance_comparison.png", "batting_consistency_scatter.png"]:
            full_p = os.path.join(plots_dir, p)
            self.assertTrue(os.path.isfile(full_p), f"Missing plot: {full_p}")
            self.assertGreater(os.path.getsize(full_p), 1000)

if __name__ == "__main__":
    unittest.main()
