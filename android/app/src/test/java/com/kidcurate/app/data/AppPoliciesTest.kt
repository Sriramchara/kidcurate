package com.kidcurate.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppPoliciesTest {
    @Test
    fun parentPinCanOnlyBeReusedByItsGoogleAccount() {
        assertTrue(
            ParentSessionPolicy.canReusePin(
                hasPin = true,
                pinOwnerEmail = "parent@example.com",
                currentAccountEmail = "",
                signingInEmail = "PARENT@example.com",
            )
        )
        assertFalse(
            ParentSessionPolicy.canReusePin(
                hasPin = true,
                pinOwnerEmail = "parent@example.com",
                currentAccountEmail = "",
                signingInEmail = "different@example.com",
            )
        )
        assertFalse(
            ParentSessionPolicy.canReusePin(
                hasPin = false,
                pinOwnerEmail = "parent@example.com",
                currentAccountEmail = "parent@example.com",
                signingInEmail = "parent@example.com",
            )
        )
    }

    @Test
    fun legacyPinUsesCurrentAuthenticatedEmailAsOwner() {
        assertTrue(
            ParentSessionPolicy.canReusePin(
                hasPin = true,
                pinOwnerEmail = "",
                currentAccountEmail = "parent@example.com",
                signingInEmail = "parent@example.com",
            )
        )
    }

    @Test
    fun compactLandscapeCoversShortWidePhonesOnly() {
        assertTrue(ResponsiveLayoutPolicy.isCompactLandscape(widthDp = 915, heightDp = 412))
        assertFalse(ResponsiveLayoutPolicy.isCompactLandscape(widthDp = 412, heightDp = 915))
        assertFalse(ResponsiveLayoutPolicy.isCompactLandscape(widthDp = 1280, heightDp = 800))
    }

    @Test
    fun tabletAndLandscapePredicatesIdentifyTabletScreens() {
        assertTrue(ResponsiveLayoutPolicy.isTablet(widthDp = 600))
        assertTrue(ResponsiveLayoutPolicy.isTablet(widthDp = 800))
        assertFalse(ResponsiveLayoutPolicy.isTablet(widthDp = 412))

        assertTrue(ResponsiveLayoutPolicy.isLandscape(widthDp = 915, heightDp = 412))
        assertFalse(ResponsiveLayoutPolicy.isLandscape(widthDp = 412, heightDp = 915))

        assertTrue(ResponsiveLayoutPolicy.isTabletLandscape(widthDp = 1280, heightDp = 800))
        assertFalse(ResponsiveLayoutPolicy.isTabletLandscape(widthDp = 915, heightDp = 412))
        assertFalse(ResponsiveLayoutPolicy.isTabletLandscape(widthDp = 800, heightDp = 1280))
    }

    @Test
    fun responsiveLayoutPolicySelectsAdaptiveTwoPaneAndGridConfigurations() {
        // PIN two-pane layout: any landscape orientation
        assertTrue(ResponsiveLayoutPolicy.shouldUseTwoPanePin(widthDp = 915, heightDp = 412))
        assertTrue(ResponsiveLayoutPolicy.shouldUseTwoPanePin(widthDp = 1280, heightDp = 800))
        assertFalse(ResponsiveLayoutPolicy.shouldUseTwoPanePin(widthDp = 412, heightDp = 915))
        assertFalse(ResponsiveLayoutPolicy.shouldUseTwoPanePin(widthDp = 800, heightDp = 1280))

        // Playlist two-pane console
        assertTrue(ResponsiveLayoutPolicy.shouldShowTwoPanePlaylist(widthDp = 915, heightDp = 412))
        assertTrue(ResponsiveLayoutPolicy.shouldShowTwoPanePlaylist(widthDp = 1280, heightDp = 800))
        assertFalse(ResponsiveLayoutPolicy.shouldShowTwoPanePlaylist(widthDp = 412, heightDp = 915))

        // Playlist grid columns: tablet landscape = 2, tablet portrait = 2, phone = 1
        assertEquals(2, ResponsiveLayoutPolicy.playlistGridColumns(widthDp = 1280, heightDp = 800))
        assertEquals(2, ResponsiveLayoutPolicy.playlistGridColumns(widthDp = 800, heightDp = 1280))
        assertEquals(1, ResponsiveLayoutPolicy.playlistGridColumns(widthDp = 915, heightDp = 412))
        assertEquals(1, ResponsiveLayoutPolicy.playlistGridColumns(widthDp = 412, heightDp = 915))

        // PIN button sizing: adaptive across tablet vs compact landscape vs portrait
        assertEquals(60, ResponsiveLayoutPolicy.pinButtonSizeDp(widthDp = 1280, heightDp = 800))
        assertEquals(46, ResponsiveLayoutPolicy.pinButtonSizeDp(widthDp = 915, heightDp = 412))
        assertEquals(64, ResponsiveLayoutPolicy.pinButtonSizeDp(widthDp = 360, heightDp = 640))
        assertEquals(74, ResponsiveLayoutPolicy.pinButtonSizeDp(widthDp = 412, heightDp = 915))
    }

    @Test
    fun cloudConnectionPolicyAllocatesAppropriateProbeTimeouts() {
        assertEquals(4, CloudConnectionPolicy.getRecommendedProbeTimeoutSeconds("http://localhost:8000/"))
        assertEquals(4, CloudConnectionPolicy.getRecommendedProbeTimeoutSeconds("http://10.0.2.2:8000/"))
        assertEquals(4, CloudConnectionPolicy.getRecommendedProbeTimeoutSeconds("http://127.0.0.1:8000/"))
        assertEquals(25, CloudConnectionPolicy.getRecommendedProbeTimeoutSeconds("https://kidcurate-api.onrender.com/"))
        assertEquals(25, CloudConnectionPolicy.getRecommendedProbeTimeoutSeconds("https://api.kidcurate.com/"))
    }

    @Test
    fun cloudConnectionPolicySanitizesBaseUrls() {
        assertEquals("http://localhost:8000/", CloudConnectionPolicy.sanitizeBaseUrl("http://localhost:8000"))
        assertEquals("http://localhost:8000/", CloudConnectionPolicy.sanitizeBaseUrl("http://localhost:8000/"))
        assertEquals("https://kidcurate-api.onrender.com/", CloudConnectionPolicy.sanitizeBaseUrl("  https://kidcurate-api.onrender.com  "))
        assertEquals("", CloudConnectionPolicy.sanitizeBaseUrl("   "))
    }

    @Test
    fun cloudConnectionPolicyCalculatesMaxColdStartWindow() {
        val attempts = 2
        val probeTimeout = CloudConnectionPolicy.getRecommendedProbeTimeoutSeconds("https://kidcurate-api.onrender.com/")
        val backoff = 2
        val totalWindow = (attempts * probeTimeout) + ((attempts - 1) * backoff)
        assertTrue(totalWindow >= 45)
    }

    @Test
    fun parentLockTypeMapsStorageKeysSafely() {
        assertEquals(ParentLockType.PIN, ParentLockType.fromStorageKey("pin"))
        assertEquals(ParentLockType.PATTERN, ParentLockType.fromStorageKey("pattern"))
        assertEquals(ParentLockType.PASSWORD, ParentLockType.fromStorageKey("PASSWORD"))
        assertEquals(ParentLockType.PIN, ParentLockType.fromStorageKey(null))
        assertEquals(ParentLockType.PIN, ParentLockType.fromStorageKey("unknown_type"))
    }

    @Test
    fun parentLockPolicyValidatesPinPatternAndPassword() {
        assertTrue(ParentLockPolicy.isValidPin("4829"))
        assertFalse(ParentLockPolicy.isValidPin("123"))
        assertFalse(ParentLockPolicy.isValidPin("12a4"))

        assertTrue(ParentLockPolicy.isValidPattern(listOf(0, 1, 4, 7)))
        assertFalse(ParentLockPolicy.isValidPattern(listOf(0, 1, 2)))
        assertFalse(ParentLockPolicy.isValidPattern(listOf(0, 1, 1, 2)))
        assertFalse(ParentLockPolicy.isValidPattern(listOf(0, 1, 2, 9)))

        assertTrue(ParentLockPolicy.isValidPassword("Parent2026"))
        assertFalse(ParentLockPolicy.isValidPassword("short"))

        assertEquals("0-1-4-7-8", ParentLockPolicy.encodePattern(listOf(0, 1, 4, 7, 8)))
        assertEquals(listOf(0, 1, 4, 7, 8), ParentLockPolicy.decodePattern("0-1-4-7-8"))
        assertEquals(null, ParentLockPolicy.validateCredential(ParentLockType.PATTERN, "0-1-4-7"))
        assertTrue(ParentLockPolicy.validateCredential(ParentLockType.PATTERN, "0-1-2") != null)
    }

    @Test
    fun parentLockPolicyAutoConnectsSkippedCollinearMidpoints() {
        // Dragging 0 -> 2 skips midpoint 1; must auto-insert 1
        assertEquals(listOf(0, 1, 2), ParentLockPolicy.resolvePatternStep(listOf(0), 2))
        // Dragging 0 -> 8 skips center 4; must auto-insert 4
        assertEquals(listOf(0, 4, 8), ParentLockPolicy.resolvePatternStep(listOf(0), 8))
        // Dragging 2 -> 6 skips center 4; must auto-insert 4
        assertEquals(listOf(2, 4, 6), ParentLockPolicy.resolvePatternStep(listOf(2), 6))
        // Knight move 0 -> 5 has no collinear midpoint; must not insert anything extra
        assertEquals(listOf(0, 5), ParentLockPolicy.resolvePatternStep(listOf(0), 5))
        // Already selected dot should be ignored
        assertEquals(listOf(0, 1, 2), ParentLockPolicy.resolvePatternStep(listOf(0, 1, 2), 1))
    }

    @Test
    fun parentLockPolicyHashesAndVerifiesWithPbkdf2AndRandomSalts() {
        val saltA = ByteArray(16) { it.toByte() }
        val saltB = ByteArray(16) { (it + 32).toByte() }

        val patternEncoded = ParentLockPolicy.encodePattern(listOf(0, 3, 6, 7, 8))
        val hashA = ParentLockPolicy.hashSecretPbkdf2(patternEncoded, saltA)
        val hashB = ParentLockPolicy.hashSecretPbkdf2(patternEncoded, saltB)

        assertTrue(hashA.startsWith("pbkdf2$100000$"))
        assertTrue(hashA != hashB)
        assertTrue(ParentLockPolicy.verifySecretPbkdf2(patternEncoded, hashA))
        assertTrue(ParentLockPolicy.verifySecretPbkdf2(patternEncoded, hashB))
        assertFalse(ParentLockPolicy.verifySecretPbkdf2("0-3-6-7-5", hashA))

        val pwHash = ParentLockPolicy.hashSecretPbkdf2("KidCurate#99", saltA)
        assertTrue(ParentLockPolicy.verifySecretPbkdf2("KidCurate#99", pwHash))
        assertFalse(ParentLockPolicy.verifySecretPbkdf2("kidcurate#99", pwHash))
    }

    @Test
    fun playbackResumePolicyPreventsResumingAtEndOfVideo() {
        // Video is 375s (06:15). Saved progress at 375s or 340s (>= 85%) must reset to 0f.
        assertEquals(0f, PlaybackResumePolicy.safeResumeSeconds(375f, 375), 0.01f)
        assertEquals(0f, PlaybackResumePolicy.safeResumeSeconds(340f, 375), 0.01f)
        // Mid-video progress (120s of 375s) resumes cleanly.
        assertEquals(120f, PlaybackResumePolicy.safeResumeSeconds(120f, 375), 0.01f)
        // Near-start progress (< 1s) starts cleanly at 0f.
        assertEquals(0f, PlaybackResumePolicy.safeResumeSeconds(0.5f, 375), 0.01f)

        assertFalse(PlaybackResumePolicy.shouldPersistProgress(375f, 375f, isEnded = true))
        assertFalse(PlaybackResumePolicy.shouldPersistProgress(350f, 375f, isEnded = false))
        assertTrue(PlaybackResumePolicy.shouldPersistProgress(120f, 375f, isEnded = false))
    }
}
