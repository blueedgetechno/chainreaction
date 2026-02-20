package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.ui.theme.PlayerColors

@Composable
fun GameEndScreen(
    winnerId: Int,
    player1Score: Int,
    player2Score: Int,
    totalMoves: Int,
    durationSeconds: Long,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    val players = GameConfig.getPlayers()
    val winner = players.firstOrNull { it.id == winnerId }
    val winnerName = winner?.name ?: "Player $winnerId"
    val winnerColor = PlayerColors.getOrElse((winner?.colorIndex ?: 0)) { PlayerColors[0] }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        winnerColor.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.background,
                        winnerColor.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Victory emoji
            Text(
                text = "👑",
                fontSize = 72.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "$winnerName\nWins!",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = winnerColor,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp
            )

            Spacer(Modifier.height(36.dp))

            // Stats card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Game Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider()

                    StatRow(
                        label = "Final Score",
                        value = "${GameConfig.player1Name}: $player1Score  |  ${GameConfig.player2Name}: $player2Score"
                    )
                    StatRow(
                        label = "Total Moves",
                        value = "$totalMoves"
                    )
                    StatRow(
                        label = "Duration",
                        value = formatDuration(durationSeconds)
                    )
                    StatRow(
                        label = "Grid Size",
                        value = "${GameConfig.gridSize}×${GameConfig.gridSize}"
                    )
                    if (GameConfig.gameMode == com.blueedge.chainreaction.data.GameMode.VS_BOT) {
                        StatRow(
                            label = "Bot Difficulty",
                            value = GameConfig.botDifficulty.name.lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // Play Again button — 3D raised style
            Raised3DButton(
                text = "Play Again",
                onClick = onPlayAgain,
                mainColor = winnerColor,
                shadowColor = winnerColor.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Main Menu button — 3D raised style
            Raised3DButton(
                text = "Main Menu",
                onClick = onMainMenu,
                mainColor = Color(0xFFD4956B),
                shadowColor = Color(0xFFB07A52),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Raised3DButton(
    text: String,
    onClick: () -> Unit,
    mainColor: Color,
    shadowColor: Color,
    modifier: Modifier = Modifier,
    shadowHeight: Dp = 6.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val yOffset by animateDpAsState(
        targetValue = if (isPressed) shadowHeight else 0.dp,
        animationSpec = tween(durationMillis = 80),
        label = "buttonPress"
    )

    Box(
        modifier = modifier.height(66.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(18.dp))
                .background(shadowColor)
        )
        // Main button layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .offset(y = yOffset)
                .clip(RoundedCornerShape(18.dp))
                .background(mainColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
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
    return String.format("%d:%02d", mins, secs)
}
