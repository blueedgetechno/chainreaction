package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.blueedge.chainreaction.ui.theme.GameBlue
import kotlinx.coroutines.delay
import kotlin.math.pow

// ── Splash Screen ──

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(4000L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        ChainReactionLoadingAnimation(
            modifier = Modifier.size(200.dp),
            color = GameBlue
        )
    }
}

// ── Reusable loading animation ──

/**
 * Looping chain-reaction loading animation that mimics the game mechanic:
 *
 * 1. Single orb at center, dots spread from 1→4
 * 2. Orb explodes into 4 orbs moving in cardinal directions, each with 1 dot
 * 3. Each orb's dots spread 1→4
 * 4. Each explodes into 4 (16 total); 4 center-bound merge, 12 outer fade out
 * 5. Back to single orb with 1 dot — loop restarts
 */
@Composable
fun ChainReactionLoadingAnimation(
    modifier: Modifier = Modifier,
    color: Color = GameBlue,
    cycleDurationMs: Int = 3500
) {
    val transition = rememberInfiniteTransition(label = "chainLoading")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(cycleDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loadingProgress"
    )

    Canvas(modifier = modifier) {
        drawChainReactionFrame(progress, color)
    }
}

// ── Animation internals ──

// Phase timing boundaries (fractions of 1 full cycle)
private const val PA_END = 0.17f   // Phase A end: single orb fills 1→4 dots
private const val PB_END = 0.38f   // Phase B end: explosion, 4 orbs fly outward
private const val PC_END = 0.55f   // Phase C end: 4 orbs fill 1→4 dots each
private const val PD_END = 0.85f   // Phase D end: 16 orbs, center merge + outer fade
// Phase E: 0.85→1.0: single orb, 1 dot hold (seamless loop restart)

// Cardinal directions: N, S, E, W
private val DIRECTIONS = listOf(
    Offset(0f, -1f),
    Offset(0f, 1f),
    Offset(1f, 0f),
    Offset(-1f, 0f)
)

/** Decelerate easing (cubic ease-out) */
private fun ease(t: Float): Float = 1f - (1f - t).pow(3)

