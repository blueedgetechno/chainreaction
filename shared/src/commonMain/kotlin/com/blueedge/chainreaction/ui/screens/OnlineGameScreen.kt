package com.blueedge.chainreaction.ui.screens

import com.blueedge.chainreaction.platform.PlatformBackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
import com.blueedge.chainreaction.platform.LocalAdManager
import com.blueedge.chainreaction.platform.LocalSoundPlayer
import com.blueedge.chainreaction.platform.currentTimeMillis
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.sqrt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineGameScreen(
    action: String,
    roomCode: String,
    onExit: () -> Unit,
    viewModel: OnlineGameViewModel = viewModel(factory = viewModelFactory { initializer { OnlineGameViewModel() } })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val adManager = LocalAdManager.current

    // Trigger room action based on navigation parameter
    LaunchedEffect(action, roomCode) {
        when (action) {
            "create" -> viewModel.createRoom()
            "join" -> viewModel.joinRoom(roomCode)
            "random" -> viewModel.findRandomMatch()
            "matched" -> viewModel.attachToRoom(roomCode)
        }
    }

    // Preload interstitial ad
    LaunchedEffect(Unit) {
        adManager.load()
    }

    // Show ad after game over
    LaunchedEffect(state.gameStatus) {
        if (state.gameStatus == GameStatus.GAME_OVER) {
            if (!adManager.isReady()) {
                adManager.load()
            }
            delay(500)
            adManager.show {}
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
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showExitFromSettingsConfirmation by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pendingExit by remember { mutableStateOf(false) }

    // Deferred exit: dialog fades out first, then navigate
    LaunchedEffect(pendingExit) {
        if (pendingExit) {
            delay(200)
            viewModel.leaveRoom()
            onExit()
        }
    }

    PlatformBackHandler(enabled = state.gameStatus == GameStatus.IN_PROGRESS) {
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
                        text = Strings.opponentLeft,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = Strings.opponentLeftMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Exit button
                        val exitInteractionSource = remember { MutableInteractionSource() }
                        val isExitPressed by exitInteractionSource.collectIsPressedAsState()
                        val exitShadowHeight = 4.dp
                        val exitYOffset by animateDpAsState(
                            targetValue = if (isExitPressed) exitShadowHeight else 0.dp,
                            animationSpec = tween(durationMillis = 80),
                            label = "disconnectExitPress"
                        )
                        // Resume w/ Bot button
                        val botInteractionSource = remember { MutableInteractionSource() }
                        val isBotPressed by botInteractionSource.collectIsPressedAsState()
                        val botShadowHeight = 4.dp
                        val botYOffset by animateDpAsState(
                            targetValue = if (isBotPressed) botShadowHeight else 0.dp,
                            animationSpec = tween(durationMillis = 80),
                            label = "disconnectBotPress"
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .offset(y = botShadowHeight)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF388E3C))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .offset(y = botYOffset)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF4CAF50))
                                    .clickable(
                                        interactionSource = botInteractionSource,
                                        indication = null,
                                        onClick = {
                                            viewModel.switchToBot()
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Strings.playWithBot,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
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

        val isMyTurn = state.currentPlayerId == state.localPlayerId
        var showTurnSkipped by remember { mutableStateOf(false) }
        var skippedWasMyTurn by remember { mutableStateOf(false) }

        // Skip indicator at indicator position (replaces center overlay)
        if (showTurnSkipped) {
            val skippedAlpha = remember(showTurnSkipped) { Animatable(1f) }
            LaunchedEffect(Unit) {
                delay(900)
                skippedAlpha.animateTo(0f, tween(300))
                showTurnSkipped = false
            }
            val skipText = if (skippedWasMyTurn) "Your turn\nskipped" else "Their turn\nskipped"
            val showAtBottom = skippedWasMyTurn
            val borderWidth = 8.dp

            if (isLandscape) {
                val showAtRight = showAtBottom
                val indicatorAlignment = if (showAtRight) Alignment.CenterEnd else Alignment.CenterStart
                val edgeOffset = if (showAtRight) borderWidth else -borderWidth
                BoxWithConstraints(
                    modifier = Modifier
                        .align(indicatorAlignment)
                        .offset(x = edgeOffset)
                        .fillMaxHeight(0.85f)
                        .aspectRatio(0.5f)
                        .alpha(skippedAlpha.value),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                val strokeW = borderWidth.toPx()
                                val arcSide = size.height - strokeW
                                drawArc(
                                    color = Color.White,
                                    startAngle = if (showAtRight) 90f else -90f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = if (showAtRight)
                                        Offset(size.width - arcSide / 2 - strokeW / 2, strokeW / 2)
                                    else
                                        Offset(-arcSide / 2 + strokeW / 2, strokeW / 2),
                                    size = androidx.compose.ui.geometry.Size(arcSide, arcSide),
                                    style = Stroke(width = strokeW)
                                )
                                drawContent()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = skipText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer { rotationZ = if (showAtRight) 90f else -90f }
                        )
                    }
                }
            } else {
                val indicatorAlignment = if (showAtBottom) Alignment.BottomCenter else Alignment.TopCenter
                val edgeOffset = if (showAtBottom) borderWidth else -borderWidth
                BoxWithConstraints(
                    modifier = Modifier
                        .align(indicatorAlignment)
                        .offset(y = edgeOffset)
                        .fillMaxWidth(0.85f)
                        .aspectRatio(2f)
                        .alpha(skippedAlpha.value),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                val strokeW = borderWidth.toPx()
                                val arcSide = size.width - strokeW
                                drawArc(
                                    color = Color.White,
                                    startAngle = if (showAtBottom) 180f else 0f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = if (showAtBottom)
                                        Offset(strokeW / 2, size.height - arcSide / 2 - strokeW / 2)
                                    else
                                        Offset(strokeW / 2, -arcSide / 2 + strokeW / 2),
                                    size = androidx.compose.ui.geometry.Size(arcSide, arcSide),
                                    style = Stroke(width = strokeW)
                                )
                                drawContent()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = skipText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── Timer-based turn indicator (online, not bot mode) ───────────
        if (!state.isBotMode && state.gameStatus == GameStatus.IN_PROGRESS) {

            // Derive countdown from ViewModel deadline so it survives navigation
            var countdown by remember { mutableIntStateOf(30) }
            var timerFraction by remember { mutableFloatStateOf(1f) }

            val animatedFraction by animateFloatAsState(
                targetValue = timerFraction,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
                label = "timerFraction"
            )

            // Countdown driven by deadline — pauses on disconnect
            LaunchedEffect(state.turnDeadlineMs) {
                if (state.turnDeadlineMs <= 0L) return@LaunchedEffect
                while (true) {
                    if (state.opponentDisconnected) {
                        delay(200)
                        continue
                    }
                    val remainingMs = (state.turnDeadlineMs - currentTimeMillis()).coerceAtLeast(0)
                    val secs = (remainingMs / 1000).toInt()
                    countdown = secs
                    timerFraction = secs / 30f
                    if (secs <= 0) {
                        skippedWasMyTurn = isMyTurn
                        showTurnSkipped = true
                        viewModel.skipTurn()
                        break
                    }
                    delay(500)
                }
            }

            if (!state.isAnimating) {
                val indicatorText = if (isMyTurn) {
                    "${Strings.yourTurn} ($countdown)"
                } else {
                    "${Strings.theirTurn} ($countdown)"
                }
                val showAtBottom = isMyTurn
                val borderWidth = 8.dp

                val turnIndicatorScale = remember(state.currentPlayerId) { Animatable(0f) }
                LaunchedEffect(state.currentPlayerId) {
                    turnIndicatorScale.animateTo(1f, animationSpec = tween(300))
                }

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
                                transformOrigin = TransformOrigin(originX, 0.5f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawWithContent {
                                    val strokeW = borderWidth.toPx()
                                    val arcSide = size.height - strokeW
                                    val sweepAngle = 180f * animatedFraction
                                    // Backdrop arc (dim)
                                    drawArc(
                                        color = Color.White.copy(alpha = 0.3f),
                                        startAngle = if (showAtRight) 90f else -90f,
                                        sweepAngle = 180f,
                                        useCenter = false,
                                        topLeft = if (showAtRight)
                                            Offset(size.width - arcSide / 2 - strokeW / 2, strokeW / 2)
                                        else
                                            Offset(-arcSide / 2 + strokeW / 2, strokeW / 2),
                                        size = androidx.compose.ui.geometry.Size(arcSide, arcSide),
                                        style = Stroke(width = strokeW)
                                    )
                                    // Timer arc (bright)
                                    drawArc(
                                        color = Color.White,
                                        startAngle = if (showAtRight) 90f else -90f,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        topLeft = if (showAtRight)
                                            Offset(size.width - arcSide / 2 - strokeW / 2, strokeW / 2)
                                        else
                                            Offset(-arcSide / 2 + strokeW / 2, strokeW / 2),
                                        size = androidx.compose.ui.geometry.Size(arcSide, arcSide),
                                        style = Stroke(width = strokeW)
                                    )
                                    drawContent()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = indicatorText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
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
                                .clip(shape)
                                .drawWithContent {
                                    drawContent()
                                    val strokeW = borderWidth.toPx()
                                    val arcSide = size.width - strokeW
                                    val sweepAngle = 180f * animatedFraction
                                    // Backdrop arc (dim)
                                    drawArc(
                                        color = Color.White.copy(alpha = 0.3f),
                                        startAngle = if (showAtBottom) 180f else 0f,
                                        sweepAngle = 180f,
                                        useCenter = false,
                                        topLeft = if (showAtBottom)
                                            Offset(strokeW / 2, size.height - arcSide / 2 - strokeW / 2)
                                        else
                                            Offset(strokeW / 2, -arcSide / 2 + strokeW / 2),
                                        size = androidx.compose.ui.geometry.Size(arcSide, arcSide),
                                        style = Stroke(width = strokeW)
                                    )
                                    // Timer arc (bright)
                                    drawArc(
                                        color = Color.White,
                                        startAngle = if (showAtBottom) 180f else 0f,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        topLeft = if (showAtBottom)
                                            Offset(strokeW / 2, size.height - arcSide / 2 - strokeW / 2)
                                        else
                                            Offset(strokeW / 2, -arcSide / 2 + strokeW / 2),
                                        size = androidx.compose.ui.geometry.Size(arcSide, arcSide),
                                        style = Stroke(width = strokeW)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = indicatorText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ── Bot mode: simple fade-in/out indicator (no timer) ───────────
        if (state.isBotMode && state.gameStatus == GameStatus.IN_PROGRESS) {
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

                val indicatorText = if (isMyTurn) Strings.yourTurn else Strings.botsTurn
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
                            .alpha(turnIndicatorAlpha.value)
                            .graphicsLayer {
                                scaleX = turnIndicatorScale.value
                                scaleY = turnIndicatorScale.value
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
                                .clip(shape)
                                .border(borderWidth, Color.White, shape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = indicatorText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
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
                            .alpha(turnIndicatorAlpha.value)
                            .graphicsLayer {
                                scaleX = turnIndicatorScale.value
                                scaleY = turnIndicatorScale.value
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
                                .clip(shape)
                                .border(borderWidth, Color.White, shape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = indicatorText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color.White
                            )
                        }
                    }
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
                        showSettingsSheet = true
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

    // ── Settings Bottom Sheet ───────────────────────────────────────
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = Strings.gameSettings,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                // Music & Sound toggles
                val soundPlayer = LocalSoundPlayer.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SettingsToggleButton(
                        iconEnabled = Icons.Default.MusicNote,
                        iconDisabled = Icons.Default.MusicOff,
                        label = Strings.music,
                        enabled = GameConfig.musicEnabled,
                        onToggle = {
                            GameConfig.musicEnabled = !GameConfig.musicEnabled
                            soundPlayer.onMusicToggled()
                        }
                    )
                    SettingsToggleButton(
                        iconEnabled = Icons.Default.VolumeUp,
                        iconDisabled = Icons.Default.VolumeOff,
                        label = Strings.sound,
                        enabled = GameConfig.soundEnabled,
                        onToggle = { GameConfig.soundEnabled = !GameConfig.soundEnabled }
                    )
                }

                Raised3DButton(
                    text = Strings.exitToMenu,
                    onClick = {
                        showSettingsSheet = false
                        showExitFromSettingsConfirmation = true
                    },
                    mainColor = Color(0xFFEA695E),
                    shadowColor = Color(0xFFC55550),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            } // end Box
        }
    }

    // Exit from settings confirmation dialog
    if (showExitFromSettingsConfirmation) {
        Dialog(onDismissRequest = { showExitFromSettingsConfirmation = false }) {
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
                        text = Strings.exitToMenuQ,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = Strings.exitMessage,
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
                            label = "settingsExitCancelPress"
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
                                        onClick = { showExitFromSettingsConfirmation = false }
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

                        val confirmInteractionSource = remember { MutableInteractionSource() }
                        val isConfirmPressed by confirmInteractionSource.collectIsPressedAsState()
                        val confirmShadowHeight = 4.dp
                        val confirmYOffset by animateDpAsState(
                            targetValue = if (isConfirmPressed) confirmShadowHeight else 0.dp,
                            animationSpec = tween(durationMillis = 80),
                            label = "settingsExitConfirmPress"
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .offset(y = confirmShadowHeight)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFC55550))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .offset(y = confirmYOffset)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFEA695E))
                                    .clickable(
                                        interactionSource = confirmInteractionSource,
                                        indication = null,
                                        onClick = {
                                            showExitFromSettingsConfirmation = false
                                            pendingExit = true
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

    // Victory overlay
    if (state.gameStatus == GameStatus.GAME_OVER) {
        val winnerColor = playerColors.getOrElse(state.winnerId - 1) { Color(0xFF41AFD4) }
        val isLocalWinner = state.winnerId == state.localPlayerId
        val isBotMode = state.isBotMode
        val winnerName = when {
            isBotMode -> {
                val winnerPlayer = state.players.firstOrNull { it.id == state.winnerId }
                if (winnerPlayer?.isBot == true) Strings.bot else Strings.you
            }
            isLocalWinner -> Strings.you
            else -> state.opponentName.ifEmpty { "Opponent" }
        }
        val winsText = if (isBotMode) {
            val winnerPlayer = state.players.firstOrNull { it.id == state.winnerId }
            if (winnerPlayer?.isBot == true) Strings.wins else Strings.won
        } else {
            if (isLocalWinner) Strings.won else Strings.wins
        }

        VictoryScreen(
            winnerName = winnerName,
            winsText = winsText,
            winnerColor = winnerColor,
            capturedCells = state.capturedCells,
            totalMoves = state.moveCount,
            durationSeconds = viewModel.getGameDurationSeconds(),
            showPlayAgain = false,
            onPlayAgain = {},
            onMenu = {
                viewModel.leaveRoom()
                onExit()
            }
        )
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
                                            pendingExit = true
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

            // if (roomCode.isNotEmpty()) {
            //     Spacer(modifier = Modifier.height(16.dp))

            //     Text(
            //         text = "Room Code",
            //         style = MaterialTheme.typography.bodyLarge,
            //         color = Color(0xFF888888)
            //     )

            //     Spacer(modifier = Modifier.height(8.dp))

            //     Text(
            //         text = roomCode,
            //         style = MaterialTheme.typography.displaySmall,
            //         fontWeight = FontWeight.Black,
            //         color = MaterialTheme.colorScheme.primary,
            //         letterSpacing = 8.sp
            //     )
            // }

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

@Composable
private fun SettingsToggleButton(
    iconEnabled: ImageVector,
    iconDisabled: ImageVector,
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
        label = "sheetToggleBg"
    )
    val iconColor = if (enabled) Color.White
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            onClick = onToggle,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (enabled) iconEnabled else iconDisabled,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = if (enabled) FontWeight.Bold else FontWeight.Normal
        )
    }
}
