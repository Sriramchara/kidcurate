package com.kidcurate.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val shape: ParticleShape,
    val phase: Float,
)

enum class ParticleShape { CIRCLE, RIBBON, STAR }

@Composable
fun ConfettiCelebration(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
) {
    if (!isActive) return

    val colors = listOf(
        Color(0xFFFFD166), // Sparkling Sun Gold
        Color(0xFFFF70A6), // Sweet Bubblegum Rose
        Color(0xFF70D6FF), // Bright Sky Blue
        Color(0xFF06D6A0), // Fresh Mint Emerald
        Color(0xFF8338EC), // Vivid Royal Violet
        Color(0xFFFF9F1C), // Warm Tangerine
        Color(0xFFE0AAFF), // Soft Lilac
    )

    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = -0.1f - Random.nextFloat() * 0.5f,
                vx = (Random.nextFloat() - 0.5f) * 0.25f,
                vy = 0.35f + Random.nextFloat() * 0.55f,
                color = colors[Random.nextInt(colors.size)],
                size = 10f + Random.nextFloat() * 16f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 540f,
                shape = when (Random.nextInt(3)) {
                    0 -> ParticleShape.STAR
                    1 -> ParticleShape.RIBBON
                    else -> ParticleShape.CIRCLE
                },
                phase = Random.nextFloat() * 2f * PI.toFloat(),
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti_anim")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "confetti_progress",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            // Subtle harmonic sway left/right for natural floating feel
            val sway = sin(progress * 4f * PI.toFloat() + p.phase) * (width * 0.035f)
            val currentY = ((p.y + p.vy * progress) % 1.25f) * height
            val currentX = (p.x * width) + (p.vx * progress * width) + sway
            val currentRot = p.rotation + p.rotationSpeed * progress

            rotate(degrees = currentRot, pivot = Offset(currentX, currentY)) {
                when (p.shape) {
                    ParticleShape.STAR -> {
                        // Twinkling shimmer effect
                        val twinkleAlpha = (0.55f + 0.45f * sin(progress * 14f + p.phase)).coerceIn(0.2f, 1f)
                        val starPath = createStarPath(
                            cx = currentX,
                            cy = currentY,
                            outerRadius = p.size,
                            innerRadius = p.size * 0.42f,
                        )
                        drawPath(
                            path = starPath,
                            color = p.color.copy(alpha = twinkleAlpha),
                        )
                    }
                    ParticleShape.RIBBON -> {
                        // 3D tumble compression across the horizontal axis
                        val tumble = abs(cos(progress * 8f + p.phase)).coerceIn(0.15f, 1f)
                        scale(scaleX = tumble, scaleY = 1f, pivot = Offset(currentX, currentY)) {
                            drawRect(
                                color = p.color,
                                topLeft = Offset(currentX - p.size, currentY - p.size * 0.35f),
                                size = Size(p.size * 2f, p.size * 0.7f),
                            )
                        }
                    }
                    ParticleShape.CIRCLE -> {
                        drawCircle(
                            color = p.color.copy(alpha = 0.9f),
                            radius = p.size * 0.45f,
                            center = Offset(currentX, currentY),
                        )
                    }
                }
            }
        }
    }
}

private fun createStarPath(
    cx: Float,
    cy: Float,
    outerRadius: Float,
    innerRadius: Float,
): Path {
    val path = Path()
    val points = 5
    val step = PI / points
    var angle = -PI / 2.0 // Start pointing directly upwards

    for (i in 0 until (2 * points)) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = (cx + r * cos(angle)).toFloat()
        val y = (cy + r * sin(angle)).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
        angle += step
    }
    path.close()
    return path
}
