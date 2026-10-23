# PitchIQ Player Intelligence Lab — Multi-Dimensional Player Rankings Report

This document records the multi-dimensional player rankings derived strictly from:
`C:\Users\Khush\Desktop\TEST\eligible_ball_by_ball.csv`

## 1. Summary of Rankings & Sensitivity Robustness

| Ranking System | Population | Minimum Eligibility Criteria | Baseline vs Alternative A ($\rho$) | Baseline vs Alternative B ($\rho$) | Top-Ranked Player Under Stated Method |
|---|---|---|---|---|---|
| **Best-Performing Batters** | 127 | Innings $\ge 15$, Balls Faced $\ge 500$ | **0.9903** *(Efficiency-Heavy)* | **0.9777** *(Volume-Heavy)* | **KS Williamson** (Score: 87.14) |
| **Best-Performing Bowlers** | 79 | Legal Balls $\ge 600$ (100 ov), Wkts $\ge 15$ | **0.9082** *(Strike-Heavy)* | **0.8888** *(Control-Heavy)* | **AR Patel** (Score: 96.70) |
| **All-Rounders** | 47 | Runs $\ge 200$, Bowler Wkts $\ge 10$ | **0.9534** *(Batting-Biased)* | **0.9103** *(Bowling-Biased)* | **KA Jamieson** (Score: 87.89) |
| **Match Contribution** | 203 | Matches Played $\ge 5$ | **0.9588** *(POTM-Heavy)* | **0.9454** *(Pure Share)* | **KA Jamieson** (Score: 63.13) |
| **Overall Player Ranking** | 135 | Matches Played $\ge 10$ | **0.9482** *(Specialist-Heavy)* | **0.9891** *(Contribution-Heavy)* | **M Labuschagne** (Score: 65.65) |

## 2. Generated Ranking Tables in `research/player_intelligence_lab/`
- `ranking_batters.csv` (127 players)
- `ranking_bowlers.csv` (79 players)
- `ranking_allrounders.csv` (47 players)
- `ranking_match_contribution.csv` (203 players)
- `ranking_overall.csv` (135 players)
- `ranking_sensitivity_analysis.json`

## 3. Generated Plots in `research/player_intelligence_lab/plots/`
- `rankings_batters_top15.png`
- `rankings_bowlers_top15.png`
- `rankings_allrounders_top15.png`
- `rankings_match_contribution_top15.png`
- `rankings_overall_top15.png`
- `rankings_sensitivity_correlation.png`

## 4. Test Verification
- `test_player_rankings.py`: 5 tests passing.
- Total Lab suite: 36/36 unit tests passing.
