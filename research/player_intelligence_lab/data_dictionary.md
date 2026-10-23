# PitchIQ Player Intelligence Lab — Data Dictionary & Metric Formulas

## 1. Single Authoritative Data Source
- **Source File**: `C:\Users\Khush\Desktop\TEST\eligible_ball_by_ball.csv`
- **Integrity Rule**: No external datasets, API calls, or auxiliary scorecards are used to calculate, adjust, or enrich any metrics in these tables. All statistics are derived exclusively from the 316,090 deliveries spanning 166 Test matches (2016/17–2021).
- **Population**: Exactly 344 unique participating players (344 batters, 235 bowlers, 235 dual-role / all-rounders, 109 batting-only).

---

## 2. Generated Tables

The feature engineering pipeline generates 4 primary CSV datasets in `research/player_intelligence_lab/`:
1. `batting_features.csv` (344 players)
2. `bowling_features.csv` (235 players)
3. `allrounder_features.csv` (235 dual-role players)
4. `player_features_master.csv` (344 players)

---

## 3. Batting Metrics (`batting_features.csv`)

| Column Name | Data Type | Formula / Origin | Missing Data / Zero Handling | Description |
|---|---|---|---|---|
| `player_id` | String | `striker` column in CSV | Never null | Short player identifier (e.g. `SPD Smith`, `V Kohli`). |
| `full_name` | String | `full name_striker` | If empty, falls back to `player_id` | Full canonical name of the batter. |
| `country` | String | `country_striker` | If empty, "Unknown" | Country representing the batter. |
| `batting_style` | String | `batting style_striker` | If empty, "Unknown" | Stance (e.g. "Right hand Bat", "Left hand Bat"). |
| `playing_role` | String | `playing role_striker` | If empty, "Player" | Player specialty role. |
| `matches` | Integer | Count of distinct `match_id` where player was striker | 0 if unrecorded | Number of unique matches in which batter faced at least one delivery. |
| `innings` | Integer | Count of distinct `(match_id, innings)` combinations | 0 if unrecorded | Number of innings batted. |
| `not_outs` | Integer | `innings - dismissals` | Min 0 | Number of innings where the batter remained undefeated. |
| `runs` | Integer | Sum of `runs_off_bat` | 0 if no runs | Total aggregate Test runs scored off the bat (extras excluded). |
| `balls_faced` | Integer | Count of deliveries where `wides == 0` | 0 if no balls | Deliveries faced. Note: Wides are excluded from balls faced; No-balls are included. |
| `batting_average` | Float (2 dp) | `runs / (innings - not_outs)` | `null` (empty string) if dismissals == 0 | Traditional batting average. Note: undefeated batters (0 dismissals) are explicitly set to null, avoiding division by zero or misleading infinity. |
| `strike_rate` | Float (2 dp) | `(runs / balls_faced) * 100` | `null` (empty string) if balls_faced == 0 | Batting strike rate per 100 balls. |
| `fours` | Integer | Count of deliveries with `runs_off_bat == 4` | 0 | Total boundaries (fours). |
| `sixes` | Integer | Count of deliveries with `runs_off_bat == 6` | 0 | Total sixes. |
| `boundary_runs` | Integer | `(fours * 4) + (sixes * 6)` | 0 | Total runs accumulated via boundaries. |
| `boundary_run_pct` | Float (2 dp) | `(boundary_runs / runs) * 100` | 0.0 if runs == 0 | Percentage of total runs scored in boundaries. |
| `dot_balls_faced` | Integer | Count of balls faced (`wides == 0`) with `runs_off_bat == 0` | 0 | Total dot balls faced. |
| `dot_ball_pct` | Float (2 dp) | `(dot_balls_faced / balls_faced) * 100` | 0.0 if balls_faced == 0 | Dot ball percentage faced. |
| `hundreds` | Integer | Count of innings with aggregate runs >= 100 | 0 | Centuries scored in eligible matches. |
| `fifties` | Integer | Count of innings with aggregate runs in [50, 99] | 0 | Half-centuries scored in eligible matches. |
| `ducks` | Integer | Count of innings with 0 runs where batter was dismissed | 0 | Dismissals for zero runs. |
| `highest_score` | Integer | Max innings runs in a single innings | 0 | Batter's highest individual innings score. |
| `primary_scoring_region` | String | Region with max `runs_off_bat` across all scoring shots | "Unclassified" if no shots classified | Ground zone yielding most runs (e.g. `Cover`, `Mid Wicket`, `Point`, `Third Man`, `Square Leg`, `Fine Leg`, `Mid-On / Long-On`, `Mid-Off / Long-Off`, `Straight`). |
| `primary_region_runs` | Integer | Runs scored in primary scoring region | 0 | Runs scored into primary zone. |
| `primary_region_pct` | Float (2 dp) | `(primary_region_runs / runs) * 100` | 0.0 if runs == 0 | Concentration percentage of primary zone. |
| `cover_runs` | Integer | Sum of `runs_off_bat` classified in Cover | 0 | Runs scored through cover / extra cover. |
| `mid_wicket_runs` | Integer | Sum of `runs_off_bat` classified in Mid Wicket | 0 | Runs scored through mid-wicket / cow corner. |
| `point_runs` | Integer | Sum of `runs_off_bat` classified in Point | 0 | Runs scored through point / gully. |
| `third_man_runs` | Integer | Sum of `runs_off_bat` classified in Third Man | 0 | Runs scored through third man / slips. |
| `square_leg_runs` | Integer | Sum of `runs_off_bat` classified in Square Leg | 0 | Runs scored through square leg. |
| `fine_leg_runs` | Integer | Sum of `runs_off_bat` classified in Fine Leg | 0 | Runs scored through fine leg / leg glance. |
| `mid_on_runs` | Integer | Sum of `runs_off_bat` classified in Mid-On / Long-On | 0 | Runs scored down mid-on / long-on. |
| `mid_off_runs` | Integer | Sum of `runs_off_bat` classified in Mid-Off / Long-Off | 0 | Runs scored down mid-off / long-off. |
| `straight_runs` | Integer | Sum of `runs_off_bat` classified in Straight | 0 | Runs scored straight down the ground. |

