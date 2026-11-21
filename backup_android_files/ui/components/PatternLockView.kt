package com.kidcurate.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidcurate.app.data.ParentLockPolicy
import com.kidcurate.app.ui.theme.BrandCoral
import com.kidcurate.app.ui.theme.BrandPrimary
import com.kidcurate.app.ui.theme.BrandSuccess
import kotlinx.coroutines.delay
import kotlin.math.hypot

/**
 * Interactive 3x3 Gesture Pattern Lock component for independent parent verification.
 * Captures touch-drag gestures cleanly inside scrollable dialogs, auto-connects skipped
 * collinear midpoints, triggers tactile haptic pulses on each node, and outputs ordered
 * dot indices (0..8) upon finger release.
 */
@Composable
fun PatternLockView(
    modifier: Modifier = Modifier,
    maxSize: Dp = 236.dp,
    enabled: Boolean = true,
    isError: Boolean = false,
    isSuccess: Boolean = false,
    resetKey: Any? = null,
    helperText: String = "Draw pattern (connect at least ${ParentLockPolicy.MIN_PATTERN_DOTS} dots)",
    onPatternStart: () -> Unit = {},
    onPatternComplete: (List<Int>) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val currentOnPatternStart by rememberUpdatedState(onPatternStart)
    val currentOnPatternComplete by rememberUpdatedState(onPatternComplete)
    val currentEnabled by rememberUpdatedState(enabled)

    var selectedDots by remember(resetKey) { mutableStateOf<List<Int>>(emptyList()) }
    var currentPointer by remember(resetKey) { mutableStateOf<Offset?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Clear error trace automatically after a brief visual flash so the parent can retry smoothly
    LaunchedEffect(isError, selectedDots) {
        if (isError && selectedDots.isNotEmpty() && currentPointer == null) {
            delay(750)
            selectedDots = emptyList()
        }
    }

    val activeColor = when {
        isError -> BrandCoral
        isSuccess -> BrandSuccess
        else -> BrandPrimary
    }
    val idleDotColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val idleRingColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    val surfaceBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val borderColor = when {
        isError -> BrandCoral.copy(alpha = 0.7f)
        isSuccess -> BrandSuccess.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    fun dotCenter(index: Int, width: Float, height: Float): Offset {
        val cellW = width / 3f
        val cellH = height / 3f
        val row = index / 3
        val col = index % 3
        return Offset(
            x = cellW * col + cellW / 2f,
            y = cellH * row + cellH / 2f,
        )
    }

    fun hitTestDot(pos: Offset, width: Float, height: Float): Int? {
        if (width <= 0f || height <= 0f) return null
        val cellMin = minOf(width, height) / 3f
        val hitRadius = cellMin * 0.34f
        for (idx in 0..8) {
            val center = dotCenter(idx, width, height)
            val dist = hypot((pos.x - center.x).toDouble(), (pos.y - center.y).toDouble()).toFloat()
            if (dist <= hitRadius) {
                return idx
            }
        }
        return null
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxSize)
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(surfaceBg)
                .border(1.5.dp, borderColor, RoundedCornerShape(20.dp))
                .padding(8.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(resetKey) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            if (!currentEnabled) return@awaitEachGesture
                            down.consume()

                            val w = canvasSize.width.toFloat()
                            val h = canvasSize.height.toFloat()
                            var activeSequence = emptyList<Int>()
                            currentOnPatternStart()

                            val startHit = hitTestDot(down.position, w, h)
                            if (startHit != null) {
                                activeSequence = listOf(startHit)
                                selectedDots = activeSequence
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            } else {
                                selectedDots = emptyList()
                            }
                            currentPointer = down.position

                            val pointerId = down.id
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == pointerId }
                                    ?: event.changes.firstOrNull()
                                    ?: break

                                if (!change.pressed) {
                                    change.consume()
                                    break
                                }

                                change.consume()
                                currentPointer = change.position
                                val hit = hitTestDot(change.position, w, h)
                                if (hit != null && hit !in activeSequence) {
                                    val updated = ParentLockPolicy.resolvePatternStep(activeSequence, hit)
                                    if (updated.size > activeSequence.size) {
                                        activeSequence = updated
                                        selectedDots = activeSequence
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                            }

                            currentPointer = null
                            if (activeSequence.isNotEmpty()) {
                                currentOnPatternComplete(activeSequence)
                            }
                        }
                    },
            ) {
                val w = size.width
                val h = size.height
                val cellMin = minOf(w, h) / 3f
                val strokeWidthPx = 4.dp.toPx()
                val outerRingRadius = cellMin * 0.24f
                val haloRadius = cellMin * 0.30f
                val innerDotRadius = cellMin * 0.085f
                val selectedInnerRadius = cellMin * 0.115f

                // 1. Connecting lines between selected dots
                if (selectedDots.size >= 2) {
                    for (i in 0 until selectedDots.size - 1) {
                        val p1 = dotCenter(selectedDots[i], w, h)
                        val p2 = dotCenter(selectedDots[i + 1], w, h)
                        drawLine(
                            color = activeColor.copy(alpha = 0.85f),
                            start = p1,
                            end = p2,
                            strokeWidth = strokeWidthPx,
                            cap = StrokeCap.Round,
                        )
                    }
                }

                // 2. Trailing line to active finger position
                val lastDot = selectedDots.lastOrNull()
                val pointer = currentPointer
                if (lastDot != null && pointer != null) {
                    val lastCenter = dotCenter(lastDot, w, h)
                    val clampedPointer = Offset(
                        x = pointer.x.coerceIn(0f, w),
                        y = pointer.y.coerceIn(0f, h),
                    )
                    drawLine(
                        color = activeColor.copy(alpha = 0.45f),
                        start = lastCenter,
                        end = clampedPointer,
                        strokeWidth = strokeWidthPx * 0.85f,
                        cap = StrokeCap.Round,
                    )
                }

                // 3. Render 3x3 dot matrix
                for (idx in 0..8) {
                    val center = dotCenter(idx, w, h)
                    val isSelected = idx in selectedDots
                    if (isSelected) {
                        drawCircle(
                            color = activeColor.copy(alpha = 0.16f),
                            radius = haloRadius,
                            center = center,
                        )
                        drawCircle(
                            color = activeColor.copy(alpha = 0.35f),
                            radius = outerRingRadius,
                            center = center,
                        )
                        drawCircle(
                            color = activeColor,
                            radius = selectedInnerRadius,
                            center = center,
                        )
                    } else {
                        drawCircle(
                            color = idleRingColor.copy(alpha = 0.14f),
                            radius = outerRingRadius * 0.72f,
                            center = center,
                        )
                        drawCircle(
                            color = idleDotColor,
                            radius = innerDotRadius,
                            center = center,
                        )
                    }
                }
            }
        }

        if (helperText.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = helperText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isError) BrandCoral else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
