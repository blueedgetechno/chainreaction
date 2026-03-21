package com.blueedge.chainreaction.ui.screens

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
import com.blueedge.chainreaction.data.Strings
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
    val isOnlineMode = GameConfig.gameMode == GameMode.ONLINE_MULTIPLAYER
    val winnerName = when {
        isBotMode -> if (winner?.isBot == true) Strings.bot else Strings.you
        isOnlineMode -> Strings.colorName(winner?.colorIndex ?: 0)
        else -> Strings.colorName(winner?.colorIndex ?: 0)
    }
    val winsText = if (isBotMode || isOnlineMode) Strings.won else Strings.wins
    val winnerColor = PlayerColors.getOrElse((winner?.colorIndex ?: 0)) { PlayerColors[0] }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(winnerColor)
    ) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            // --- Landscape layout: winner info left, stats + buttons right ---
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
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
                    Text(
                        text = "\uD83C\uDFC6",
                        fontSize = 56.sp
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
                    // Stats card
                    StatsCard(capturedCells, totalMoves, durationSeconds)

                    Spacer(Modifier.height(24.dp))

                    if (!isOnlineMode) {
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
                        text = Strings.mainMenu,
                        onClick = onMainMenu,
                        mainColor = SecondaryActionColor,
                        shadowColor = SecondaryActionShadow,
                        textColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // --- Portrait layout (original, now scrollable) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
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

                // Stats card
                StatsCard(capturedCells, totalMoves, durationSeconds)

                Spacer(Modifier.height(40.dp))

                if (!isOnlineMode) {
                    // Play Again button — 3D raised style
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

                // Main Menu button — 3D raised style
                Raised3DButton(
                    text = Strings.mainMenu,
                    onClick = onMainMenu,
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

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                StatRow(
                    label = Strings.finalScore,
                    value = "$capturedCells"
                )
                StatRow(
                    label = Strings.totalMoves,
                    value = "$totalMoves"
                )
                StatRow(
                    label = Strings.duration,
                    value = formatDuration(durationSeconds)
                )
                StatRow(
                    label = Strings.gridSizeLabel,
                    value = "${GameConfig.gridSize} x ${GameConfig.gridSize}"
                )
                if (GameConfig.gameMode == com.blueedge.chainreaction.data.GameMode.VS_BOT) {
                    StatRow(
                        label = Strings.botDifficulty,
                        value = when (GameConfig.botDifficulty) {
                            com.blueedge.chainreaction.data.BotDifficulty.EASY -> Strings.easy
                            com.blueedge.chainreaction.data.BotDifficulty.MEDIUM -> Strings.medium
                            com.blueedge.chainreaction.data.BotDifficulty.HARD -> Strings.hard
                        }
                    )
                }
            }
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
