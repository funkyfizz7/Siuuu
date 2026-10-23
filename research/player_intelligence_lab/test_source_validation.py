"""
Unit Tests for PitchIQ Player Intelligence Lab — Source Validation Module
"""

import unittest
import os
import sys
import json

# Ensure local module directory is in python path
current_dir = os.path.dirname(os.path.abspath(__file__))
if current_dir not in sys.path:
    sys.path.insert(0, current_dir)

from source_validation import (
    DEFAULT_SOURCE_PATH,
    SourceValidationModule,
    ValidationSummary,
    BOWLER_CREDITED_DISMISSALS,
    NON_BOWLER_DISMISSALS
)

class TestSourceValidationModule(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls.validator = SourceValidationModule(DEFAULT_SOURCE_PATH)
        cls.summary = cls.validator.validate()
        cls.players = cls.validator.players

    def test_source_file_presence_and_size(self):
        self.assertTrue(self.summary.source_file_exists)
        self.assertGreater(self.summary.source_file_size_bytes, 300 * 1024 * 1024)
        self.assertEqual(self.summary.source_path, DEFAULT_SOURCE_PATH)

    def test_exact_schema_columns(self):
        self.assertEqual(self.summary.total_columns, 58)
        expected_cols = [
            "match_id", "season", "start_date", "venue", "innings", "ball",
            "batting_team", "bowling_team", "striker", "non_striker", "bowler",
            "ball_length", "ball_line", "shot_played", "shot_direction",
            "runs_off_bat", "extras", "wides", "noballs", "byes", "legbyes",
            "penalty", "wicket_type", "player_dismissed", "wicket",
            "striker balls faced", "total striker runs", "country_striker",
            "batting style_striker", "country_bowler", "bowling style_bowler"
        ]
        for col in expected_cols:
            self.assertIn(col, self.summary.column_names)

    def test_source_deliveries_and_matches_count(self):
        # Exactly 316,090 deliveries across exactly 166 matches
        self.assertEqual(self.summary.total_rows, 316090)
        self.assertEqual(self.summary.unique_matches_count, 166)
        self.assertEqual(self.summary.date_range_start, "2017-01-02")
        self.assertEqual(self.summary.date_range_end, "2021-03-29")

    def test_delivery_key_uniqueness(self):
        # 0 duplicate deliveries on composite primary key (match_id, innings, ball)
        self.assertEqual(self.summary.duplicate_delivery_keys_count, 0)
        self.assertEqual(self.summary.invalid_innings_rows_count, 0)
        self.assertEqual(self.summary.invalid_runs_rows_count, 0)
        self.assertEqual(self.summary.invalid_wickets_rows_count, 0)
        self.assertEqual(self.summary.invalid_ball_numbers_count, 0)

    def test_player_population_and_roles(self):
        # Exactly 344 players represented
        self.assertEqual(self.summary.total_unique_players, 344)
        self.assertEqual(len(self.players), 344)
        
        # 344 batters faced balls
        self.assertEqual(self.summary.total_batters, 344)
        # 235 bowlers bowled deliveries
        self.assertEqual(self.summary.total_bowlers, 235)
        # Exactly 109 players batted only and never bowled
        self.assertEqual(self.summary.total_batting_only, 109)
        # Exactly 235 players performed both roles
        self.assertEqual(self.summary.total_both_roles, 235)
        self.assertEqual(self.summary.total_batting_only + self.summary.total_both_roles, 344)

    def test_traceable_name_mapping(self):
        # Every short name must map to a valid non-empty full name and country
        for p_name, p in self.players.items():
            self.assertTrue(len(p.short_name) > 0)
            self.assertTrue(len(p.full_name) > 0)
            self.assertTrue(len(p.country) > 0)
            self.assertNotEqual(p.country, "Unknown")
            
        # Verify specific player name tracings
        self.assertEqual(self.players["SPD Smith"].full_name, "Steven Peter Devereux Smith")
        self.assertEqual(self.players["SPD Smith"].country, "Australia")
        self.assertTrue(self.players["SPD Smith"].is_both) # Smith has bowled in Tests
        
        self.assertEqual(self.players["V Kohli"].full_name, "Virat Kohli")
        self.assertEqual(self.players["V Kohli"].country, "India")
        self.assertTrue(self.players["V Kohli"].is_both) # Kohli bowled 25 deliveries in 4 matches
        
        self.assertEqual(self.players["AM Rahane"].full_name, "Ajinkya Madhukar Rahane")
        self.assertEqual(self.players["AM Rahane"].country, "India")
        self.assertTrue(self.players["AM Rahane"].is_batting_only) # Rahane never bowled in this window

    def test_missing_vs_zero_distinction(self):
        # Descriptors have substantial missing rates
        self.assertGreater(self.summary.missing_value_percentages["ball_length"], 25.0)
        self.assertGreater(self.summary.missing_value_percentages["ball_line"], 30.0)
        self.assertGreater(self.summary.missing_value_percentages["shot_played"], 30.0)
        self.assertGreater(self.summary.missing_value_percentages["shot_direction"], 45.0)

        # Core scoring fields have zero missing values
        self.assertEqual(self.summary.missing_value_counts["runs_off_bat"], 0)
        self.assertEqual(self.summary.missing_value_counts["extras"], 0)
        self.assertEqual(self.summary.missing_value_counts["wicket"], 0)

    def test_bowler_vs_team_dismissals(self):
        # Bowler credited vs non-bowler wickets
        self.assertEqual(self.summary.total_wickets_in_file, 5506)
        self.assertEqual(self.summary.total_bowler_credited_wickets, 5346)
        self.assertEqual(self.summary.total_non_bowler_wickets, 160)
        self.assertEqual(self.summary.wicket_counts_by_type.get("run out", 0), 146)
        self.assertEqual(self.summary.wicket_counts_by_type.get("retired hurt", 0), 13)

    def test_zero_excluded_records(self):
        self.assertEqual(self.summary.excluded_records_count, 0)
        self.assertEqual(len(self.summary.excluded_reasons), 0)

if __name__ == "__main__":
    unittest.main()
