package com.blueedge.chainreaction.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.ui.components.GameGrid
import com.blueedge.chainreaction.ui.game.GameViewModel
import com.blueedge.chainreaction.ui.theme.PlayerColors
import kotlin.math.sqrt

@Composable
fun GameBoardScreen(
    onGameEnd: (winnerId: Int, p1Score: Int, p2Score: Int, moves: Int, duration: Long) -> Unit,
    onExit: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val playerColors = state.players.map { player ->
        PlayerColors.getOrElse(player.colorIndex) { PlayerColors[player.id - 1] }
    }
    val currentPlayerColor = playerColors.getOrElse(state.currentPlayerId - 1) { Color(0xFF41AFD4) }

    // Vivid background that transitions between player colors
    val bgColor by animateColorAsState(
        targetValue = currentPlayerColor,
        animationSpec = tween(600),
        label = "bgColor"
    )

    var showExitConfirmation by remember { mutableStateOf(false) }

    // Handle back button press
    BackHandler(enabled = state.gameStatus == GameStatus.IN_PROGRESS) {
        showExitConfirmation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Grid centered
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (state.board.isNotEmpty()) {
                GameGrid(
                    board = state.board,
                    gridSize = state.gridSize,
                    currentPlayerId = state.currentPlayerId,
                    playerColors = playerColors,
                    explodingCells = state.explodingCells,
                    explosionMoves = state.explosionMoves,
                    onCellClick = { row, col -> viewModel.onCellClicked(row, col) },
                    isInteractionEnabled = !state.isAnimating &&
                            !state.botThinking &&
                            state.gameStatus == GameStatus.IN_PROGRESS,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )
            }
        }

        // Settings icon — top right, below status bar
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 12.dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.85f), CircleShape)
                .clickable { onOpenSettings() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color(0xFF333333),
                modifier = Modifier.size(22.dp)
            )
        }
    }

    // Full-screen victory overlay with circular fill animation
    if (state.gameStatus == GameStatus.GAME_OVER) {
        val winnerColor = playerColors.getOrElse(state.winnerId - 1) { Color(0xFF41AFD4) }
        val winnerName = state.players.firstOrNull { it.id == state.winnerId }?.name
            ?: "Player ${state.winnerId}"

        // Circular reveal animation
        val circleRadius = remember { Animatable(0f) }
        val contentAlpha = remember { Animatable(0f) }
        var screenSize by remember { mutableStateOf(IntSize.Zero) }

        LaunchedEffect(state.gameStatus) {
            // Animate circle expanding to cover full screen
            // Diagonal of the screen = sqrt(w² + h²). We use 3000f as a safe fallback
            // for when screen size hasn't been reported yet (covers all common display resolutions).
            val maxRadius = if (screenSize != IntSize.Zero) {
                sqrt((screenSize.width * screenSize.width + screenSize.height * screenSize.height).toDouble()).toFloat()
            } else {
                3000f
            }
            circleRadius.animateTo(
                targetValue = maxRadius,
                animationSpec = tween(durationMillis = 700)
            )
            // Fade in content after circle fills screen
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { screenSize = it },
            contentAlignment = Alignment.Center
        ) {
            // Animated circular fill canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = winnerColor,
                    radius = circleRadius.value,
                    center = Offset(size.width / 2f, size.height / 2f)
                )
            }

            // Victory content fades in after circle fills
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(32.dp)
                    .alpha(contentAlpha.value)
            ) {
                Text(
                    text = "\uD83C\uDFC6",
                    fontSize = 80.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = winnerName,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "WINS!",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Play Again button
                VictoryRaisedButton(
                    text = "Play Again",
                    mainColor = Color.White.copy(alpha = 0.98f),
                    shadowColor = Color.Black.copy(alpha = 0.25f),
                    textColor = winnerColor,
                    onClick = {
                        onGameEnd(
                            state.winnerId,
                            state.player1Score,
                            state.player2Score,
                            state.moveCount,
                            viewModel.getGameDurationSeconds()
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats button
                VictoryRaisedButton(
                    text = "View Stats",
                    mainColor = Color.White.copy(alpha = 0.5f),
                    shadowColor = Color.Black.copy(alpha = 0.25f),
                    textColor = Color.White,
                    onClick = {
                        onGameEnd(
                            state.winnerId,
                            state.player1Score,
                            state.player2Score,
                            state.moveCount,
                            viewModel.getGameDurationSeconds()
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Menu button
                VictoryRaisedButton(
                    text = "Menu",
                    mainColor = Color.White.copy(alpha = 0.3f),
                    shadowColor = Color.Black.copy(alpha = 0.25f),
                    textColor = Color.White,
                    onClick = { onExit() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Exit confirmation dialog
    if (showExitConfirmation) {
        Dialog(onDismissRequest = { showExitConfirmation = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Exit Game?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Are you sure you want to exit? Your game progress will be lost.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel button with shadow
                        val cancelInteractionSource = remember { MutableInteractionSource() }
                        val isCancelPressed by cancelInteractionSource.collectIsPressedAsState()
                        val cancelShadowHeight = 4.dp
                        val cancelYOffset by animateDpAsState(
                            targetValue = if (isCancelPressed) cancelShadowHeight else 0.dp,
                            animationSpec = tween(durationMillis = 80),
                            label = "cancelPress"
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            // Shadow layer
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .offset(y = cancelShadowHeight)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFB0B0B0))
                            )
                            // Main button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .offset(y = cancelYOffset)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFE0E0E0))
                                    .clickable(
                                        interactionSource = cancelInteractionSource,
                                        indication = null,
                                        onClick = { showExitConfirmation = false }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                            }
                        }

                        // Exit button with shadow
                        val exitInteractionSource = remember { MutableInteractionSource() }
                        val isExitPressed by exitInteractionSource.collectIsPressedAsState()
                        val exitShadowHeight = 4.dp
                        val exitYOffset by animateDpAsState(
                            targetValue = if (isExitPressed) exitShadowHeight else 0.dp,
                            animationSpec = tween(durationMillis = 80),
                            label = "exitPress"
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            // Shadow layer
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .offset(y = exitShadowHeight)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFC55550))
                            )
                            // Main button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .offset(y = exitYOffset)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFEA695E))
                                    .clickable(
                                        interactionSource = exitInteractionSource,
                                        indication = null,
                                        onClick = {
                                            showExitConfirmation = false
                                            onExit()
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Exit",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A 3D raised button used on the victory overlay.
 */
@Composable
private fun VictoryRaisedButton(
    text: String,
    mainColor: Color,
    shadowColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shadowHeight = 6.dp
    val yOffset by animateDpAsState(
        targetValue = if (isPressed) shadowHeight else 0.dp,
        animationSpec = tween(durationMillis = 80),
        label = "victoryButtonPress"
    )

    Box(modifier = modifier.height(76.dp)) {
        // Shadow layer (static, always at bottom)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .offset(y = shadowHeight)
                .clip(RoundedCornerShape(20.dp))
                .background(shadowColor)
        )
        // Main button layer (moves down on press)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .offset(y = yOffset)
                .clip(RoundedCornerShape(20.dp))
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
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = textColor
            )
        }
    }
}

