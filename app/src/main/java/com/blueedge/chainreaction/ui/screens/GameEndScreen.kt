package com.blueedge.chainreaction.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val winnerName = if (winnerId == 1) GameConfig.player1Name else GameConfig.player2Name
    val winnerColorIndex = if (winnerId == 1) GameConfig.player1ColorIndex else GameConfig.player2ColorIndex
    val winnerColor = PlayerColors.getOrElse(winnerColorIndex) { PlayerColors[0] }

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

            // Play Again button
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = winnerColor
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    "Play Again",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // Main Menu button
            OutlinedButton(
                onClick = onMainMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Main Menu",
                    style = MaterialTheme.typography.titleMedium
                )
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
    return String.format("%d:%02d", mins, secs)
}
