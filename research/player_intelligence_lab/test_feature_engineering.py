"""
Unit Tests for Feature Engineering Module
Tests calculations against established ground truths from eligible_ball_by_ball.csv
"""

import os
import unittest
import csv
from research.player_intelligence_lab.feature_engineering import (
    classify_scoring_region,
    is_accurate_delivery,
    balls_to_overs_notation,
    FeatureEngineeringPipeline,
    DEFAULT_SOURCE_PATH,
    BOWLER_CREDITED_DISMISSALS
)

class TestFeatureEngineering(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.pipeline = FeatureEngineeringPipeline(DEFAULT_SOURCE_PATH)
        cls.pipeline.extract_and_aggregate()
        cls.bat_rows = {r["player_id"]: r for r in cls.pipeline.generate_batting_table()}
        cls.bowl_rows = {r["player_id"]: r for r in cls.pipeline.generate_bowling_table()}
        cls.master_rows = {r["player_id"]: r for r in cls.pipeline.generate_master_table(
            list(cls.bat_rows.values()), list(cls.bowl_rows.values())
        )}

    def test_population_counts(self):
        """Verify exact population numbers match source validation."""
        self.assertEqual(len(self.bat_rows), 344)
        self.assertEqual(len(self.bowl_rows), 235)
        self.assertEqual(len(self.master_rows), 344)

    def test_overs_notation(self):
        """Verify cricket overs notation: X.0 through X.5."""
        self.assertEqual(balls_to_overs_notation(0), "0.0")
        self.assertEqual(balls_to_overs_notation(1), "0.1")
        self.assertEqual(balls_to_overs_notation(5), "0.5")
        self.assertEqual(balls_to_overs_notation(6), "1.0")
        self.assertEqual(balls_to_overs_notation(1894), "315.4")
        self.assertEqual(balls_to_overs_notation(7470), "1245.0")

    def test_benchmark_batter_smith(self):
        """Verify benchmark metrics for SPD Smith."""
        self.assertIn("SPD Smith", self.bat_rows)
        smith = self.bat_rows["SPD Smith"]
        self.assertEqual(smith["matches"], 28)
        self.assertEqual(smith["innings"], 49)
        self.assertEqual(smith["runs"], 2871)
        self.assertEqual(smith["not_outs"], 4)
        # 49 innings - 4 not outs = 45 dismissals. 2871 / 45 = 63.80
        self.assertEqual(smith["batting_average"], 63.8)
        self.assertEqual(smith["hundreds"], 10)
        self.assertEqual(smith["fifties"], 12)
        self.assertEqual(smith["highest_score"], 239)
        self.assertIn(smith["primary_scoring_region"], ["Mid Wicket", "Cover", "Point"])

    def test_benchmark_batter_kohli(self):
        """Verify benchmark metrics for V Kohli."""
        self.assertIn("V Kohli", self.bat_rows)
        kohli = self.bat_rows["V Kohli"]
        self.assertEqual(kohli["matches"], 38)
        self.assertEqual(kohli["innings"], 63)
        self.assertEqual(kohli["runs"], 3281)
        self.assertEqual(kohli["not_outs"], 4)
        # 63 innings - 4 not outs = 59 dismissals. 3281 / 59 = 55.61
        self.assertEqual(kohli["batting_average"], 55.61)
        self.assertEqual(kohli["hundreds"], 12)
        self.assertEqual(kohli["fifties"], 11)
        self.assertEqual(kohli["highest_score"], 254)
        self.assertIn(kohli["primary_scoring_region"], ["Cover", "Mid Wicket", "Point"])

    def test_benchmark_bowler_cummins(self):
        """Verify benchmark metrics for PJ Cummins."""
        self.assertIn("PJ Cummins", self.bowl_rows)
        cummins = self.bowl_rows["PJ Cummins"]
        self.assertEqual(cummins["matches"], 33)
        self.assertEqual(cummins["wickets"], 157)
        self.assertEqual(cummins["legal_balls_bowled"], 7470)
        self.assertEqual(cummins["overs_bowled"], "1245.0")
        self.assertEqual(cummins["runs_conceded"], 3425)
        # 3425 / 157 = 21.82
        self.assertEqual(cummins["bowling_average"], 21.82)
        # 3425 / 1245.0 = 2.75
        self.assertEqual(cummins["economy_rate"], 2.75)
        # 7470 / 157 = 47.58
        self.assertEqual(cummins["bowling_strike_rate"], 47.58)

    def test_benchmark_bowler_bumrah(self):
        """Verify benchmark metrics for JJ Bumrah."""
        self.assertIn("JJ Bumrah", self.bowl_rows)
        bumrah = self.bowl_rows["JJ Bumrah"]
        self.assertEqual(bumrah["matches"], 19)
        self.assertEqual(bumrah["innings"], 37)
        self.assertEqual(bumrah["wickets"], 83)
        self.assertEqual(bumrah["legal_balls_bowled"], 4075)
        self.assertEqual(bumrah["overs_bowled"], "679.1")
        self.assertEqual(bumrah["runs_conceded"], 1835)
        # 1835 / 83 = 22.11
        self.assertEqual(bumrah["bowling_average"], 22.11)
        # 1835 / (4075/6) = 2.70
        self.assertEqual(bumrah["economy_rate"], 2.7)
        # 4075 / 83 = 49.10
        self.assertEqual(bumrah["bowling_strike_rate"], 49.1)

    def test_bowler_accuracy_fallback_logic(self):
        """Test accuracy determination when described vs when missing."""
        # 1. Described good length + outside off -> accurate regardless of runs
        is_acc, desc = is_accurate_delivery("good length", "outside off", 4)
        self.assertTrue(is_acc)
        self.assertTrue(desc)

        # 2. Described short + outside off -> inaccurate regardless of runs
        is_acc, desc = is_accurate_delivery("short", "outside off", 0)
        self.assertFalse(is_acc)
        self.assertTrue(desc)

        # 3. Undescribed line and length with 0 runs conceded -> accurate (fallback rule)
        is_acc, desc = is_accurate_delivery("", "", 0)
        self.assertTrue(is_acc)
        self.assertFalse(desc)

        # 4. Undescribed line and length with >0 runs conceded -> inaccurate (fallback rule)
        is_acc, desc = is_accurate_delivery("", "", 1)
        self.assertFalse(is_acc)
        self.assertFalse(desc)

    def test_scoring_region_classification(self):
        """Test classification of scoring region based on commentary text."""
        self.assertEqual(classify_scoring_region("through covers", "cover drive"), "Cover")
        self.assertEqual(classify_scoring_region("backward point", "cut shot"), "Point")
        self.assertEqual(classify_scoring_region("deep mid-wicket", "pull"), "Mid Wicket")
        self.assertEqual(classify_scoring_region("fine leg", "glance"), "Fine Leg")
        self.assertEqual(classify_scoring_region("third man", "steer"), "Third Man")
        self.assertEqual(classify_scoring_region("straight down ground", "push"), "Straight")

    def test_missing_data_not_zero(self):
        """Verify that undefined rates are represented as None, not 0.0."""
        # Check bowlers with 0 wickets
        zero_wkt_bowlers = [b for b in self.bowl_rows.values() if b["wickets"] == 0]
        self.assertGreater(len(zero_wkt_bowlers), 0)
        for b in zero_wkt_bowlers:
            self.assertIsNone(b["bowling_average"], f"{b['player_id']} with 0 wickets should have None average")
            self.assertIsNone(b["bowling_strike_rate"], f"{b['player_id']} with 0 wickets should have None strike rate")

        # Check batters with 0 dismissals (undefeated)
        undefeated_batters = [b for b in self.bat_rows.values() if (b["innings"] - b["not_outs"]) == 0]
        self.assertGreater(len(undefeated_batters), 0)
        for b in undefeated_batters:
            self.assertIsNone(b["batting_average"], f"{b['player_id']} with 0 dismissals should have None average")

    def test_dismissal_types_integrity(self):
        """Verify non-bowler dismissals are excluded from bowler wickets."""
        # Check that 'run out', 'retired hurt', 'retired not out' are not in credited dismissals
        self.assertNotIn("run out", BOWLER_CREDITED_DISMISSALS)
        self.assertNotIn("retired hurt", BOWLER_CREDITED_DISMISSALS)
        self.assertNotIn("retired not out", BOWLER_CREDITED_DISMISSALS)

if __name__ == "__main__":
    unittest.main()
