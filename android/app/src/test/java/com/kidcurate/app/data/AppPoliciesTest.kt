package com.kidcurate.app.data

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
}
