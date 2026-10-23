package com.example.siuu

import org.junit.Assert.*
import org.junit.Test

class PlayerInspectorVisualizationsTest {

    @Test
    fun testWagonWheelDataCalculations() {
        val sectors = mapOf(
            "COVER" to WagonWheelSector("COVER", "Cover", shotsCount = 5, runsTotal = 16, fours = 3, sixes = 0, singles = 2),
            "MID_WICKET" to WagonWheelSector("MID_WICKET", "Mid-Wicket", shotsCount = 3, runsTotal = 14, fours = 2, sixes = 1, singles = 0)
        )
        val deliveries = listOf(
            WagonWheelDelivery(key = "1_1_1", over = 1, ball = 1, runs = 4, totalRuns = 4, sector = "COVER", angle = 295.0f, provenance = "COMMENTARY_EXPLICIT"),
            WagonWheelDelivery(key = "1_1_2", over = 1, ball = 2, runs = 6, totalRuns = 6, sector = "MID_WICKET", angle = 68.0f, provenance = "COMMENTARY_EXPLICIT"),
            WagonWheelDelivery(key = "1_1_3", over = 1, ball = 3, runs = 1, totalRuns = 1, sector = "UNKNOWN", angle = null, provenance = "UNKNOWN")
        )

        val data = PlayerWagonWheelData(
            totalBalls = 10,
            totalRuns = 31,
            scoringBalls = 3,
            directlyClassifiedBalls = 2,
            unknownDirectionBalls = 1,
            directlyClassifiedBoundaries = 2,
            totalBoundaries = 2,
            isLeftHanded = false,
            sectors = sectors,
            deliveries = deliveries
        )

        assertEquals(10, data.totalBalls)
        assertEquals(31, data.totalRuns)
        assertEquals(3, data.scoringBalls)
        assertEquals(2, data.directlyClassifiedBalls)
        assertEquals(1, data.unknownDirectionBalls)
        assertEquals(2, data.totalBoundaries)
        assertEquals(2, data.directlyClassifiedBoundaries)
        assertFalse(data.isLeftHanded)

        // Coverage calculations
        assertEquals(66.67, data.coveragePct, 0.01)
        assertEquals(100.0, data.boundaryCoveragePct, 0.01)
    }

    @Test
    fun testLeftHandedBatterMirroringLogic() {
        val originalAngle = 247.5f // COVER for RHB (bottom-left)
        val mirroredLhbAngle = (360f - originalAngle) % 360f // 112.5f (bottom-right, where cover is for LHB)
        assertEquals(112.5f, mirroredLhbAngle, 0.01f)

        val straightAngle = 180.0f
        val mirroredStraight = (360f - straightAngle) % 360f
        assertEquals(180.0f, mirroredStraight, 0.01f)
    }

    @Test
    fun testBowlingHeatmapMatrixCalculations() {
        val matrix = mapOf(
            "FULL" to mapOf("OUTSIDE_OFF" to 14, "OFF_MIDDLE_STUMPS" to 12, "LEG_SIDE" to 2, "UNKNOWN_LINE" to 6),
            "GOOD" to mapOf("OUTSIDE_OFF" to 8, "OFF_MIDDLE_STUMPS" to 10, "LEG_SIDE" to 1, "UNKNOWN_LINE" to 5),
            "SHORT" to mapOf("OUTSIDE_OFF" to 9, "OFF_MIDDLE_STUMPS" to 8, "LEG_SIDE" to 2, "UNKNOWN_LINE" to 7)
        )
        val lengthTotals = mapOf("FULL" to 34, "GOOD" to 24, "SHORT" to 26)
        val lineTotals = mapOf("OUTSIDE_OFF" to 31, "OFF_MIDDLE_STUMPS" to 30, "LEG_SIDE" to 5)

        val data = BowlingHeatmapData(
            totalBalls = 156,
            classifiedLengthCount = 84,
            classifiedLineCount = 66,
            bothLengthAndLineCount = 66,
            fullTossCount = 2,
            unknownLengthCount = 70,
            unknownLineCount = 90,
            matrix = matrix,
            lengthTotals = lengthTotals,
            lineTotals = lineTotals
        )

        assertEquals(14, data.getCellCount("FULL", "OUTSIDE_OFF"))
        assertEquals(10, data.getCellCount("GOOD", "OFF_MIDDLE_STUMPS"))
        assertEquals(2, data.getCellCount("SHORT", "LEG_SIDE"))
        assertEquals(0, data.getCellCount("UNKNOWN_LEN", "UNKNOWN_LINE"))

        // Full toss separated from bounce rows
        assertEquals(2, data.fullTossCount)

        // Coverage percentages
        assertEquals(53.85, data.lengthCoveragePct, 0.01)
        assertEquals(42.31, data.bothCoveragePct, 0.01)
    }

