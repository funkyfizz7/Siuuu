# Forensic Image Quality Summary Report - Top 50 PitchIQ Players

## Overall
- **Top 50 images audited**: 50 total player photos evaluated from `app/src/main/assets/players/`
- **Data Source**: `player_photo_priority.csv` (Rank 1 to Rank 50)

## Resolution Breakdown
- **A Grade (>= 1024 px)**: 0 (0%)
- **B Grade (512-1023 px)**: 0 (0%)
- **C Grade (256-511 px)**: 50 (100%)
- **D Grade (< 256 px)**: 0 (0%)

| Quality Metric | Compliant | At Risk | Audit Finding |
| :--- | :--- | :--- | :--- |
| **Aspect Ratio (1:1 Square)** | 50 / 50 (100%) | 0 / 50 (0%) | All 50 images are 1:1 square. |
| **Resolution (>= 512 px)** | 0 / 50 (0%) | 50 / 50 (100%) | All 50 images are capped at 256x256 px (Grade C). |
| **File Size (>= 15 KB)** | 0 / 50 (0%) | 50 / 50 (100%) | All 50 images are below 15 KB threshold (too_small = TRUE). |
| **Authentic Photo Asset** | 11 / 50 (22%) | 39 / 50 (78%) | 39 players use identical 766B dummy placeholder WebPs. |

## Problem Images (50 / 50)
List of every image meeting problem criteria (quality_grade <= C OR too_small = TRUE OR possible_upscaled = TRUE):