---

## 4. Bowling Metrics (`bowling_features.csv`)

| Column Name | Data Type | Formula / Origin | Missing Data / Zero Handling | Description |
|---|---|---|---|---|
| `player_id` | String | `bowler` column in CSV | Never null | Short player identifier. |
| `full_name` | String | `full name_bowler` | If empty, falls back to `player_id` | Full canonical name of the bowler. |
| `country` | String | `country_bowler` | If empty, "Unknown" | Bowler's national team. |
| `bowling_style` | String | `bowling style_bowler` | If empty, "Unknown" | Bowling style (e.g. "Right arm Fast", "Slow Left arm Orthodox"). |
| `playing_role` | String | `playing role_bowler` | If empty, "Bowler" | Bowler role specialty. |
| `matches` | Integer | Count of distinct `match_id` where player bowled | 0 | Matches in which player bowled. |
| `innings` | Integer | Count of distinct `(match_id, innings)` combinations bowled | 0 | Bowling innings. |
| `overs_bowled` | String | Standard notation: `(legal_balls // 6).(legal_balls % 6)` | "0.0" | Standard cricket notation (e.g., `1245.0`, `315.4`, `717.5`). |
| `overs_decimal` | Float (2 dp) | `round(legal_balls / 6.0, 2)` | 0.0 | Mathematical decimal overs for rate calculations. |
| `legal_balls_bowled` | Integer | Count of deliveries where `wides == 0 and noballs == 0` | 0 | Valid deliveries completed. |
| `total_deliveries_bowled`| Integer | Total delivery rows for bowler | 0 | All deliveries including wides and no-balls. |
| `maidens` | Integer | Count of distinct completed 6-ball overs conceding 0 runs | 0 | Maiden overs bowled. |
| `runs_conceded` | Integer | Sum of `runs_off_bat + wides + noballs` | 0 | Bowler runs conceded. Sourced by strictly excluding byes and leg-byes. |
| `wickets` | Integer | Count of dismissals where `wicket == 1` and `wicket_type` in `BOWLER_CREDITED_DISMISSALS` | 0 | Bowler-credited wickets (`caught`, `bowled`, `lbw`, `stumped`, `caught and bowled`, `hit wicket`). Strictly excludes `run out`, `retired hurt`, and `retired not out`. |
| `bowling_average` | Float (2 dp) | `runs_conceded / wickets` | `null` (empty string) if wickets == 0 | Runs conceded per wicket. Bowlers with 0 wickets are set to null (never zero or inf). |
| `economy_rate` | Float (2 dp) | `runs_conceded / (legal_balls_bowled / 6.0)` | `null` (empty string) if legal_balls == 0 | Runs conceded per 6 legal balls. |
| `bowling_strike_rate` | Float (2 dp) | `legal_balls_bowled / wickets` | `null` (empty string) if wickets == 0 | Legal balls bowled per wicket. |
| `wides_conceded` | Integer | Sum of `wides` column | 0 | Wides bowled. |
| `noballs_conceded` | Integer | Sum of `noballs` column | 0 | No-balls bowled. |
| `dot_balls_bowled` | Integer | Deliveries where `runs_off_bat + wides + noballs == 0` | 0 | Dot balls bowled. |
| `bowling_dot_pct` | Float (2 dp) | `(dot_balls_bowled / total_deliveries) * 100` | 0.0 if deliveries == 0 | Percentage of deliveries conceding zero runs. |
| `line_length_accurate_balls` | Integer | Count of balls satisfying accuracy criteria | 0 | Accurate deliveries (evaluated via commentary or fallback). |
| `line_length_accuracy_pct` | Float (2 dp) | `(line_length_accurate_balls / total_deliveries) * 100` | 0.0 if deliveries == 0 | Overall line and length accuracy percentage. |
| `described_balls` | Integer | Balls where `ball_length != ""` or `ball_line != ""` | 0 | Number of balls with commentary line/length metadata. |
| `described_coverage_pct` | Float (2 dp) | `(described_balls / total_deliveries) * 100` | 0.0 | Percentage of deliveries with explicit commentary descriptions. |
| `good_length_balls` | Integer | Deliveries tagged `good length` | 0 | Deliveries landing on good length. |
| `full_length_balls` | Integer | Deliveries tagged `full`, `yorker`, `block hole`, `slot`, `half-volley` | 0 | Deliveries landing on full lengths. |
| `short_length_balls` | Integer | Deliveries tagged `short`, `bouncer`, `back of length`, `half tracker` | 0 | Deliveries landing short / bouncers. |
| `outside_off_balls` | Integer | Deliveries tagged `outside off`, `corridor of uncertainty` | 0 | Deliveries outside off-stump channel. |
| `stumps_line_balls` | Integer | Deliveries tagged `on off stump`, `on middle stump`, `on the stumps`, `on middle and off` | 0 | Deliveries directly attacking stumps. |
| `leg_side_balls` | Integer | Deliveries tagged `on leg stump`, `on middle and leg`, `down the leg`, `outside leg stump`, `leg` | 0 | Deliveries directed at pads or down leg. |