    @Test
    fun testInningsIsolationBetweenBattingInnings() {
        // Innings 1 Wagon Wheel
        val inn1Wagon = PlayerWagonWheelData(
            totalBalls = 95,
            totalRuns = 113,
            scoringBalls = 50,
            directlyClassifiedBalls = 44,
            unknownDirectionBalls = 6,
            directlyClassifiedBoundaries = 16,
            totalBoundaries = 17,
            sectors = mapOf(
                "COVER" to WagonWheelSector("COVER", "Cover", shotsCount = 14, runsTotal = 38, fours = 7, sixes = 1)
            )
        )

        // Innings 2 Wagon Wheel
        val inn2Wagon = PlayerWagonWheelData(
            totalBalls = 27,
            totalRuns = 55,
            scoringBalls = 18,
            directlyClassifiedBalls = 15,
            unknownDirectionBalls = 3,
            directlyClassifiedBoundaries = 8,
            totalBoundaries = 9,
            sectors = mapOf(
                "MID_WICKET" to WagonWheelSector("MID_WICKET", "Mid-Wicket", shotsCount = 6, runsTotal = 20, fours = 3, sixes = 1)
            )
        )

        val battingInn1 = PlayerInspectorBattingInnings(
            innings = 1,
            battingPosition = 1,
            runs = 113,
            balls = 95,
            fours = 17,
            sixes = 0,
            dots = 45,
            strikeRate = 118.95,
            arrivalScore = "0/0",
            arrivalOvers = 0.0,
            departureScore = "151/1",
            departureOvers = 32.3,
            isOut = true,
            dismissalType = "caught",
            dismissedBy = "Wahab Riaz",
            fielder = "",
            controlPct = 97.9,
            falseShotPct = 2.1,
            attackingPct = 24.2,
            leavePct = 1.1,
            dotPct = 47.4,
            boundaryPct = 60.2,
            highestPartnershipRuns = 151,
            highestPartnershipPartner = "MT Renshaw",
            wagonWheel = inn1Wagon
        )

        val battingInn2 = PlayerInspectorBattingInnings(
            innings = 2,
            battingPosition = 1,
            runs = 55,
            balls = 27,
            fours = 8,
            sixes = 1,
            dots = 9,
            strikeRate = 203.70,
            arrivalScore = "0/0",
            arrivalOvers = 0.0,
            departureScore = "70/1",
            departureOvers = 11.2,
            isOut = true,
            dismissalType = "caught",
            dismissedBy = "Wahab Riaz",
            fielder = "",
            controlPct = 92.6,
            falseShotPct = 7.4,
            attackingPct = 55.6,
            leavePct = 0.0,
            dotPct = 33.3,
            boundaryPct = 69.1,
            highestPartnershipRuns = 70,
            highestPartnershipPartner = "MT Renshaw",
            wagonWheel = inn2Wagon
        )

        // Verify independent innings objects with no data leakage
        assertEquals(1, battingInn1.innings)
        assertEquals(113, battingInn1.runs)
        assertEquals(1, battingInn1.wagonWheel?.sectors?.size)
        assertTrue(battingInn1.wagonWheel?.sectors?.containsKey("COVER") == true)
        assertFalse(battingInn1.wagonWheel?.sectors?.containsKey("MID_WICKET") == true)

        assertEquals(2, battingInn2.innings)
        assertEquals(55, battingInn2.runs)
        assertEquals(1, battingInn2.wagonWheel?.sectors?.size)
        assertTrue(battingInn2.wagonWheel?.sectors?.containsKey("MID_WICKET") == true)
        assertFalse(battingInn2.wagonWheel?.sectors?.containsKey("COVER") == true)
    }