| Rank | Player ID | Name | Country | Image File | Format | Dimensions | File Size | Grade | Too Small | Upscaled / Dummy |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | 135 | Joseph Edward Root | England | `joe_root.jpg` | JPEG | 256x256 | 5.86 KB | C | TRUE | FALSE (Real Photo) |
| 2 | 31 | Benjamin Andrew Stokes | England | `ben_stokes.jpg` | JPEG | 256x256 | 6.99 KB | C | TRUE | FALSE (Real Photo) |
| 3 | 326 | Virat Kohli | India | `virat_kohli.jpg` | JPEG | 256x256 | 8.25 KB | C | TRUE | FALSE (Real Photo) |
| 4 | 292 | Stuart Christopher John Broad | England | `stuart_broad.jpg` | JPEG | 256x256 | 6.14 KB | C | TRUE | FALSE (Real Photo) |
| 5 | 85 | Frank Dimuth Madushanka Karunaratne | Sri Lanka | `dimuth_karunaratne.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 6 | 68 | Dean Elgar | South Africa | `dean_elgar.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 7 | 222 | Nathan Michael Lyon | Australia | `nathan_lyon.jpg` | JPEG | 256x256 | 5.06 KB | C | TRUE | FALSE (Real Photo) |
| 8 | 139 | Kagiso Rabada | South Africa | `kagiso_rabada.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 9 | 12 | Ajinkya Madhukar Rahane | India | `ajinkya_rahane.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 10 | 72 | Dickwella Patabendige Dilantha Niroshan Dickwella | Sri Lanka | `niroshan_dickwella.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 11 | 52 | Cheteshwar Arvind Pujara | India | `cheteshwar_pujara.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 12 | 253 | Ravichandran Ashwin | India | `ravichandran_ashwin.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 13 | 30 | Balapuwaduge Kusal Gimhan Mendis | Sri Lanka | `kusal_mendis.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 14 | 116 | James Michael Anderson | England | `james_anderson.jpg` | JPEG | 256x256 | 7.49 KB | C | TRUE | FALSE (Real Photo) |
| 15 | 147 | Keshav Athmanand Maharaj | South Africa | `keshav_maharaj.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 16 | 141 | Kane Stuart Williamson | New Zealand | `kane_williamson.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 17 | 236 | Patrick James Cummins | Australia | `pat_cummins.jpg` | JPEG | 256x256 | 6.14 KB | C | TRUE | FALSE (Real Photo) |
| 18 | 248 | Quinton de Kock | South Africa | `quinton_de_kock.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 19 | 313 | Timothy Grant Southee | New Zealand | `tim_southee.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 20 | 252 | Ranasinghe Arachchige Suranga Lakmal | Sri Lanka | `suranga_lakmal.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 21 | 291 | Steven Peter Devereux Smith | Australia | `steve_smith.jpg` | JPEG | 256x256 | 6.61 KB | C | TRUE | FALSE (Real Photo) |
| 22 | 151 | Kraigg Clairmonte Brathwaite | West Indies | `kraigg_brathwaite.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 23 | 254 | Ravindrasinh Anirudhsinh Jadeja | India | `ravindra_jadeja.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 24 | 84 | Francois du Plessis | South Africa | `faf_du_plessis.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 25 | 277 | Shannon Terry Gabriel | West Indies | `shannon_gabriel.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 26 | 119 | Jason Omar Holder | West Indies | `jason_holder.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 27 | 170 | Mahawaduge Dilruwan Kamalaneth Perera | Sri Lanka | `dilruwan_perera.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 28 | 71 | Dhananjaya Maduranga de Silva | Sri Lanka | `dhananjaya_de_silva.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 29 | 131 | Jonathan Marc Bairstow | England | `jonny_bairstow.jpg` | JPEG | 256x256 | 6.13 KB | C | TRUE | FALSE (Real Photo) |
| 30 | 164 | Lokuge Dinesh Chandimal | Sri Lanka | `dinesh_chandimal.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 31 | 190 | Mitchell Aaron Starc | Australia | `mitchell_starc.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 32 | 319 | Trent Alexander Boult | New Zealand | `trent_boult.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 33 | 260 | Roston Lamar Chase | West Indies | `roston_chase.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 34 | 311 | Thomas William Maxwell Latham | New Zealand | `tom_latham.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 35 | 341 | Yasir Shah | Pakistan | `yasir_shah.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 36 | 146 | Kemar Andre Jamal Roach | West Indies | `kemar_roach.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 37 | 100 | Henry Michael Nicholls | New Zealand | `hm_nicholls.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 38 | 324 | Vernon Darryl Philander | South Africa | `vernon_philander.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 39 | 193 | Moeen Munir Ali | England | `mm_ali.jpg` | JPEG | 256x256 | 5.69 KB | C | TRUE | FALSE (Real Photo) |
| 40 | 211 | Mohammed Shami Ahmed | India | `mohammed_shami.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 41 | 167 | Luteru Ross Poutoa Lote Taylor | New Zealand | `ross_taylor.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 42 | 29 | Azhar Ali | Pakistan | `azhar_ali.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 43 | 225 | Neil Wagner | New Zealand | `neil_wagner.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 44 | 303 | Temba Bavuma | South Africa | `temba_bavuma.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 45 | 186 | Mehidy Hasan Miraz | Bangladesh | `mehidy_hasan_miraz.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 46 | 134 | Joseph Charles Buttler | England | `jos_buttler.jpg` | JPEG | 256x256 | 5.29 KB | C | TRUE | FALSE (Real Photo) |
| 47 | 109 | Ishant Sharma | India | `i_sharma.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 48 | 196 | Mohammad Babar Azam | Pakistan | `babar_azam.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |
| 49 | 66 | David Andrew Warner | Australia | `david_warner.jpg` | JPEG | 256x256 | 7.26 KB | C | TRUE | FALSE (Real Photo) |
| 50 | 213 | Mominul Haque | Bangladesh | `mominul_haque.webp` | WEBP | 256x256 | 0.75 KB | C | TRUE | TRUE (Generic Dummy WebP) |

## Replacement Priority Ranking

### HIGH PRIORITY (50 Images)
All 50 images qualify as **HIGH PRIORITY** because their resolution is < 512 px (256x256 px), their file size is < 15 KB, and 39 of them are generic silhouette placeholders.

#### Category 1: Missing Real Portraits (39 Players with Generic 766B Dummy Placeholders)
These players do not have real face photos in the assets directory and display the identical default silhouette:
- **#5 Frank Dimuth Madushanka Karunaratne** (ID: 85, Sri Lanka) -> File: `dimuth_karunaratne.webp`
- **#6 Dean Elgar** (ID: 68, South Africa) -> File: `dean_elgar.webp`
- **#8 Kagiso Rabada** (ID: 139, South Africa) -> File: `kagiso_rabada.webp`
- **#9 Ajinkya Madhukar Rahane** (ID: 12, India) -> File: `ajinkya_rahane.webp`
- **#10 Dickwella Patabendige Dilantha Niroshan Dickwella** (ID: 72, Sri Lanka) -> File: `niroshan_dickwella.webp`
- **#11 Cheteshwar Arvind Pujara** (ID: 52, India) -> File: `cheteshwar_pujara.webp`
- **#12 Ravichandran Ashwin** (ID: 253, India) -> File: `ravichandran_ashwin.webp`
- **#13 Balapuwaduge Kusal Gimhan Mendis** (ID: 30, Sri Lanka) -> File: `kusal_mendis.webp`
- **#15 Keshav Athmanand Maharaj** (ID: 147, South Africa) -> File: `keshav_maharaj.webp`
- **#16 Kane Stuart Williamson** (ID: 141, New Zealand) -> File: `kane_williamson.webp`
- **#18 Quinton de Kock** (ID: 248, South Africa) -> File: `quinton_de_kock.webp`
- **#19 Timothy Grant Southee** (ID: 313, New Zealand) -> File: `tim_southee.webp`
- **#20 Ranasinghe Arachchige Suranga Lakmal** (ID: 252, Sri Lanka) -> File: `suranga_lakmal.webp`
- **#22 Kraigg Clairmonte Brathwaite** (ID: 151, West Indies) -> File: `kraigg_brathwaite.webp`
- **#23 Ravindrasinh Anirudhsinh Jadeja** (ID: 254, India) -> File: `ravindra_jadeja.webp`
- **#24 Francois du Plessis** (ID: 84, South Africa) -> File: `faf_du_plessis.webp`
- **#25 Shannon Terry Gabriel** (ID: 277, West Indies) -> File: `shannon_gabriel.webp`
- **#26 Jason Omar Holder** (ID: 119, West Indies) -> File: `jason_holder.webp`
- **#27 Mahawaduge Dilruwan Kamalaneth Perera** (ID: 170, Sri Lanka) -> File: `dilruwan_perera.webp`
- **#28 Dhananjaya Maduranga de Silva** (ID: 71, Sri Lanka) -> File: `dhananjaya_de_silva.webp`
- **#30 Lokuge Dinesh Chandimal** (ID: 164, Sri Lanka) -> File: `dinesh_chandimal.webp`
- **#31 Mitchell Aaron Starc** (ID: 190, Australia) -> File: `mitchell_starc.webp`
- **#32 Trent Alexander Boult** (ID: 319, New Zealand) -> File: `trent_boult.webp`
- **#33 Roston Lamar Chase** (ID: 260, West Indies) -> File: `roston_chase.webp`
- **#34 Thomas William Maxwell Latham** (ID: 311, New Zealand) -> File: `tom_latham.webp`
- **#35 Yasir Shah** (ID: 341, Pakistan) -> File: `yasir_shah.webp`
- **#36 Kemar Andre Jamal Roach** (ID: 146, West Indies) -> File: `kemar_roach.webp`
- **#37 Henry Michael Nicholls** (ID: 100, New Zealand) -> File: `hm_nicholls.webp`
- **#38 Vernon Darryl Philander** (ID: 324, South Africa) -> File: `vernon_philander.webp`
- **#40 Mohammed Shami Ahmed** (ID: 211, India) -> File: `mohammed_shami.webp`
- **#41 Luteru Ross Poutoa Lote Taylor** (ID: 167, New Zealand) -> File: `ross_taylor.webp`
- **#42 Azhar Ali** (ID: 29, Pakistan) -> File: `azhar_ali.webp`
- **#43 Neil Wagner** (ID: 225, New Zealand) -> File: `neil_wagner.webp`
- **#44 Temba Bavuma** (ID: 303, South Africa) -> File: `temba_bavuma.webp`
- **#45 Mehidy Hasan Miraz** (ID: 186, Bangladesh) -> File: `mehidy_hasan_miraz.webp`
- **#47 Ishant Sharma** (ID: 109, India) -> File: `i_sharma.webp`
- **#48 Mohammad Babar Azam** (ID: 196, Pakistan) -> File: `babar_azam.webp`
- **#50 Mominul Haque** (ID: 213, Bangladesh) -> File: `mominul_haque.webp`

#### Category 2: Low-Resolution Real Photos (11 Players with 256x256 JPEGs)
These players have actual face photos, but their resolution is only 256x256 px and file size is 5-8 KB, causing blur on modern mobile displays:
- **#1 Joseph Edward Root** (ID: 135, England) -> File: `joe_root.jpg` (256x256 px, 5.86 KB)
- **#2 Benjamin Andrew Stokes** (ID: 31, England) -> File: `ben_stokes.jpg` (256x256 px, 6.99 KB)
- **#3 Virat Kohli** (ID: 326, India) -> File: `virat_kohli.jpg` (256x256 px, 8.25 KB)
- **#4 Stuart Christopher John Broad** (ID: 292, England) -> File: `stuart_broad.jpg` (256x256 px, 6.14 KB)
- **#7 Nathan Michael Lyon** (ID: 222, Australia) -> File: `nathan_lyon.jpg` (256x256 px, 5.06 KB)
- **#14 James Michael Anderson** (ID: 116, England) -> File: `james_anderson.jpg` (256x256 px, 7.49 KB)
- **#17 Patrick James Cummins** (ID: 236, Australia) -> File: `pat_cummins.jpg` (256x256 px, 6.14 KB)
- **#21 Steven Peter Devereux Smith** (ID: 291, Australia) -> File: `steve_smith.jpg` (256x256 px, 6.61 KB)
- **#29 Jonathan Marc Bairstow** (ID: 131, England) -> File: `jonny_bairstow.jpg` (256x256 px, 6.13 KB)
- **#39 Moeen Munir Ali** (ID: 193, England) -> File: `mm_ali.jpg` (256x256 px, 5.69 KB)
- **#46 Joseph Charles Buttler** (ID: 134, England) -> File: `jos_buttler.jpg` (256x256 px, 5.29 KB)
- **#49 David Andrew Warner** (ID: 66, Australia) -> File: `david_warner.jpg` (256x256 px, 7.26 KB)

### MEDIUM PRIORITY (0 Images)
- **None** (No intermediate resolution assets 512-800 px exist).

### LOW PRIORITY (0 Images)
- **None** (No Grade A assets >= 1024 px currently exist in Top 50).

---
## Root Cause Analysis & Recommendations
1. **Why Images Appear Blurry**:
   - **Viewport Scaling**: Modern Android phones (FHD+/QHD+ at 400-560 dpi) render player cards at 100-300dp, which requires 300-900 physical pixels. Upscaling 256x256 images causes bilinear interpolation blur and blockiness.
   - **Placeholder Duplication**: 78% of the Top 50 players are mapped to a 766-byte dummy WebP with no facial likeness.
2. **Recommended Action Plan**:
   - Source crisp **1024x1024 px (1:1 square)** high-resolution headshots for all 50 players.
   - Export to **WebP (Quality 90-95%)** targeting **60-150 KB** per player.
   - Prioritize replacing Category 1 (the 39 missing player photos) first, followed by Category 2 (upgrading the 11 low-res photos).
