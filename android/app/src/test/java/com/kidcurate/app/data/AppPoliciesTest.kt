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