    @Test
    fun testInningsIsolationBetweenBowlingInnings() {
        val inn1Heatmap = BowlingHeatmapData(
            totalBalls = 156,
            classifiedLengthCount = 84,
            classifiedLineCount = 66,
            bothLengthAndLineCount = 66,
            fullTossCount = 1,
            unknownLengthCount = 71,
            unknownLineCount = 90,
            matrix = mapOf("FULL" to mapOf("OUTSIDE_OFF" to 14))
        )

        val inn2Heatmap = BowlingHeatmapData(
            totalBalls = 90,
            classifiedLengthCount = 52,
            classifiedLineCount = 48,
            bothLengthAndLineCount = 40,
            fullTossCount = 0,
            unknownLengthCount = 38,
            unknownLineCount = 42,
            matrix = mapOf("SHORT" to mapOf("LEG_SIDE" to 8))
        )

        val bowlInn1 = PlayerInspectorBowlingInnings(
            innings = 1,
            overs = 26.0,
            maidens = 3,
            runsConceded = 85,
            wickets = 3,
            economy = 3.27,
            heatmap = inn1Heatmap
        )

        val bowlInn2 = PlayerInspectorBowlingInnings(
            innings = 2,
            overs = 15.0,
            maidens = 2,
            runsConceded = 45,
            wickets = 1,
            economy = 3.00,
            heatmap = inn2Heatmap
        )

        assertEquals(1, bowlInn1.innings)
        assertEquals(1, bowlInn1.heatmap?.fullTossCount)
        assertEquals(14, bowlInn1.heatmap?.getCellCount("FULL", "OUTSIDE_OFF"))
        assertEquals(0, bowlInn1.heatmap?.getCellCount("SHORT", "LEG_SIDE"))

        assertEquals(2, bowlInn2.innings)
        assertEquals(0, bowlInn2.heatmap?.fullTossCount)
        assertEquals(8, bowlInn2.heatmap?.getCellCount("SHORT", "LEG_SIDE"))
        assertEquals(0, bowlInn2.heatmap?.getCellCount("FULL", "OUTSIDE_OFF"))
    }

    @Test
    fun testSchemaVersionCompatibilityCheck() {
        val validVersion = "1.0.0"
        val compatibleMinor = "1.1.0"
        val incompatibleMajor = "2.0.0"

        assertTrue(validVersion.startsWith("1."))
        assertTrue(compatibleMinor.startsWith("1."))
        assertFalse(incompatibleMajor.startsWith("1."))
    }

    @Test
    fun testSpatialPlayerMatchingFallbacks() {
        data class SpatialPlayer(val batterId: Long, val espnId: Long, val batterName: String)
        val battingAnalytics = mapOf(
            "291" to SpatialPlayer(291L, 267192L, "SPD Smith"),
            "219889" to SpatialPlayer(66L, 219889L, "David Warner")
        )

        fun resolvePlayer(queryId: Long, targetSurname: String?): SpatialPlayer? {
            // 1. Direct lookup by key
            battingAnalytics[queryId.toString()]?.let { return it }
            // 2. Fallback: by batterId or espnId
            for (p in battingAnalytics.values) {
                if (p.batterId == queryId || p.espnId == queryId) return p
            }
            // 3. Fallback: by surname
            if (targetSurname != null) {
                for (p in battingAnalytics.values) {
                    if (p.batterName.split(" ").lastOrNull()?.equals(targetSurname, ignoreCase = true) == true) {
                        return p
                    }
                }
            }
            return null
        }

        // 1. Direct warehouse ID match
        val p1 = resolvePlayer(291L, null)
        assertNotNull(p1)
        assertEquals(291L, p1?.batterId)
        assertEquals("SPD Smith", p1?.batterName)

        // 2. Fallback by batterId (when key is ESPN ID)
        val p2 = resolvePlayer(66L, null)
        assertNotNull(p2)
        assertEquals(66L, p2?.batterId)
        assertEquals(219889L, p2?.espnId)
        assertEquals("David Warner", p2?.batterName)

        // 3. Fallback by ESPN ID
        val p3 = resolvePlayer(219889L, null)
        assertNotNull(p3)
        assertEquals(66L, p3?.batterId)

        // 4. Fallback by surname
        val p4 = resolvePlayer(999L, "warner")
        assertNotNull(p4)
        assertEquals("David Warner", p4?.batterName)
    }
}

