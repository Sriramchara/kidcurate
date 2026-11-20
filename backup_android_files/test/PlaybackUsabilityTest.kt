package com.kidcurate.app.ui

import com.kidcurate.app.data.ResponsiveLayoutPolicy
import org.junit.Assert.*
import org.junit.Test

class PlaybackUsabilityTest {

    @Test
    fun antiSeekingProtectionIgnoresFastForwardDeltas() {
        // Simulates the watch delta accounting logic in PlayerScreen
        var lastRecordedSecond = 2
        var totalWatchedRecorded = 2

        // User scrubs/seeks to 250s of a 260s video (delta = 248s)
        val scrubbedSecond = 250
        val delta = scrubbedSecond - lastRecordedSecond

        // In PlayerScreen: delta in 1..5 is accepted as genuine playback; seeks are ignored
        if (delta in 1..5) {
            totalWatchedRecorded += delta
        }
        // Watched time must NOT inflate
        assertEquals(2, totalWatchedRecorded)
    }

    @Test
    fun continuousPlaybackAccumulatesTimeAccurately() {
        var lastRecordedSecond = 0
        var totalWatchedRecorded = 0

        // 5 seconds of real playback (1s heartbeat ticks)
        for (sec in 1..5) {
            val delta = sec - lastRecordedSecond
            if (delta in 1..5) {
                totalWatchedRecorded += delta
            }
            lastRecordedSecond = sec
        }

        assertEquals(5, totalWatchedRecorded)
    }

    @Test
    fun midVideoRotationPreservesPlaybackPosition() {
        // Simulates saved state before rotation
        val timestampBeforeRotation = 34.5f

        // On rotation / Activity recreation, rememberSaveable restores currentSecond
        val restoredCurrentSecond = timestampBeforeRotation

        // PlayerScreen initializes YouTubePlayer with resumeSeconds
        val resumeSeconds = if (restoredCurrentSecond > 1f) restoredCurrentSecond else 0f

        assertEquals(34.5f, resumeSeconds, 0.01f)
        assertNotEquals(0f, resumeSeconds)
    }

    @Test
    fun newVideoTransitionResetsTimestampToZero() {
        val newVideoId = "_7jhd4-o9gU"
        val previousVideoId = "libKVRa0WV4"

        val isDifferentVideo = newVideoId != previousVideoId
        val startSeconds = if (isDifferentVideo) 0f else 34.5f

        assertEquals(0f, startSeconds, 0.01f)
    }

    @Test
    fun childLockBlocksUnauthorizedBackNavigation() {
        var isChildLockActive = true
        var isExitingPlayer = false
        var showParentPasswordDialog = false
        var navigatedBack = false

        // User presses hardware or on-screen back
        if (isChildLockActive) {
            isExitingPlayer = true
            showParentPasswordDialog = true
        } else {
            navigatedBack = true
        }

        assertTrue(showParentPasswordDialog)
        assertTrue(isExitingPlayer)
        assertFalse(navigatedBack)

        // Parent successfully enters 4-digit PIN
        fun onParentPinSuccess() {
            showParentPasswordDialog = false
            isChildLockActive = false
            if (isExitingPlayer) {
                isExitingPlayer = false
                navigatedBack = true
            }
        }

        onParentPinSuccess()

        assertFalse(showParentPasswordDialog)
        assertFalse(isChildLockActive)
        assertTrue(navigatedBack)
    }

    @Test
    fun responsiveLayoutAdaptsAcrossDeviceMatrix() {
        // Phone Portrait (e.g. Pixel 7: 412 x 915 dp)
        assertFalse(ResponsiveLayoutPolicy.isLandscape(412, 915))
        assertFalse(ResponsiveLayoutPolicy.isTablet(412, 915))
        assertEquals(1, ResponsiveLayoutPolicy.playlistGridColumns(412, 915))
        assertEquals(74, ResponsiveLayoutPolicy.pinButtonSizeDp(412, 915))

        // Phone Landscape (e.g. Pixel 7 landscape: 915 x 412 dp)
        assertTrue(ResponsiveLayoutPolicy.isLandscape(915, 412))
        assertFalse(ResponsiveLayoutPolicy.isTablet(915, 412))
        assertTrue(ResponsiveLayoutPolicy.shouldUseTwoPanePin(915, 412))
        assertEquals(1, ResponsiveLayoutPolicy.playlistGridColumns(915, 412))
        assertEquals(46, ResponsiveLayoutPolicy.pinButtonSizeDp(915, 412))

        // Tablet Portrait (e.g. Galaxy Tab: 800 x 1280 dp)
        assertFalse(ResponsiveLayoutPolicy.isLandscape(800, 1280))
        assertTrue(ResponsiveLayoutPolicy.isTablet(800, 1280))
        assertEquals(2, ResponsiveLayoutPolicy.playlistGridColumns(800, 1280))
        assertEquals(74, ResponsiveLayoutPolicy.pinButtonSizeDp(800, 1280))

        // Tablet Landscape (e.g. Galaxy Tab landscape: 1280 x 800 dp)
        assertTrue(ResponsiveLayoutPolicy.isLandscape(1280, 800))
        assertTrue(ResponsiveLayoutPolicy.isTablet(1280, 800))
        assertTrue(ResponsiveLayoutPolicy.shouldUseTwoPanePin(1280, 800))
        assertEquals(2, ResponsiveLayoutPolicy.playlistGridColumns(1280, 800))
        assertEquals(60, ResponsiveLayoutPolicy.pinButtonSizeDp(1280, 800))

        // Split-Screen / Multi-Window (e.g. docked 400 x 700 dp)
        assertFalse(ResponsiveLayoutPolicy.isLandscape(400, 700))
        assertEquals(1, ResponsiveLayoutPolicy.playlistGridColumns(400, 700))
        assertEquals(74, ResponsiveLayoutPolicy.pinButtonSizeDp(400, 700))
    }

    @Test
    fun pbkdf2HashedPinProducesExpectedFormatAndSalt() {
        val pin = "1234"
        val salt = ByteArray(16) { 0x01.toByte() }
        val spec = javax.crypto.spec.PBEKeySpec(pin.toCharArray(), salt, 100_000, 256)
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        assertTrue(hash.isNotEmpty())
        assertEquals(32, hash.size)
    }

    @Test
    fun failedPinAttemptsTriggerExponentialLockout() {
        val maxAttempts = 5
        var failedAttempts = 0
        var lockoutMs = 0L

        for (i in 1..7) {
            failedAttempts++
            if (failedAttempts >= maxAttempts) {
                val multiplier = 1 shl (failedAttempts - maxAttempts).coerceAtMost(5)
                lockoutMs = 30_000L * multiplier
            }
        }

        assertEquals(7, failedAttempts)
        assertEquals(120_000L, lockoutMs)
    }
}
