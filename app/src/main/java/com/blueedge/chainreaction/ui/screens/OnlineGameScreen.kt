package com.blueedge.chainreaction.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blueedge.chainreaction.ads.InterstitialAdManager
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.GameMode
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.data.Strings
import com.blueedge.chainreaction.ui.components.GameGrid
import com.blueedge.chainreaction.ui.components.Raised3DButton
import com.blueedge.chainreaction.ui.game.OnlineGameViewModel
import com.blueedge.chainreaction.ui.theme.PlayerColors
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
import com.blueedge.chainreaction.ui.theme.SecondaryActionShadow
import kotlin.math.sqrt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnlineGameScreen(
    action: String,
    roomCode: String,
    onGameEnd: (winnerId: Int, capturedCells: Int, moves: Int, duration: Long) -> Unit,
    onExit: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: OnlineGameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Trigger room action based on navigation parameter
    LaunchedEffect(action, roomCode) {
        when (action) {
            "create" -> viewModel.createRoom()
            "join" -> viewModel.joinRoom(roomCode)
            "random" -> viewModel.findRandomMatch()
        }
    }

    // Preload interstitial ad
    LaunchedEffect(Unit) {
        InterstitialAdManager.load(context)
    }

    // Show ad after game over
    LaunchedEffect(state.gameStatus) {
        if (state.gameStatus == GameStatus.GAME_OVER) {
            if (!InterstitialAdManager.isReady()) {
                InterstitialAdManager.load(context)
            }
            delay(500)
            (context as? Activity)?.let { activity ->
                InterstitialAdManager.show(activity)
            }
        }
    }

    // Resume game when returning from settings
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (viewModel.state.value.isPaused) {
                viewModel.resumeGame()
            }
        }
    }

    val playerColors = state.players.map { player ->
        PlayerColors.getOrElse(player.colorIndex) { PlayerColors[player.id - 1] }
    }
    val currentPlayerColor = playerColors.getOrElse(state.currentPlayerId - 1) { Color(0xFF41AFD4) }

    val bgColor by animateColorAsState(
        targetValue = currentPlayerColor,
        animationSpec = tween(600),
        label = "bgColor"
    )

    var showExitConfirmation by remember { mutableStateOf(false) }

    BackHandler(enabled = state.gameStatus == GameStatus.IN_PROGRESS) {
        showExitConfirmation = true
    }

    // Waiting for opponent screen
    if (state.waitingForOpponent) {
        WaitingForOpponentOverlay(
            roomCode = state.roomCode,
            onCancel = {
                viewModel.leaveRoom()
                onExit()
            }
        )
        return
    }

    // Opponent disconnected dialog
    if (state.opponentDisconnected && state.gameStatus == GameStatus.IN_PROGRESS) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Opponent Disconnected",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your opponent has left the game.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Raised3DButton(
                        text = "Back to Menu",
                        onClick = {
                            viewModel.leaveRoom()
                            onExit()
                        },
                        mainColor = SecondaryActionColor,
                        shadowColor = SecondaryActionShadow,
                        textColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        val isLandscape = maxWidth > maxHeight

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
                            state.gameStatus == GameStatus.IN_PROGRESS &&
                            state.currentPlayerId == state.localPlayerId,
                    modifier = if (isLandscape) {
                        Modifier
                            .fillMaxHeight()
                            .padding(vertical = 12.dp)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    }
                )
            }
        }

        // Turn indicator
        if (state.gameStatus == GameStatus.IN_PROGRESS) {
            key(state.currentPlayerId) {
                val turnIndicatorScale = remember { Animatable(0f) }
                val turnIndicatorAlpha = remember { Animatable(0f) }

                LaunchedEffect(Unit) {
                    coroutineScope {
                        launch { turnIndicatorAlpha.animateTo(1f, animationSpec = tween(300)) }
                        turnIndicatorScale.animateTo(1f, animationSpec = tween(300))
                    }
                    delay(1200)
                    coroutineScope {
                        launch { turnIndicatorAlpha.animateTo(0f, animationSpec = tween(300)) }
                        turnIndicatorScale.animateTo(0f, animationSpec = tween(300))
                    }
                }

                val isMyTurn = state.currentPlayerId == state.localPlayerId
                val indicatorText = if (isMyTurn) Strings.yourTurn else {
                    val opponentName = state.opponentName.ifEmpty { "Opponent" }
                    "$opponentName's turn"
                }
                val showAtBottom = isMyTurn

                val borderWidth = 8.dp

                if (isLandscape) {
                    val showAtRight = showAtBottom
                    val indicatorAlignment = if (showAtRight) Alignment.CenterEnd else Alignment.CenterStart
                    val originX = if (showAtRight) 1f else 0f
                    val edgeOffset = if (showAtRight) borderWidth else -borderWidth

                    BoxWithConstraints(
                        modifier = Modifier
                            .align(indicatorAlignment)
                            .offset(x = edgeOffset)
                            .fillMaxHeight(0.85f)
                            .aspectRatio(0.5f)
                            .graphicsLayer {
                                scaleX = turnIndicatorScale.value
                                scaleY = turnIndicatorScale.value
                                alpha = turnIndicatorAlpha.value
                                transformOrigin = TransformOrigin(originX, 0.5f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val cornerRadius = maxWidth
                        val shape = if (showAtRight)
                            RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius)
                        else
                            RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent, shape)
                                .clip(shape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = indicatorText,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.graphicsLayer { rotationZ = if (showAtRight) 90f else -90f }
                            )
                        }
                    }
                } else {
                    val indicatorAlignment = if (showAtBottom) Alignment.BottomCenter else Alignment.TopCenter
                    val originY = if (showAtBottom) 1f else 0f
                    val edgeOffset = if (showAtBottom) borderWidth else -borderWidth

                    BoxWithConstraints(
                        modifier = Modifier
                            .align(indicatorAlignment)
                            .offset(y = edgeOffset)
                            .fillMaxWidth(0.85f)
                            .aspectRatio(2f)
                            .graphicsLayer {
                                scaleX = turnIndicatorScale.value
                                scaleY = turnIndicatorScale.value
                                alpha = turnIndicatorAlpha.value
                                transformOrigin = TransformOrigin(0.5f, originY)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val cornerRadius = maxWidth / 2
                        val shape = if (showAtBottom)
                            RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                        else
                            RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent, shape)
                                .clip(shape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = indicatorText,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // "Waiting for opponent's move" indicator when it's not our turn
        if (state.gameStatus == GameStatus.IN_PROGRESS &&
            state.currentPlayerId != state.localPlayerId &&
            !state.isAnimating
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "waitDots")
            val waitAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "waitAlpha"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .alpha(waitAlpha)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Waiting for opponent...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Settings icon
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 12.dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.85f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.pauseGame()
                        onOpenSettings()
                    }
                ),
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

    // Victory overlay
    if (state.gameStatus == GameStatus.GAME_OVER) {
        val winnerColor = playerColors.getOrElse(state.winnerId - 1) { Color(0xFF41AFD4) }
        val isLocalWinner = state.winnerId == state.localPlayerId
        val winnerName = if (isLocalWinner) Strings.you else state.opponentName.ifEmpty { "Opponent" }
        val winsText = if (isLocalWinner) Strings.won else Strings.wins

        val circleRadius = remember { Animatable(0f) }
        val contentAlpha = remember { Animatable(0f) }
        var screenSize by remember { mutableStateOf(IntSize.Zero) }

        LaunchedEffect(state.gameStatus) {
            val maxRadius = if (screenSize != IntSize.Zero) {
                sqrt((screenSize.width * screenSize.width + screenSize.height * screenSize.height).toDouble()).toFloat()
            } else {
                3000f
            }
            circleRadius.animateTo(targetValue = maxRadius, animationSpec = tween(durationMillis = 700))
            contentAlpha.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 350))
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { screenSize = it },
            contentAlignment = Alignment.Center
        ) {
            val isLandscapeOverlay = maxWidth > maxHeight

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = winnerColor,
                    radius = circleRadius.value,
                    center = Offset(size.width / 2f, size.height / 2f)
                )
            }

            // Stats icon
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 12.dp)
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
                    .alpha(contentAlpha.value)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onGameEnd(
                                state.winnerId,
                                state.capturedCells,
                                state.moveCount,
                                viewModel.getGameDurationSeconds()
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = "View Stats",
                    modifier = Modifier.size(22.dp)
                )
            }

            if (isLandscapeOverlay) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(32.dp)
                        .alpha(contentAlpha.value)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "\uD83C\uDFC6", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
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
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Raised3DButton(
                            text = Strings.menu,
                            textColor = Color.White,
                            mainColor = SecondaryActionColor,
                            shadowColor = SecondaryActionShadow,
                            onClick = {
                                viewModel.leaveRoom()
                                onExit()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(32.dp)
                        .alpha(contentAlpha.value)
                ) {
                    Text(text = "\uD83C\uDFC6", fontSize = 80.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = winnerName,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = winsText,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Raised3DButton(
                        text = Strings.menu,
                        textColor = Color.White,
                        mainColor = SecondaryActionColor,
                        shadowColor = SecondaryActionShadow,
                        onClick = {
                            viewModel.leaveRoom()
                            onExit()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Strings.exitGameQ,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Leaving will forfeit the game.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val cancelInteractionSource = remember { MutableInteractionSource() }
                        val isCancelPressed by cancelInteractionSource.collectIsPressedAsState()
                        val cancelShadowHeight = 4.dp
                        val cancelYOffset by animateDpAsState(
                            targetValue = if (isCancelPressed) cancelShadowHeight else 0.dp,
                            animationSpec = tween(durationMillis = 80),
                            label = "cancelPress"
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .offset(y = cancelShadowHeight)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFB0B0B0))
                            )
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
                                    text = Strings.cancel,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                            }
                        }

                        val exitInteractionSource = remember { MutableInteractionSource() }
                        val isExitPressed by exitInteractionSource.collectIsPressedAsState()
                        val exitShadowHeight = 4.dp
                        val exitYOffset by animateDpAsState(
                            targetValue = if (isExitPressed) exitShadowHeight else 0.dp,
                            animationSpec = tween(durationMillis = 80),
                            label = "exitPress"
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .offset(y = exitShadowHeight)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFC55550))
                            )
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
                                            viewModel.leaveRoom()
                                            onExit()
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Strings.exit,
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

@Composable
private fun WaitingForOpponentOverlay(
    roomCode: String,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waitPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waitPulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "⏳",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Waiting for Opponent",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            if (roomCode.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Room Code",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF888888)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = roomCode,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 8.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(pulseAlpha)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Waiting for opponent to join...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Raised3DButton(
                text = "Cancel",
                onClick = onCancel,
                mainColor = SecondaryActionColor,
                shadowColor = SecondaryActionShadow,
                textColor = Color.White,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    }
}
