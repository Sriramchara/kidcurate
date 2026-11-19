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
}