---

## 5. Commentary-Derived Spatial & Accuracy Rules

### 5.1 Batter Scoring Region Classification
For every scoring delivery (`runs_off_bat > 0`), the pipeline classifies ground sector by inspecting `shot_direction` and `shot_played`:
1. **Primary Keyword Sector Mapping**:
   - `Cover`: Contains "cover", "extra cover", "covers", "sweeper cover".
   - `Mid Wicket`: Contains "mid wicket", "midwicket", "cow corner", "through midwicket", "through mid-wicket".
   - `Point`: Contains "point", "gully", "cover-point", "backward point".
   - `Third Man`: Contains "third man", "third-man", "slips", "slip", "behind the wicket".
   - `Square Leg`: Contains "square leg", "backward square", "fine of square".
   - `Fine Leg`: Contains "fine leg", "fine-leg", "leg glance", "glance", "long leg".
   - `Mid-On / Long-On`: Contains "mid-on", "mid on", "long on", "long-on".
   - `Mid-Off / Long-Off`: Contains "mid-off", "mid off", "long off", "long-off".
   - `Straight`: Contains "straight", "down the ground", "past the bowler", "bowler's head".
2. **Shot-Type Fallbacks** (when explicit direction is omitted):
   - `cut`, `dab` -> `Point`
   - `sweep`, `reverse sweep` -> `Square Leg`
   - `pull`, `hook`, `flick`, `clips`, `whips` -> `Mid Wicket`
   - `drive`, `punch` -> `Cover`
   - `edge` -> `Third Man`
3. **Primary Scoring Region Determination**:
   - For each batter, runs across all classified sectors are summed.
   - The region with the maximum total runs becomes `primary_scoring_region`.
   - `primary_region_pct` is `(primary_region_runs / total_runs) * 100`.

### 5.2 Bowler Line & Length Accuracy Determination
- **Described Balls**:
  If either `ball_length` or `ball_line` is present in commentary:
  - Accurate length: `good length`, `full`, `yorker`, `block hole`, `slot`, `half-volley`.
  - Accurate line: `outside off`, `on off stump`, `on middle stump`, `on the stumps`, `on middle and off`, `on middle and leg`, `corridor of uncertainty`.
  - Delivery is marked **Accurate** if length is accurate (or empty) and line is accurate (or empty).
- **Undescribed Balls (User Rule Fallback)**:
  Where commentary omits both line and length:
  - If `total_runs_on_ball == 0` (`runs_off_bat + wides + noballs == 0`), the delivery is counted as **Accurate**.
  - If `total_runs_on_ball > 0`, the delivery is counted as **Inaccurate**.
- **Accuracy Metric**:
  `line_length_accuracy_pct = (line_length_accurate_balls / total_deliveries_bowled) * 100`

---

## 6. Verification & Ground Truth Benchmarks

Calculations in these tables have been verified by automated unit tests (`test_feature_engineering.py`) against known benchmark players in the dataset:
- **SPD Smith**: 28 matches, 49 innings, 2,871 runs, 45 dismissals, Batting Avg **63.80**, 10 centuries, 12 fifties, High Score 239.
- **V Kohli**: 38 matches, 63 innings, 3,281 runs, 59 dismissals, Batting Avg **55.61**, 12 centuries, 11 fifties, High Score 254.
- **PJ Cummins**: 33 matches, 1,245.0 overs, 157 wickets, 3,425 runs conceded, Bowling Avg **21.82**, Economy **2.75**, Strike Rate **47.58**.
- **JJ Bumrah**: 19 matches, 37 innings, 679.1 overs, 83 wickets, 1,835 runs conceded, Bowling Avg **22.11**, Economy **2.70**, Strike Rate **49.10**.
