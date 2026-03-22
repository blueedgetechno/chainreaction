package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.RoomStatus
import com.blueedge.chainreaction.data.Strings
import com.blueedge.chainreaction.platform.LocalOnlineGameRepo
import com.blueedge.chainreaction.ui.components.Raised3DButton
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
import com.blueedge.chainreaction.ui.theme.SecondaryActionShadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SmartToy
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnlineLobbyScreen(
    onBack: () -> Unit,
    onPlayVsBot: () -> Unit,
    onMatchFound: (roomCode: String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "search")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "searchPulse"
    )

    // Elapsed timer
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsedSeconds++
        }
    }
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timerText = if (minutes > 0) {
        "${minutes}:${seconds.toString().padStart(2, '0')}"
    } else {
        "0:${seconds.toString().padStart(2, '0')}"
    }

    // Matchmaking state
    var roomCode by remember { mutableStateOf("") }
    var matchmakingStarted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var listenerJob by remember { mutableStateOf<Job?>(null) }

    val onlineGameRepo = LocalOnlineGameRepo.current

    // Start matchmaking on first composition
    LaunchedEffect(Unit) {
        if (!matchmakingStarted) {
            matchmakingStarted = true
            val code = onlineGameRepo.findRandomMatch(
                gridSize = GameConfig.gridSize,
                gameVariant = GameConfig.gameVariant,
                playerName = Strings.colorName(GameConfig.player1ColorIndex),
                playerColorIndex = GameConfig.player1ColorIndex
            )
            roomCode = code

            // Check if we already joined as guest (status = IN_PROGRESS)
            // If so, navigate immediately. Otherwise, listen for opponent.
            listenerJob = scope.launch {
                onlineGameRepo.listenToRoom(code).collect { room ->
                    if (room.status == RoomStatus.IN_PROGRESS.name) {
                        onMatchFound(code)
                    }
                }
            }
        }
    }

    // Clean up room if user leaves before match
    DisposableEffect(Unit) {
        onDispose {
            listenerJob?.cancel()
            if (roomCode.isNotEmpty()) {
                scope.launch {
                    try { onlineGameRepo.leaveRoom(roomCode) } catch (_: Exception) {}
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        val isLandscape = maxWidth > maxHeight
        val portraitScrollState = rememberScrollState()
        val landscapeScrollState = rememberScrollState()

        val outerColumnModifier = if (isLandscape) {
            Modifier.fillMaxSize().verticalScroll(landscapeScrollState)
        } else {
            Modifier.fillMaxSize()
        }
        
        val innerColumnModifier = if (isLandscape) {
            Modifier.widthIn(max = 480.dp).fillMaxWidth()
        } else {
            Modifier.fillMaxSize().verticalScroll(portraitScrollState)
        }

        Column(
            modifier = outerColumnModifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Column(
            modifier = innerColumnModifier
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = Strings.playWStranger,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (!isLandscape) {
                Spacer(modifier = Modifier.size(16.dp))
            }

            // Timer
            Text(
                text = timerText,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Searching indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().alpha(pulseAlpha)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.searchingForMatch,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Raised3DButton(
                    text = Strings.botInstead,
                    topText = Strings.playWith,
                    onClick = onPlayVsBot,
                    modifier = Modifier.fillMaxWidth(),
                    mainColor = MaterialTheme.colorScheme.tertiary,
                    shadowColor = Color(0xFFA8524E),
                    icon = Icons.Default.SmartToy
                )
                Raised3DButton(
                    text = Strings.back,
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    mainColor = SecondaryActionColor,
                    shadowColor = SecondaryActionShadow
                )
            }
        }
        }
    }
}
