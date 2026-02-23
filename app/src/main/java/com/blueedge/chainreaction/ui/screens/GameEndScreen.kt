package com.blueedge.chainreaction.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.GameMode
import com.blueedge.chainreaction.ui.components.Raised3DButton
import com.blueedge.chainreaction.ui.theme.PlayerColorNames
import com.blueedge.chainreaction.ui.theme.PlayerColors
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
import com.blueedge.chainreaction.ui.theme.SecondaryActionShadow

@Composable
fun GameEndScreen(
    winnerId: Int,
    capturedCells: Int,
    totalMoves: Int,
    durationSeconds: Long,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    val players = GameConfig.getPlayers()
    val winner = players.firstOrNull { it.id == winnerId }
    val isBotMode = GameConfig.gameMode == GameMode.VS_BOT
    val winnerName = if (isBotMode) {
        if (winner?.isBot == true) "Bot" else "You"
    } else {
        PlayerColorNames.getOrElse(winner?.colorIndex ?: 0) { "Player $winnerId" }
    }
    val winsText = if (isBotMode) "Won!" else "Wins!"
    val winnerColor = PlayerColors.getOrElse((winner?.colorIndex ?: 0)) { PlayerColors[0] }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(winnerColor)
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
                text = "\uD83C\uDFC6",
                fontSize = 72.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "$winnerName\n$winsText",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp
            )

            Spacer(Modifier.height(36.dp))

            // Stats card with shadow
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
                        text = "Game Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    StatRow(
                        label = "Final Score",
                        value = "$capturedCells"
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
                        value = "${GameConfig.gridSize} x ${GameConfig.gridSize}"
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
            }

            Spacer(Modifier.height(40.dp))

            // Play Again button — 3D raised style
            Raised3DButton(
                text = "Play Again",
                onClick = onPlayAgain,
                mainColor = Color.White,
                shadowColor = Color(0xFFDDDDDD),
                textColor = winnerColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Main Menu button — 3D raised style
            Raised3DButton(
                text = "Main Menu",
                onClick = onMainMenu,
                mainColor = SecondaryActionColor,
                shadowColor = SecondaryActionShadow,
                textColor = Color.White,
                modifier = Modifier.fillMaxWidth()
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
