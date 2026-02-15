package com.blueedge.chainreaction.presentation.setup

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blueedge.chainreaction.data.model.GameMode

/**
 * Game setup screen for configuring game parameters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSetupScreen(
    gameMode: GameMode,
    onNavigateToGame: (gridSize: Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedGridSize by remember { mutableStateOf(6) }
    val gridSizeOptions = listOf(5, 6, 7, 8, 10)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (gameMode.isBot) "Play vs Bot" else "Local Multiplayer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Grid Size Selection
            Text(
                text = "Select Grid Size",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Grid size options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                gridSizeOptions.forEach { size ->
                    GridSizeOption(
                        size = size,
                        isSelected = selectedGridSize == size,
                        onClick = { selectedGridSize = size }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Grid Preview
            Text(
                text = "${selectedGridSize}x${selectedGridSize} Grid Preview",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Preview grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(selectedGridSize),
                modifier = Modifier
                    .size(280.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline
                    ),
                userScrollEnabled = false
            ) {
                items(selectedGridSize * selectedGridSize) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bot difficulty selection (if bot mode)
            if (gameMode.isBot) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Bot Difficulty: ${gameMode.difficulty?.name ?: "EASY"}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (gameMode.difficulty) {
                                com.blueedge.chainreaction.data.model.BotDifficulty.EASY -> 
                                    "Bot makes random valid moves"
                                com.blueedge.chainreaction.data.model.BotDifficulty.MEDIUM -> 
                                    "Bot uses defensive strategy"
                                com.blueedge.chainreaction.data.model.BotDifficulty.HARD -> 
                                    "Bot uses minimax algorithm"
                                else -> "Bot makes random valid moves"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Start Game Button
            Button(
                onClick = { onNavigateToGame(selectedGridSize) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "START GAME",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun GridSizeOption(
    size: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        modifier = Modifier.size(60.dp)
    ) {
        Text(
            text = "${size}x${size}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}
