package com.blueedge.chainreaction.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blueedge.chainreaction.data.model.Player

/**
 * Score bar showing player territories
 */
@Composable
fun ScoreBar(
    players: List<Player>,
    player1Cells: Int,
    player2Cells: Int,
    modifier: Modifier = Modifier
) {
    val totalCells = player1Cells + player2Cells
    val player1Percentage = if (totalCells > 0) {
        player1Cells.toFloat() / totalCells
    } else 0.5f
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${players.getOrNull(0)?.name ?: "Player 1"}: $player1Cells",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = players.getOrNull(0)?.color ?: MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${players.getOrNull(1)?.name ?: "Player 2"}: $player2Cells",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = players.getOrNull(1)?.color ?: MaterialTheme.colorScheme.error
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Progress bar showing territory distribution
        Row(modifier = Modifier.fillMaxWidth()) {
            if (player1Cells > 0) {
                Box(
                    modifier = Modifier
                        .weight(player1Percentage)
                        .height(8.dp)
                        .background(
                            players.getOrNull(0)?.color ?: MaterialTheme.colorScheme.primary
                        )
                )
            }
            if (player2Cells > 0) {
                Box(
                    modifier = Modifier
                        .weight(1f - player1Percentage)
                        .height(8.dp)
                        .background(
                            players.getOrNull(1)?.color ?: MaterialTheme.colorScheme.error
                        )
                )
            }
        }
    }
}
