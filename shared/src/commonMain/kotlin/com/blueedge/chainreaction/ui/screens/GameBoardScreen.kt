package com.blueedge.chainreaction.ui.screens

import com.blueedge.chainreaction.platform.PlatformBackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
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
import com.blueedge.chainreaction.ui.game.GameViewModel
import com.blueedge.chainreaction.ui.theme.PlayerColorNames
import com.blueedge.chainreaction.ui.theme.PlayerColors
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
import com.blueedge.chainreaction.ui.theme.SecondaryActionShadow
import com.blueedge.chainreaction.platform.LocalAdManager
import com.blueedge.chainreaction.platform.LocalSoundPlayer
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
fun GameBoardScreen(
    onPlayAgain: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    viewModel: GameViewModel = viewModel(factory = viewModelFactory { initializer { GameViewModel() } })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val adManager = LocalAdManager.current

    // Preload interstitial ad so it's ready when the game ends.
    LaunchedEffect(Unit) {
        adManager.load()
    }

    // Show interstitial ad after victory screen appears
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

    // Vivid background that transitions between player colors
    val bgColor by animateColorAsState(
        targetValue = currentPlayerColor,
        animationSpec = tween(600),
        label = "bgColor"
    )

    var showExitConfirmation by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showExitFromSettingsConfirmation by remember { mutableStateOf(false) }
    var showRestartConfirmation by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pendingExit by remember { mutableStateOf(false) }

    // Deferred exit: dialog fades out first, then navigate
    LaunchedEffect(pendingExit) {
        if (pendingExit) {
            delay(200)
            onExit()
        }
    }

    // Turn indicator animation state
    val isSoloMode = GameConfig.gameMode == GameMode.VS_BOT

    // Handle back button press
    PlatformBackHandler(enabled = state.gameStatus == GameStatus.IN_PROGRESS) {
        showExitConfirmation = true
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        val isLandscape = maxWidth > maxHeight

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

        // Turn indicator semicircles (rendered before settings icon so it appears behind it)
        if (state.gameStatus == GameStatus.IN_PROGRESS) {
            key(state.currentPlayerId) {
                val turnIndicatorScale = remember { Animatable(0f) }
                val turnIndicatorAlpha = remember { Animatable(0f) }

                LaunchedEffect(Unit) {
                    // Animate in: scale + fade run in parallel
                    coroutineScope {
                        launch { turnIndicatorAlpha.animateTo(1f, animationSpec = tween(300)) }
                        turnIndicatorScale.animateTo(1f, animationSpec = tween(300))
                    }
                    delay(1200)
                    // Animate out: scale + fade run in parallel
                    coroutineScope {
                        launch { turnIndicatorAlpha.animateTo(0f, animationSpec = tween(300)) }
                        turnIndicatorScale.animateTo(0f, animationSpec = tween(300))
                    }
                }

                val currentPlayer = state.players.firstOrNull { it.id == state.currentPlayerId }
                val colorIndex = currentPlayer?.colorIndex ?: (state.currentPlayerId - 1)
                val colorName = Strings.colorName(colorIndex)

                val indicatorText: String
                val showAtBottom: Boolean // In portrait: bottom/top; in landscape: reused as right/left

                if (isSoloMode) {
                    val botPlayer = state.players.firstOrNull { it.isBot }
                    val isBotTurn = state.currentPlayerId == botPlayer?.id
                    indicatorText = if (!isBotTurn) Strings.yourTurn else Strings.botsTurn
                    showAtBottom = !isBotTurn
                } else {
                    indicatorText = Strings.playerTurn(colorName)
                    // Alternate position based on move count: even → bottom/right, odd → top/left
                    showAtBottom = (state.moveCount % 2 == 0)
                }

            val borderWidth = 8.dp

            if (isLandscape) {
                // Landscape: semicircles come from left/right sides
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
                            .border(borderWidth, Color.White, shape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = indicatorText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .graphicsLayer { rotationZ = if (showAtRight) 90f else -90f }
                        )
                    }
                }
            } else {
                // Portrait: semicircles come from top/bottom (original behavior)
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
                    // cornerRadius = maxWidth/2 = height (aspectRatio 2:1), making a perfect semicircle
                    val cornerRadius = maxWidth / 2
                    val shape = if (showAtBottom)
                        RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                    else
                        RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent, shape)
                            .border(borderWidth, Color.White, shape),
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

        // Settings icon — top right, below status bar (rendered after turn indicator so it appears on top)
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
                    text = Strings.restartGame,
                    onClick = {
                        showSettingsSheet = false
                        showRestartConfirmation = true
                    },
                    mainColor = Color(0xFF41AFD4),
                    shadowColor = Color(0xFF2E8DAD),
                    modifier = Modifier.fillMaxWidth()
                )

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

    // Restart confirmation dialog
    if (showRestartConfirmation) {
        ConfirmationDialog(
            title = Strings.restartGameQ,
            message = Strings.restartMessage,
            confirmText = Strings.restart,
            confirmColor = Color(0xFF41AFD4),
            confirmShadowColor = Color(0xFF2E8DAD),
            onConfirm = {
                showRestartConfirmation = false
                onRestart()
            },
            onDismiss = { showRestartConfirmation = false }
        )
    }

    // Exit from settings confirmation dialog
    if (showExitFromSettingsConfirmation) {
        ConfirmationDialog(
            title = Strings.exitToMenuQ,
            message = Strings.exitMessage,
            confirmText = Strings.exit,
            confirmColor = Color(0xFFEA695E),
            confirmShadowColor = Color(0xFFC55550),
            onConfirm = {
                showExitFromSettingsConfirmation = false
                pendingExit = true
            },
            onDismiss = { showExitFromSettingsConfirmation = false }
        )
    }

    // Full-screen victory overlay
    if (state.gameStatus == GameStatus.GAME_OVER) {
        val winnerColor = playerColors.getOrElse(state.winnerId - 1) { Color(0xFF41AFD4) }
        val winnerPlayer = state.players.firstOrNull { it.id == state.winnerId }
        val isBotMode = GameConfig.gameMode == GameMode.VS_BOT
        val winnerName = if (isBotMode) {
            if (winnerPlayer?.isBot == true) Strings.bot else Strings.you
        } else {
            val colorIndex = winnerPlayer?.colorIndex ?: (state.winnerId - 1)
            Strings.colorName(colorIndex)
        }
        val winsText = if (isBotMode) Strings.won else Strings.wins

        VictoryScreen(
            winnerName = winnerName,
            winsText = winsText,
            winnerColor = winnerColor,
            capturedCells = state.capturedCells,
            totalMoves = state.moveCount,
            durationSeconds = viewModel.getGameDurationSeconds(),
            showPlayAgain = true,
            onPlayAgain = { onPlayAgain() },
            onMenu = { onExit() }
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Strings.exitGameQ,
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
                                    text = Strings.cancel,
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

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    confirmShadowColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
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
                        label = "confirmDlgCancel"
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
                                    onClick = onDismiss
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
                        label = "confirmDlgConfirm"
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .offset(y = confirmShadowHeight)
                                .clip(RoundedCornerShape(16.dp))
                                .background(confirmShadowColor)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .offset(y = confirmYOffset)
                                .clip(RoundedCornerShape(16.dp))
                                .background(confirmColor)
                                .clickable(
                                    interactionSource = confirmInteractionSource,
                                    indication = null,
                                    onClick = onConfirm
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = confirmText,
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
