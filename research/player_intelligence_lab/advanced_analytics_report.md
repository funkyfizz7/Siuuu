# PitchIQ Player Intelligence Lab — Advanced Analytics Report

This document reports the execution of the four requested analytical extensions using exclusively:
`C:\Users\Khush\Desktop\TEST\eligible_ball_by_ball.csv`

## 1. Feasibility & Verification Summary

| Analysis | Status | Sample Size | Deterministic Reason & Schema Finding |
|---|---|---|---|
| **Home / Away** | **UNSUPPORTED** | 0 | The source CSV contains no host country or home team columns. Stadium names in `venue` do not provide native home/away relationships. Neutral venues exist (e.g. Dubai, Abu Dhabi). As instructed, no outside lookup or nationality inference was applied. |
| **Match Impact** | **SUPPORTED** | 344 players | Formulated as an observational contribution index (team run share, team wicket share, POTM awards). Avoids unverified causal claims. |
| **All-Round Performance** | **SUPPORTED** | 74 players | Filtered by dual eligibility ($\ge 100$ runs and $\ge 5$ wickets). Metrics normalized using directional min-max scaling ($0 - 100$). |
| **Player Consistency** | **SUPPORTED** | 184 batters, 94 bowlers | Derived from reconstructed innings. Measures standard deviation, coefficient of variation ($CV$), failure rate, and IQR for players with $\ge 10$ innings. |

## 2. Generated Datasets in `research/player_intelligence_lab/`
- `home_away_verification.json`
- `match_impact_analytics.csv`
- `allround_performance_ratings.csv`
- `batting_consistency_analytics.csv`
- `bowling_consistency_analytics.csv`

## 3. Generated Visualizations in `research/player_intelligence_lab/plots/`
- `match_impact_contributions.png`
- `allround_performance_comparison.png`
- `batting_consistency_scatter.png`

## 4. Verification Test Suite
- `test_advanced_analytics.py`: 6 tests passing.
- Total lab suite: 31/31 unit tests passing.
