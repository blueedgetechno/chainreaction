package com.blueedge.chainreaction.presentation.game

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blueedge.chainreaction.data.model.GameMode

/**
 * Game board screen - main gameplay screen
 * This is a placeholder that will be fully implemented in Phase 4
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameBoardScreen(
    gridSize: Int,
    gameMode: GameMode,
    onNavigateToEnd: (winnerId: Int, moves: Int, duration: Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chain Reaction",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Game Board (${gridSize}x${gridSize})\nMode: ${gameMode.name}\n\nTo be implemented in Phase 4",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