private fun DrawScope.drawChainReactionFrame(progress: Float, color: Color) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val R = size.minDimension * 0.13f      // orb radius (bigger)
    val dr = R * 0.22f                      // white dot radius
    val ds = R * 0.5f                       // dot spread distance inside orb
    val D = size.minDimension * 0.28f      // split travel distance (bigger)

    when {
        // ── Phase A: Single orb, dots spread 1→4 ──
        progress < PA_END -> {
            val p = ease((progress / PA_END).coerceIn(0f, 1f))
            drawOrb(center, R, dr, ds, dotCount = 4, dotSpread = p, color, alpha = 1f)
        }

        // ── Phase B: Explosion → 4 orbs move outward ──
        progress < PB_END -> {
            val rawP = ((progress - PA_END) / (PB_END - PA_END)).coerceIn(0f, 1f)
            val moveP = ease(rawP)

            // Crossfade sub-phase: big circle pops + fades, 4 small circles emerge
            val crossfadeFrac = 0.15f
            val crossP = (rawP / crossfadeFrac).coerceIn(0f, 1f)

            // Fading big orb with a slight pop
            if (crossP < 1f) {
                val popScale = 1f + crossP * 0.3f
                drawOrb(center, R * popScale, dr, ds, 4, 1f, color, alpha = 1f - crossP)
            }

            // 4 emerging orbs: start at dot positions inside the big orb, fly to cardinal positions
            for (dir in DIRECTIONS) {
                val startPos = Offset(center.x + dir.x * ds, center.y + dir.y * ds)
                val endPos = Offset(center.x + dir.x * D, center.y + dir.y * D)
                val pos = Offset(
                    startPos.x + (endPos.x - startPos.x) * moveP,
                    startPos.y + (endPos.y - startPos.y) * moveP
                )
                val alpha = crossP.coerceIn(0.3f, 1f)
                drawOrb(pos, R, dr, ds, 1, 1f, color, alpha)
            }
        }

        // ── Phase C: 4 orbs at cardinal positions, dots spread 1→4 ──
        progress < PC_END -> {
            val p = ease(((progress - PB_END) / (PC_END - PB_END)).coerceIn(0f, 1f))
            for (dir in DIRECTIONS) {
                val orbPos = Offset(center.x + dir.x * D, center.y + dir.y * D)
                drawOrb(orbPos, R, dr, ds, 4, p, color, alpha = 1f)
            }
        }

        // ── Phase D: Each orb explodes → 16 total. Center-bound merge, outer fade ──
        progress < PD_END -> {
            val rawP = ((progress - PC_END) / (PD_END - PC_END)).coerceIn(0f, 1f)
            val moveP = ease(rawP)

            // Crossfade: parent orbs pop + fade, child orbs emerge
            val crossfadeFrac = 0.12f
            val crossP = (rawP / crossfadeFrac).coerceIn(0f, 1f)

            // Fading 4 parent orbs
            if (crossP < 1f) {
                for (dir in DIRECTIONS) {
                    val parentPos = Offset(center.x + dir.x * D, center.y + dir.y * D)
                    val popScale = 1f + crossP * 0.3f
                    drawOrb(parentPos, R * popScale, dr, ds, 4, 1f, color, alpha = 1f - crossP)
                }
            }

            // 16 child orbs
            for (parentDir in DIRECTIONS) {
                val parentPos = Offset(
                    center.x + parentDir.x * D,
                    center.y + parentDir.y * D
                )
                for (childDir in DIRECTIONS) {
                    // Is this child heading toward center? (opposite direction to parent)
                    val isInward =
                        parentDir.x + childDir.x == 0f && parentDir.y + childDir.y == 0f

                    val startPos = Offset(
                        parentPos.x + childDir.x * ds,
                        parentPos.y + childDir.y * ds
                    )
                    val endPos = Offset(
                        parentPos.x + childDir.x * D,
                        parentPos.y + childDir.y * D
                    )
                    val pos = Offset(
                        startPos.x + (endPos.x - startPos.x) * moveP,
                        startPos.y + (endPos.y - startPos.y) * moveP
                    )

                    val alpha = if (isInward) {
                        crossP.coerceIn(0.3f, 1f)
                    } else {
                        // Outer orbs: fade to 0 quickly (by 20% of travel)
                        val fadeCutoff = 0.20f
                        if (rawP < 0.01f) crossP.coerceIn(0.3f, 1f)
                        else (1f - (rawP / fadeCutoff)).coerceAtLeast(0f)
                    }

                    if (alpha > 0.01f) {
                        drawOrb(pos, R, dr, ds, 1, 1f, color, alpha)
                    }
                }
            }
        }

        // ── Phase E: Single orb at center, 1 dot, hold ──
        else -> {
            drawOrb(center, R, dr, ds, 1, 1f, color, alpha = 1f)
        }
    }
}

/**
 * Draw a single "orb" — a colored circle with white dots inside.
 *
 * @param orbCenter   position of the orb
 * @param orbRadius   radius of the colored circle
 * @param dotRadius   radius of each white dot
 * @param dotSpreadDist  max spread of dots from orb center
 * @param dotCount    number of dots (1 or 4)
 * @param dotSpread   0 = all dots at orb center, 1 = dots at final spread positions
 * @param color       fill color of the orb
 * @param alpha       opacity
 */
private fun DrawScope.drawOrb(
    orbCenter: Offset,
    orbRadius: Float,
    dotRadius: Float,
    dotSpreadDist: Float,
    dotCount: Int,
    dotSpread: Float,
    color: Color,
    alpha: Float
) {
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = orbRadius,
        center = orbCenter
    )

    // Dot target positions (cross layout: N, S, E, W — matches explosion directions)
    val targets = when (dotCount) {
        1 -> listOf(orbCenter)
        4 -> listOf(
            Offset(orbCenter.x, orbCenter.y - dotSpreadDist),
            Offset(orbCenter.x, orbCenter.y + dotSpreadDist),
            Offset(orbCenter.x + dotSpreadDist, orbCenter.y),
            Offset(orbCenter.x - dotSpreadDist, orbCenter.y)
        )
        else -> listOf(orbCenter)
    }

    for (target in targets) {
        val pos = Offset(
            orbCenter.x + (target.x - orbCenter.x) * dotSpread,
            orbCenter.y + (target.y - orbCenter.y) * dotSpread
        )
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = dotRadius,
            center = pos
        )
    }
}
