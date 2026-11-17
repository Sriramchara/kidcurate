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
}
