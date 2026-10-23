package com.example.siuu

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MatchHighlightsRepositoryTest {

    private lateinit var repository: DefaultMatchHighlightsRepository

    @Before
    fun setUp() {
        repository = DefaultMatchHighlightsRepository()
    }

    /**
     * Test 1: Correct Match-ID Lookup
     */
    @Test
    fun testCorrectMatchIdLookup() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152846",
                "youtube_video_id" to "K7JlaKMRccI",
                "highlights_url" to "https://www.youtube.com/watch?v=K7JlaKMRccI",
                "thumbnail_url" to "https://i.ytimg.com/vi/K7JlaKMRccI/hqdefault.jpg",
                "source_name" to "England & Wales Cricket Board",
                "verified" to "TRUE",
                "verification_date" to "2026-10-09",
                "day1_url" to "https://www.youtube.com/watch?v=K7JlaKMRccI"
            )
        )
        repository.loadFromLines(rows)

        val result = repository.getMatchHighlights(1152846L)
        assertNotNull("Record should be found for match ID 1152846", result)
        assertEquals(1152846L, result!!.matchId)
        assertEquals("K7JlaKMRccI", result.youtubeVideoId)
    }

    /**
     * Test 2: Correct CSV Field Parsing
     */
    @Test
    fun testCorrectCsvParsing() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152846",
                "youtube_video_id" to "K7JlaKMRccI",
                "highlights_url" to "https://www.youtube.com/watch?v=K7JlaKMRccI",
                "thumbnail_url" to "https://i.ytimg.com/vi/K7JlaKMRccI/hqdefault.jpg",
                "source_name" to "England & Wales Cricket Board",
                "verified" to "TRUE",
                "verification_date" to "2026-10-09",
                "day1_url" to "https://www.youtube.com/watch?v=K7JlaKMRccI",
                "day2_url" to "https://www.youtube.com/watch?v=day2vid",
                "day3_url" to "https://www.youtube.com/watch?v=day3vid",
                "day4_url" to "https://www.youtube.com/watch?v=day4vid",
                "day5_url" to "https://www.youtube.com/watch?v=day5vid"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152846L)
        assertNotNull(item)
        assertEquals("England & Wales Cricket Board", item!!.sourceName)
        assertTrue(item.verified)
        assertEquals("2026-10-09", item.verificationDate)
        assertEquals("https://www.youtube.com/watch?v=K7JlaKMRccI", item.highlightsUrl)
        assertEquals("https://i.ytimg.com/vi/K7JlaKMRccI/hqdefault.jpg", item.thumbnailUrl)
        assertEquals("https://www.youtube.com/watch?v=K7JlaKMRccI", item.day1Url)
        assertEquals("https://www.youtube.com/watch?v=day2vid", item.day2Url)
        assertEquals("https://www.youtube.com/watch?v=day3vid", item.day3Url)
        assertEquals("https://www.youtube.com/watch?v=day4vid", item.day4Url)
        assertEquals("https://www.youtube.com/watch?v=day5vid", item.day5Url)
    }

    /**
     * Test 3: Unverified Links Rejected
     */
    @Test
    fun testUnverifiedLinksRejected() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152847",
                "youtube_video_id" to "unverifiedId123",
                "highlights_url" to "https://www.youtube.com/watch?v=unverifiedId123",
                "thumbnail_url" to "https://i.ytimg.com/vi/unverifiedId123/hqdefault.jpg",
                "source_name" to "Unverified Channel",
                "verified" to "FALSE",
                "verification_date" to ""
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152847L)
        assertNotNull(item)
        assertFalse("Unverified record must have verified = false", item!!.verified)
        assertNull("Unverified record must NOT produce resolvedPlayUrl", item.resolvedPlayUrl)
    }

    /**
     * Test 4: Malformed and Non-HTTP URLs Rejected
     */
    @Test
    fun testMalformedNonHttpUrlsRejected() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152848",
                "youtube_video_id" to "",
                "highlights_url" to "javascript:alert('malicious')",
                "thumbnail_url" to "ftp://example.com/thumb.jpg",
                "source_name" to "Bad Source",
                "verified" to "TRUE",
                "day1_url" to "data:text/html,bad"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152848L)
        assertNotNull(item)
        assertNull("Non-HTTP highlights URL should be rejected", item!!.highlightsUrl)
        assertNull("Non-HTTP thumbnail URL should be rejected", item.thumbnailUrl)
        assertNull("Non-HTTP day1 URL should be rejected", item.day1Url)
        assertNull("Resolved play URL should be null for invalid URL", item.resolvedPlayUrl)
    }

    /**
     * Test 5: Missing Registry Records Return Null
     */
    @Test
    fun testMissingRegistryRecordsReturnNull() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152846",
                "youtube_video_id" to "K7JlaKMRccI",
                "verified" to "TRUE"
            )
        )
        repository.loadFromLines(rows)

        val nonExistent = repository.getMatchHighlights(9999999L)
        assertNull("Missing match ID must return null", nonExistent)
    }

    /**
     * Test 6: Duplicate Records Handled Deterministically
     */
    @Test
    fun testDuplicateRecordsHandledDeterministically() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152846",
                "youtube_video_id" to "FirstVideoId",
                "source_name" to "First Source",
                "verified" to "TRUE"
            ),
            mapOf(
                "match_id" to "1152846",
                "youtube_video_id" to "SecondVideoId",
                "source_name" to "Second Source",
                "verified" to "TRUE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152846L)
        assertNotNull(item)
        assertEquals("FirstVideoId", item!!.youtubeVideoId)
        assertEquals("First Source", item.sourceName)
    }

    /**
     * Test 7: YouTube Video ID Canonical URL Construction
     */
    @Test
    fun testYoutubeVideoIdCanonicalUrlConstruction() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152846",
                "youtube_video_id" to "K7JlaKMRccI",
                "highlights_url" to "https://youtu.be/K7JlaKMRccI?si=trackingParam",
                "verified" to "TRUE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152846L)
        assertNotNull(item)
        assertEquals(
            "Canonical YouTube URL should be constructed",
            "https://www.youtube.com/watch?v=K7JlaKMRccI",
            item!!.resolvedPlayUrl
        )
    }

    /**
     * Test 8: General External URL Fallback When No YouTube ID
     */
    @Test
    fun testGeneralExternalUrlFallback() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152849",
                "youtube_video_id" to "",
                "highlights_url" to "https://www.ecb.co.uk/video/official-highlights-1152849",
                "verified" to "TRUE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152849L)
        assertNotNull(item)
        assertEquals(
            "Should fallback to valid HTTP highlightsUrl",
            "https://www.ecb.co.uk/video/official-highlights-1152849",
            item!!.resolvedPlayUrl
        )
    }

    /**
     * Test 9: Missing Thumbnail Fallback
     */
    @Test
    fun testMissingThumbnailFallback() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152846",
                "youtube_video_id" to "K7JlaKMRccI",
                "thumbnail_url" to "",
                "verified" to "TRUE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152846L)
        assertNotNull(item)
        assertEquals(
            "Missing thumbnail should construct YouTube hqdefault thumbnail URL",
            "https://i.ytimg.com/vi/K7JlaKMRccI/hqdefault.jpg",
            item!!.resolvedThumbnailUrl
        )
    }

    /**
     * Test 10: Missing Daily URLs Omitted Cleanly
     */
    @Test
    fun testMissingDailyUrlsOmittedCleanly() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152846",
                "youtube_video_id" to "K7JlaKMRccI",
                "verified" to "TRUE",
                "day1_url" to "https://www.youtube.com/watch?v=K7JlaKMRccI",
                "day2_url" to "",
                "day3_url" to "",
                "day4_url" to "",
                "day5_url" to ""
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152846L)
        assertNotNull(item)
        assertNotNull("day1_url should be present", item!!.day1Url)
        assertNull("day2_url should be null", item.day2Url)
        assertNull("day3_url should be null", item.day3Url)
        assertNull("day4_url should be null", item.day4Url)
        assertNull("day5_url should be null", item.day5Url)
    }

    /**
     * Test 11: Five-Day Highlights Resolution & Thumbnail Generation
     */
    @Test
    fun testFiveDayHighlightsResolution() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152846",
                "youtube_video_id" to "K7JlaKMRccI",
                "highlights_url" to "https://www.youtube.com/watch?v=K7JlaKMRccI",
                "thumbnail_url" to "https://i.ytimg.com/vi/K7JlaKMRccI/hqdefault.jpg",
                "source_name" to "England & Wales Cricket Board",
                "verified" to "TRUE",
                "day1_url" to "https://www.youtube.com/watch?v=K7JlaKMRccI",
                "day2_url" to "https://www.youtube.com/watch?v=IdFp3PCQNHw",
                "day3_url" to "https://www.youtube.com/watch?v=XcKF0JLy_HM",
                "day4_url" to "https://www.youtube.com/watch?v=VZUhNtaUNWw",
                "day5_url" to "https://www.youtube.com/watch?v=rDg0vUwA0dY",
                "day1_verified" to "TRUE",
                "day2_verified" to "TRUE",
                "day3_verified" to "TRUE",
                "day4_verified" to "TRUE",
                "day5_verified" to "TRUE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152846L)
        assertNotNull(item)
        for (day in 1..5) {
            assertTrue("Day $day should be available", item!!.isDayAvailable(day))
            assertNotNull("Day $day should have playable URL", item.getPlayUrlForDay(day))
            assertNotNull("Day $day should have thumbnail URL", item.getThumbnailUrlForDay(day))
        }
        assertEquals("https://www.youtube.com/watch?v=IdFp3PCQNHw", item!!.getPlayUrlForDay(2))
        assertEquals("https://i.ytimg.com/vi/IdFp3PCQNHw/hqdefault.jpg", item.getThumbnailUrlForDay(2))
        assertEquals("https://www.youtube.com/watch?v=rDg0vUwA0dY", item.getPlayUrlForDay(5))
    }

    /**
     * Test 12: Unverified Days Rejected & Day Isolation Preserved
     */
    @Test
    fun testUnverifiedDayRejectedAndIsolated() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152846",
                "youtube_video_id" to "K7JlaKMRccI",
                "verified" to "TRUE",
                "day1_url" to "https://www.youtube.com/watch?v=K7JlaKMRccI",
                "day2_url" to "https://www.youtube.com/watch?v=unverifiedDay2",
                "day1_verified" to "TRUE",
                "day2_verified" to "FALSE",
                "day3_verified" to "FALSE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152846L)
        assertNotNull(item)
        assertTrue("Day 1 should be available", item!!.isDayAvailable(1))
        assertFalse("Day 2 should NOT be available as it is unverified", item.isDayAvailable(2))
        assertNull("Day 2 should return null play URL", item.getPlayUrlForDay(2))
        assertNull("Day 3 should return null play URL", item.getPlayUrlForDay(3))
        assertNotEquals("Day 2 must not fall back to Day 1 URL", item.getPlayUrlForDay(1), item.getPlayUrlForDay(2))
    }

    /**
     * Test 13: Ashes 2019 2nd Test (Match 1152847) 5-Day Highlights
     */
    @Test
    fun testAshes2019SecondTestHighlights() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152847",
                "youtube_video_id" to "5TLFK4Q0Evk",
                "highlights_url" to "https://www.youtube.com/watch?v=5TLFK4Q0Evk",
                "thumbnail_url" to "https://i.ytimg.com/vi/5TLFK4Q0Evk/hqdefault.jpg",
                "source_name" to "England & Wales Cricket Board",
                "verified" to "TRUE",
                "verification_date" to "2026-10-17",
                "day1_url" to "https://www.youtube.com/watch?v=5TLFK4Q0Evk",
                "day2_url" to "https://www.youtube.com/watch?v=5TLFK4Q0Evk",
                "day3_url" to "https://www.youtube.com/watch?v=RBP_-u357DA",
                "day4_url" to "https://www.youtube.com/watch?v=zuvKjgRFrxI",
                "day5_url" to "https://www.youtube.com/watch?v=tvydxOmK6nw",
                "day1_verified" to "TRUE",
                "day2_verified" to "TRUE",
                "day3_verified" to "TRUE",
                "day4_verified" to "TRUE",
                "day5_verified" to "TRUE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152847L)
        assertNotNull(item)
        assertEquals("England & Wales Cricket Board", item!!.sourceName)
        assertTrue(item.verified)
        for (day in 1..5) {
            assertTrue("Day $day should be available", item.isDayAvailable(day))
            assertNotNull("Day $day should have playable URL", item.getPlayUrlForDay(day))
            assertNotNull("Day $day should have thumbnail URL", item.getThumbnailUrlForDay(day))
        }
        assertEquals("https://www.youtube.com/watch?v=5TLFK4Q0Evk", item.getPlayUrlForDay(1))
        assertEquals("https://www.youtube.com/watch?v=5TLFK4Q0Evk", item.getPlayUrlForDay(2))
        assertEquals("https://www.youtube.com/watch?v=RBP_-u357DA", item.getPlayUrlForDay(3))
        assertEquals("https://www.youtube.com/watch?v=zuvKjgRFrxI", item.getPlayUrlForDay(4))
        assertEquals("https://www.youtube.com/watch?v=tvydxOmK6nw", item.getPlayUrlForDay(5))
        assertEquals("https://i.ytimg.com/vi/5TLFK4Q0Evk/hqdefault.jpg", item.getThumbnailUrlForDay(1))
        assertEquals("https://i.ytimg.com/vi/5TLFK4Q0Evk/hqdefault.jpg", item.getThumbnailUrlForDay(2))
        assertEquals("https://i.ytimg.com/vi/RBP_-u357DA/hqdefault.jpg", item.getThumbnailUrlForDay(3))
        assertEquals("https://i.ytimg.com/vi/zuvKjgRFrxI/hqdefault.jpg", item.getThumbnailUrlForDay(4))
        assertEquals("https://i.ytimg.com/vi/tvydxOmK6nw/hqdefault.jpg", item.getThumbnailUrlForDay(5))
    }

    /**
     * Test 14: Ashes 2019 3rd Test (Match 1152848 - Headingley)
     */
    @Test
    fun testAshes2019ThirdTestHighlights() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152848",
                "youtube_video_id" to "79ejrYmAowY",
                "highlights_url" to "https://www.youtube.com/watch?v=79ejrYmAowY",
                "thumbnail_url" to "https://i.ytimg.com/vi/79ejrYmAowY/hqdefault.jpg",
                "source_name" to "England & Wales Cricket Board",
                "verified" to "TRUE",
                "verification_date" to "2026-10-17",
                "day1_url" to "https://www.youtube.com/watch?v=79ejrYmAowY",
                "day2_url" to "https://www.youtube.com/watch?v=IlydfhxjAaA",
                "day3_url" to "https://www.youtube.com/watch?v=RZysFgJ8B_4",
                "day4_url" to "https://www.youtube.com/watch?v=HrIew7-4isI",
                "day5_url" to "",
                "day1_verified" to "TRUE",
                "day2_verified" to "TRUE",
                "day3_verified" to "TRUE",
                "day4_verified" to "TRUE",
                "day5_verified" to "FALSE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152848L)
        assertNotNull(item)
        assertEquals("England & Wales Cricket Board", item!!.sourceName)
        assertTrue(item.verified)
        for (day in 1..4) {
            assertTrue("Day $day should be available", item.isDayAvailable(day))
            assertNotNull(item.getPlayUrlForDay(day))
        }
        assertFalse("Day 5 was not played and should be unavailable", item.isDayAvailable(5))
        assertNull(item.getPlayUrlForDay(5))
        assertEquals("https://www.youtube.com/watch?v=HrIew7-4isI", item.getPlayUrlForDay(4))
    }

    /**
     * Test 15: Ashes 2019 4th Test (Match 1152849 - Old Trafford)
     */
    @Test
    fun testAshes2019FourthTestHighlights() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152849",
                "youtube_video_id" to "SaylSiV49Z4",
                "highlights_url" to "https://www.youtube.com/watch?v=SaylSiV49Z4",
                "thumbnail_url" to "https://i.ytimg.com/vi/SaylSiV49Z4/hqdefault.jpg",
                "source_name" to "England & Wales Cricket Board",
                "verified" to "TRUE",
                "verification_date" to "2026-10-17",
                "day1_url" to "https://www.youtube.com/watch?v=SaylSiV49Z4",
                "day2_url" to "https://www.youtube.com/watch?v=DUxbfz3daXQ",
                "day3_url" to "https://www.youtube.com/watch?v=G0fP8Huk6Ts",
                "day4_url" to "https://www.youtube.com/watch?v=K-6qmtYJYtg",
                "day5_url" to "https://www.youtube.com/watch?v=PWzn9EZ5Tlw",
                "day1_verified" to "TRUE",
                "day2_verified" to "TRUE",
                "day3_verified" to "TRUE",
                "day4_verified" to "TRUE",
                "day5_verified" to "TRUE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152849L)
        assertNotNull(item)
        assertTrue(item!!.verified)
        for (day in 1..5) {
            assertTrue("Day $day should be available", item.isDayAvailable(day))
            assertNotNull(item.getPlayUrlForDay(day))
        }
        assertEquals("https://www.youtube.com/watch?v=PWzn9EZ5Tlw", item.getPlayUrlForDay(5))
    }

    /**
     * Test 16: Ashes 2019 5th Test (Match 1152850 - The Oval)
     */
    @Test
    fun testAshes2019FifthTestHighlights() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1152850",
                "youtube_video_id" to "DywwOoipmoQ",
                "highlights_url" to "https://www.youtube.com/watch?v=DywwOoipmoQ",
                "thumbnail_url" to "https://i.ytimg.com/vi/DywwOoipmoQ/hqdefault.jpg",
                "source_name" to "England & Wales Cricket Board",
                "verified" to "TRUE",
                "verification_date" to "2026-10-17",
                "day1_url" to "https://www.youtube.com/watch?v=DywwOoipmoQ",
                "day2_url" to "https://www.youtube.com/watch?v=HERIUM2PbkY",
                "day3_url" to "https://www.youtube.com/watch?v=RhxVVlp05j4",
                "day4_url" to "https://www.youtube.com/watch?v=-k4b4Qw4T14",
                "day5_url" to "",
                "day1_verified" to "TRUE",
                "day2_verified" to "TRUE",
                "day3_verified" to "TRUE",
                "day4_verified" to "TRUE",
                "day5_verified" to "FALSE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1152850L)
        assertNotNull(item)
        assertTrue(item!!.verified)
        for (day in 1..4) {
            assertTrue("Day $day should be available", item.isDayAvailable(day))
            assertNotNull(item.getPlayUrlForDay(day))
        }
        assertFalse("Day 5 was not played and should be unavailable", item.isDayAvailable(5))
        assertNull(item.getPlayUrlForDay(5))
        assertEquals("https://www.youtube.com/watch?v=-k4b4Qw4T14", item.getPlayUrlForDay(4))
    }

    /**
     * Test 17: BGT 2020/21 1st Test (Match 1223869 - Adelaide Oval)
     */
    @Test
    fun testBgt2020FirstTestHighlights() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1223869",
                "youtube_video_id" to "4oTfQoj_Jrc",
                "highlights_url" to "https://www.youtube.com/watch?v=4oTfQoj_Jrc",
                "thumbnail_url" to "https://i.ytimg.com/vi/4oTfQoj_Jrc/hqdefault.jpg",
                "source_name" to "cricket.com.au",
                "verified" to "TRUE",
                "verification_date" to "2026-10-17",
                "day1_url" to "https://www.youtube.com/watch?v=4oTfQoj_Jrc",
                "day2_url" to "https://www.youtube.com/watch?v=CoY2-2uTK-Q",
                "day3_url" to "https://www.youtube.com/watch?v=6igvHGsoaQI",
                "day4_url" to "",
                "day5_url" to "",
                "day1_verified" to "TRUE",
                "day2_verified" to "TRUE",
                "day3_verified" to "TRUE",
                "day4_verified" to "FALSE",
                "day5_verified" to "FALSE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1223869L)
        assertNotNull(item)
        assertEquals("cricket.com.au", item!!.sourceName)
        assertTrue(item.verified)
        for (day in 1..3) {
            assertTrue("Day $day should be available", item.isDayAvailable(day))
            assertNotNull(item.getPlayUrlForDay(day))
        }
        assertFalse("Day 4 should be unavailable", item.isDayAvailable(4))
        assertFalse("Day 5 should be unavailable", item.isDayAvailable(5))
    }

    /**
     * Test 18: BGT 2020/21 2nd Test (Match 1223870 - Melbourne Cricket Ground)
     */
    @Test
    fun testBgt2020SecondTestHighlights() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1223870",
                "youtube_video_id" to "v2bAEek6nc4",
                "highlights_url" to "https://www.youtube.com/watch?v=v2bAEek6nc4",
                "thumbnail_url" to "https://i.ytimg.com/vi/v2bAEek6nc4/hqdefault.jpg",
                "source_name" to "cricket.com.au",
                "verified" to "TRUE",
                "verification_date" to "2026-10-17",
                "day1_url" to "https://www.youtube.com/watch?v=v2bAEek6nc4",
                "day2_url" to "https://www.youtube.com/watch?v=Fy7BQEuXDYc",
                "day3_url" to "https://www.youtube.com/watch?v=mllJ49QIIVs",
                "day4_url" to "https://www.youtube.com/watch?v=W2RT3YIPSYU",
                "day5_url" to "",
                "day1_verified" to "TRUE",
                "day2_verified" to "TRUE",
                "day3_verified" to "TRUE",
                "day4_verified" to "TRUE",
                "day5_verified" to "FALSE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1223870L)
        assertNotNull(item)
        assertEquals("cricket.com.au", item!!.sourceName)
        assertTrue(item.verified)
        for (day in 1..4) {
            assertTrue("Day $day should be available", item.isDayAvailable(day))
            assertNotNull(item.getPlayUrlForDay(day))
        }
        assertFalse("Day 5 should be unavailable", item.isDayAvailable(5))
    }

    /**
     * Test 19: BGT 2020/21 3rd Test (Match 1223871 - Sydney Cricket Ground)
     */
    @Test
    fun testBgt2020ThirdTestHighlights() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1223871",
                "youtube_video_id" to "BtF-3sseK3Q",
                "highlights_url" to "https://www.youtube.com/watch?v=BtF-3sseK3Q",
                "thumbnail_url" to "https://i.ytimg.com/vi/BtF-3sseK3Q/hqdefault.jpg",
                "source_name" to "cricket.com.au",
                "verified" to "TRUE",
                "verification_date" to "2026-10-17",
                "day1_url" to "https://www.youtube.com/watch?v=BtF-3sseK3Q",
                "day2_url" to "https://www.youtube.com/watch?v=poaCBNRtZAs",
                "day3_url" to "https://www.youtube.com/watch?v=6go1ZtaZfTk",
                "day4_url" to "https://www.youtube.com/watch?v=Z1ut5GVm-HU",
                "day5_url" to "https://www.youtube.com/watch?v=M4j4dp-Pj0o",
                "day1_verified" to "TRUE",
                "day2_verified" to "TRUE",
                "day3_verified" to "TRUE",
                "day4_verified" to "TRUE",
                "day5_verified" to "TRUE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1223871L)
        assertNotNull(item)
        assertEquals("cricket.com.au", item!!.sourceName)
        assertTrue(item.verified)
        for (day in 1..5) {
            assertTrue("Day $day should be available", item.isDayAvailable(day))
            assertNotNull(item.getPlayUrlForDay(day))
        }
        assertEquals("https://www.youtube.com/watch?v=M4j4dp-Pj0o", item.getPlayUrlForDay(5))
    }

    /**
     * Test 20: BGT 2020/21 4th Test (Match 1223872 - Brisbane Cricket Ground / Gabba)
     */
    @Test
    fun testBgt2020FourthTestHighlights() = runBlocking {
        val rows = listOf(
            mapOf(
                "match_id" to "1223872",
                "youtube_video_id" to "kvOHKfcfcc4",
                "highlights_url" to "https://www.youtube.com/watch?v=kvOHKfcfcc4",
                "thumbnail_url" to "https://i.ytimg.com/vi/kvOHKfcfcc4/hqdefault.jpg",
                "source_name" to "cricket.com.au",
                "verified" to "TRUE",
                "verification_date" to "2026-10-17",
                "day1_url" to "https://www.youtube.com/watch?v=kvOHKfcfcc4",
                "day2_url" to "https://www.youtube.com/watch?v=tyncgEZpgjk",
                "day3_url" to "https://www.youtube.com/watch?v=7bsO41zNSRE",
                "day4_url" to "https://www.youtube.com/watch?v=ezV6Ry2LVyI",
                "day5_url" to "https://www.youtube.com/watch?v=wY6UyatwVTA",
                "day1_verified" to "TRUE",
                "day2_verified" to "TRUE",
                "day3_verified" to "TRUE",
                "day4_verified" to "TRUE",
                "day5_verified" to "TRUE"
            )
        )
        repository.loadFromLines(rows)

        val item = repository.getMatchHighlights(1223872L)
        assertNotNull(item)
        assertEquals("cricket.com.au", item!!.sourceName)
        assertTrue(item.verified)
        for (day in 1..5) {
            assertTrue("Day $day should be available", item.isDayAvailable(day))
            assertNotNull(item.getPlayUrlForDay(day))
        }
        assertEquals("https://www.youtube.com/watch?v=wY6UyatwVTA", item.getPlayUrlForDay(5))
    }
}



