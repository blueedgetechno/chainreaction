package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import com.blueedge.chainreaction.data.BotDifficulty
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.GameMode
import com.blueedge.chainreaction.data.Strings
import com.blueedge.chainreaction.ui.components.Raised3DButton
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
import com.blueedge.chainreaction.ui.theme.SecondaryActionShadow
import kotlin.math.sqrt

@Composable
fun VictoryScreen(
    winnerName: String,
    winsText: String,
    winnerColor: Color,
    capturedCells: Int,
    totalMoves: Int,
    durationSeconds: Long,
    showPlayAgain: Boolean,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit
) {
    // Circular reveal animation
    val circleRadius = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    var screenSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(Unit) {
        val maxRadius = if (screenSize != IntSize.Zero) {
            sqrt((screenSize.width * screenSize.width + screenSize.height * screenSize.height).toDouble()).toFloat()
        } else {
            3000f
        }
        circleRadius.animateTo(
            targetValue = maxRadius,
            animationSpec = tween(durationMillis = 700)
        )
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 350)
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { screenSize = it },
        contentAlignment = Alignment.Center
    ) {
        val isLandscape = maxWidth > maxHeight

        // Animated circular fill
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = winnerColor,
                radius = circleRadius.value,
                center = Offset(size.width / 2f, size.height / 2f)
            )
        }

        if (isLandscape) {
            // ── Landscape: winner info left, stats + buttons right ──
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .alpha(contentAlpha.value),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Left side: trophy + winner name
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Color(0xFFFFFFFF)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "$winnerName\n$winsText",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 38.sp
                    )
                }

                Spacer(Modifier.width(24.dp))

                // Right side: stats card + buttons (scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    StatsCard(capturedCells, totalMoves, durationSeconds)

                    Spacer(Modifier.height(24.dp))

                    if (showPlayAgain) {
                        Raised3DButton(
                            text = Strings.playAgain,
                            onClick = onPlayAgain,
                            mainColor = Color.White,
                            shadowColor = Color(0xFFDDDDDD),
                            textColor = winnerColor,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))
                    }

                    Raised3DButton(
                        text = Strings.menu,
                        onClick = onMenu,
                        mainColor = SecondaryActionColor,
                        shadowColor = SecondaryActionShadow,
                        textColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // ── Portrait: scrollable vertical layout ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = Color(0xFFFFD700)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = winnerName,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = winsText,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))

                StatsCard(capturedCells, totalMoves, durationSeconds)

                Spacer(Modifier.height(28.dp))

                if (showPlayAgain) {
                    Raised3DButton(
                        text = Strings.playAgain,
                        onClick = onPlayAgain,
                        mainColor = Color.White,
                        shadowColor = Color(0xFFDDDDDD),
                        textColor = winnerColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                }

                Raised3DButton(
                    text = Strings.menu,
                    onClick = onMenu,
                    mainColor = SecondaryActionColor,
                    shadowColor = SecondaryActionShadow,
                    textColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    capturedCells: Int,
    totalMoves: Int,
    durationSeconds: Long
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 5.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFD0D0D0))
        )
        // Main card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = Strings.gameStatistics,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                StatRow(label = Strings.totalMoves, value = "$totalMoves")
                StatRow(label = Strings.duration, value = formatDuration(durationSeconds))
                StatRow(
                    label = Strings.gridSizeLabel,
                    value = "${GameConfig.gridSize} x ${GameConfig.gridSize}"
                )
                if (GameConfig.gameMode == GameMode.VS_BOT) {
                    StatRow(
                        label = Strings.botDifficulty,
                        value = when (GameConfig.botDifficulty) {
                            BotDifficulty.EASY -> Strings.easy
                            BotDifficulty.MEDIUM -> Strings.medium
                            BotDifficulty.HARD -> Strings.hard
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins}:${secs.toString().padStart(2, '0')}"
}
