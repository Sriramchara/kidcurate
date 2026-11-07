package com.kidcurate.app.ui.components

import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object SoundHaptics {
    fun playTactileClick(view: View, haptic: HapticFeedback) {
        try {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (_: Exception) {}
        try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {}
    }

    fun playPop(view: View, haptic: HapticFeedback) {
        try {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (_: Exception) {}
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}
    }

    fun playCelebration(view: View, haptic: HapticFeedback) {
        try {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (_: Exception) {}
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}
    }
}
