"""
Unit Tests for PitchIQ Player Spatial Analytics Extraction Pipeline
"""

import unittest
import json
import tempfile
import os
import csv
from build_player_spatial_analytics import (
    classify_shot_direction,
    classify_bowling_length,
    classify_bowling_line,
    compute_batter_runs,
    parse_innings_label,
    process_match_commentary,
    SECTOR_ANGLES
)

class TestPlayerSpatialAnalytics(unittest.TestCase):

    def test_explicit_shot_directions(self):
        """Test that explicit cricket field phrases map to correct sectors."""
        test_cases = [
            ("creams it through extra cover for four", "COVER"),
            ("punched past cover for a couple", "COVER"),
            ("guided fine to third man", "THIRD_MAN"),
            ("steered backward of point for a single", "POINT"),
            ("thick outside edge through gully", "POINT"),
            ("driven straight down the ground, past the bowler", "STRAIGHT"),
            ("worked gently past mid-on for one", "MID_ON"),
            ("drills it past mid-off to the fence", "MID_OFF"),
            ("heaved away through mid-wicket for a boundary", "MID_WICKET"),
            ("clipped fine off his hips towards fine leg", "FINE_LEG"),
            ("tucked behind square leg for a brace", "SQUARE_LEG"),
        ]
        for comm, expected_sector in test_cases:
            sector, angle, prov = classify_shot_direction(comm)
            self.assertEqual(sector, expected_sector, f"Failed for '{comm}'")
            self.assertEqual(prov, "COMMENTARY_EXPLICIT")
            self.assertEqual(angle, SECTOR_ANGLES[expected_sector])

    def test_ambiguous_and_false_positive_directions(self):
        """Test that non-directional phrases remain UNKNOWN."""
        test_cases = [
            "at that point, Pakistan were in total control",
            "he covers the line of the ball and blocks",
            "just a slip of the tongue from the commentator",
            "solid defensive block back to the bowler",
            "leaves it alone outside off stump",
            "good point made by the analyst",
            "",
            None
        ]
        for comm in test_cases:
            sector, angle, prov = classify_shot_direction(comm)
            self.assertEqual(sector, "UNKNOWN", f"Should be UNKNOWN for '{comm}'")
            self.assertIsNone(angle)
            self.assertEqual(prov, "UNKNOWN")

    def test_runs_alone_do_not_determine_direction(self):
        """Test that boundaries with no directional phrase remain UNKNOWN."""
        comm = "four runs scored off an edge. 142 kph"
        sector, angle, prov = classify_shot_direction(comm)
        self.assertEqual(sector, "UNKNOWN")
        self.assertIsNone(angle)

    def test_batter_runs_vs_total_runs(self):
        """Test extras logic: byes, leg byes, wides, no-balls."""
        # Clean ball: 4 runs off bat
        self.assertEqual(compute_batter_runs(total_runs=4, wides=0, noballs=0, byes=0, legbyes=0), 4)
        # Wide 1
        self.assertEqual(compute_batter_runs(total_runs=1, wides=1, noballs=0, byes=0, legbyes=0), 0)
        # Wide 5
        self.assertEqual(compute_batter_runs(total_runs=5, wides=5, noballs=0, byes=0, legbyes=0), 0)
        # Leg byes 4
        self.assertEqual(compute_batter_runs(total_runs=4, wides=0, noballs=0, byes=0, legbyes=4), 0)
        # Byes 2
        self.assertEqual(compute_batter_runs(total_runs=2, wides=0, noballs=0, byes=2, legbyes=0), 0)
        # No ball 1, no runs off bat
        self.assertEqual(compute_batter_runs(total_runs=1, wides=0, noballs=1, byes=0, legbyes=0), 0)
        # No ball 1 + 4 off bat = 5 total runs
        self.assertEqual(compute_batter_runs(total_runs=5, wides=0, noballs=1, byes=0, legbyes=0), 4)

    def test_bowling_length_categories(self):
        """Test Full, Good, Short, and Full Toss classifications."""
        # Full categories (<6m or explicit)
        self.assertEqual(classify_bowling_length("fired in a toe-crushing yorker")[0], "FULL")
        self.assertEqual(classify_bowling_length("dug out from the blockhole")[0], "FULL")
        self.assertEqual(classify_bowling_length("half-volley driven nicely")[0], "FULL")
        self.assertEqual(classify_bowling_length("fuller delivery on off")[0], "FULL")
        
        # Good length (6m - 8m)
        self.assertEqual(classify_bowling_length("landed on a good length")[0], "GOOD")
        
        # Short (>8m)
        self.assertEqual(classify_bowling_length("back of a length delivery, rises quickly")[0], "SHORT")
        self.assertEqual(classify_bowling_length("short of a length outside off")[0], "SHORT")
        self.assertEqual(classify_bowling_length("sharp bouncer over the helmet")[0], "SHORT")
        
        # Full Toss tracked separately
        self.assertEqual(classify_bowling_length("dipping full toss on middle")[0], "FULL_TOSS")
        
        # Ambiguous "length ball" remains UNKNOWN as requested
        self.assertEqual(classify_bowling_length("prods forward to a length ball")[0], "UNKNOWN")
        self.assertEqual(classify_bowling_length("tight length, defended")[0], "UNKNOWN")

    def test_bowling_line_categories(self):
        """Test lateral line classification."""
        self.assertEqual(classify_bowling_line("wide outside off, left alone")[0], "OUTSIDE_OFF")
        self.assertEqual(classify_bowling_line("in the channel outside off")[0], "OUTSIDE_OFF")
        self.assertEqual(classify_bowling_line("straight at off stump")[0], "OFF_MIDDLE_STUMPS")
        self.assertEqual(classify_bowling_line("angles into middle and leg")[0], "LEG_SIDE")
        self.assertEqual(classify_bowling_line("straying on the pads")[0], "LEG_SIDE")
        self.assertEqual(classify_bowling_line("sliding down leg")[0], "LEG_SIDE")
        self.assertEqual(classify_bowling_line("good bowling, batsman beaten")[0], "UNKNOWN")

    def test_innings_parsing(self):
        """Test innings label conversion."""
        self.assertEqual(parse_innings_label("1st innings"), 1)
        self.assertEqual(parse_innings_label("2nd innings"), 2)
        self.assertEqual(parse_innings_label("3rd innings"), 3)
        self.assertEqual(parse_innings_label("4th innings"), 4)

    def test_innings_separation_and_deduplication(self):
        """Test pipeline preserves innings separation and handles re-bowled/duplicate balls."""
        csv_rows = [
            # Header
            ["Innings", "Over_number", "Over_ball", "Batsman_id", "Batsman_name", "Bowler_id", "Bowler_name", "Total_Runs_on_delivery", "Over_wide", "Over_no_Ball", "Over_byes", "Over_leg_byes", "Commentary"],
            # Innings 1: Ball 1.1 - 4 runs through cover
            ["1st innings", "1", "1", "100", "Batter A", "200", "Bowler X", "4", "0", "0", "0", "0", "full delivery, driven through cover for four"],
            # Innings 1: Ball 1.2 - duplicate over_ball (e.g. re-bowled)
            ["1st innings", "1", "2", "100", "Batter A", "200", "Bowler X", "1", "1", "0", "0", "0", "wide outside off"],
            ["1st innings", "1", "2", "100", "Batter A", "200", "Bowler X", "0", "0", "0", "0", "0", "good length around off, blocked"],
            # Innings 2: Ball 1.1 - Same batter in second innings (must not merge)
            ["2nd innings", "1", "1", "100", "Batter A", "200", "Bowler X", "6", "0", "0", "0", "0", "short ball, hooked over fine leg for six"],
        ]
        
        with tempfile.NamedTemporaryFile("w", suffix=".csv", delete=False, newline="", encoding="utf-8") as tf:
            writer = csv.writer(tf)
            writer.writerows(csv_rows)
            temp_path = tf.name
            
        try:
            result = process_match_commentary(temp_path, match_id=999999)
            
            # Check separate innings for Batter A
            bat_record = result["battingAnalytics"]["100"]
            self.assertIn("1", bat_record["innings"])
            self.assertIn("2", bat_record["innings"])
            
            inn1 = bat_record["innings"]["1"]
            inn2 = bat_record["innings"]["2"]
            
            # Innings 1: 3 balls (1.1, 1.2 wide, 1.2 legal)
            self.assertEqual(inn1["totalBalls"], 3)
            self.assertEqual(inn1["totalRuns"], 4) # 4 off bat, wide doesn't go to batter
            self.assertEqual(inn1["sectors"]["COVER"]["fours"], 1)
            
            # Innings 2: 1 ball (1.1)
            self.assertEqual(inn2["totalBalls"], 1)
            self.assertEqual(inn2["totalRuns"], 6)
            self.assertEqual(inn2["sectors"]["FINE_LEG"]["sixes"], 1)
            
            # Check deduplication metadata
            self.assertEqual(result["metadata"]["totalDeliveries"], 4)
            self.assertEqual(result["metadata"]["uniqueDeliveries"], 3)
            self.assertEqual(result["metadata"]["duplicateDeliveriesDisambiguated"], 1)
            
            # Check bowler innings
            bowl_record = result["bowlingAnalytics"]["200"]
            self.assertIn("1", bowl_record["innings"])
            self.assertIn("2", bowl_record["innings"])
            
            inn1_bowl = bowl_record["innings"]["1"]
            self.assertEqual(inn1_bowl["classifiedLengthCount"], 2) # FULL and GOOD
            self.assertEqual(inn1_bowl["lengthTotals"]["FULL"], 1)
            self.assertEqual(inn1_bowl["lengthTotals"]["GOOD"], 1)
            self.assertEqual(inn1_bowl["classifiedLineCount"], 2) # OUTSIDE_OFF and OFF_MIDDLE_STUMPS
            
        finally:
            if os.path.exists(temp_path):
                os.remove(temp_path)

    def test_player_resolver_resolution(self):
        """Test resolver mapping from commentary names and ESPN IDs to warehouse IDs."""
        resolver_func = lambda name, eid: (291, "SPD Smith") if "smith" in name.lower() else (int(eid), name)
        csv_rows = [
            ["Innings", "Over_number", "Over_ball", "Batsman_id", "Batsman_name", "Bowler_id", "Bowler_name", "Total_Runs_on_delivery", "Over_wide", "Over_no_Ball", "Over_byes", "Over_leg_byes", "Commentary"],
            ["1st innings", "1", "1", "267192", "Steven Smith", "10617", "Stuart Broad", "4", "0", "0", "0", "0", "driven through cover for four"],
        ]
        with tempfile.NamedTemporaryFile("w", suffix=".csv", delete=False, newline="", encoding="utf-8") as tf:
            writer = csv.writer(tf)
            writer.writerows(csv_rows)
            temp_path = tf.name

        try:
            result = process_match_commentary(temp_path, match_id=1152846, resolver_func=resolver_func)
            self.assertIn("291", result["battingAnalytics"])
            bat = result["battingAnalytics"]["291"]
            self.assertEqual(bat["batterId"], 291)
            self.assertEqual(bat["espnId"], 267192)
            self.assertEqual(bat["batterName"], "SPD Smith")
        finally:
            if os.path.exists(temp_path):
                os.remove(temp_path)

if __name__ == "__main__":
    unittest.main()

