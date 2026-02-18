package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blueedge.chainreaction.data.BotDifficulty
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.GameMode
import com.blueedge.chainreaction.ui.theme.PlayerColorNames
import com.blueedge.chainreaction.ui.theme.PlayerColors
import com.blueedge.chainreaction.utils.Constants
import kotlin.math.roundToInt

@Composable
fun GameSetupScreen(
    gameMode: GameMode,
    onStartGame: () -> Unit,
    onBack: () -> Unit
) {
    var localGridSize by remember { mutableIntStateOf(GameConfig.gridSize) }
    var numPlayers by remember { mutableIntStateOf(2) }  // New: number of players (2-8)
    var botDifficulty by remember { mutableStateOf(GameConfig.botDifficulty) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (gameMode == GameMode.LOCAL_MULTIPLAYER) {
            // Grid Size Section - now a slider
            SectionCard(title = "Grid Size") {
                GridSizeSlider(
                    selectedSize = localGridSize,
                    onSizeSelected = { localGridSize = it }
                )
            }

            // Number of Players Section - slider with color preview
            SectionCard(title = "Number of Players") {
                PlayerCountSlider(
                    numPlayers = numPlayers,
                    onPlayersChanged = { numPlayers = it }
                )
            }
        } else {
            // VS_BOT mode
            // Grid Size Section - separate slider
            SectionCard(title = "Grid Size") {
                GridSizeSlider(
                    selectedSize = localGridSize,
                    onSizeSelected = { localGridSize = it }
                )
            }

            // Difficulty slider
            SectionCard(title = "Difficulty", difficulty = botDifficulty) {
                DifficultySlider(
                    selected = botDifficulty,
                    onSelected = { botDifficulty = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Back + Play buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Raised3DButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f),
                mainColor = Color(0xFFD4956B),
                shadowColor = Color(0xFFB07A52)
            )
            Raised3DButton(
                text = "Play",
                onClick = {
                    GameConfig.apply {
                        this.gameMode = gameMode
                        this.gridSize = localGridSize
                        if (gameMode == GameMode.VS_BOT) {
                            this.player1Name = "Player 1"
                            this.player1ColorIndex = 0  // Blue
                            this.player2Name = "Bot"
                            this.player2ColorIndex = 1  // Red
                            this.botDifficulty = botDifficulty
                        } else {
                            // Fixed player names and colors in order
                            this.player1Name = "Player 1"
                            this.player1ColorIndex = 0
                            this.player2Name = "Player 2"
                            this.player2ColorIndex = 1
                            // Store numPlayers for future multi-player support
                        }
                    }
                    onStartGame()
                },
                modifier = Modifier.weight(2f),
                mainColor = Color(0xFF41AFD4),
                shadowColor = Color(0xFF2E8DAD)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    difficulty: BotDifficulty? = null,
    content: @Composable () -> Unit
) {
    val shadowOffset = 5.dp
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = shadowOffset)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFD0D0D0))
        )
        // Main card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (difficulty != null) {
                    // Animated "Difficulty: Easy/Medium/Hard" title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$title: ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        AnimatedContent(
                            targetState = difficulty,
                            transitionSpec = {
                                ContentTransform(
                                    targetContentEnter = slideInVertically { -it } + fadeIn(),
                                    initialContentExit = slideOutVertically { it } + fadeOut()
                                )
                            },
                            label = "difficulty_title"
                        ) { diff ->
                            val diffColor = when (diff) {
                                BotDifficulty.EASY -> Color(0xFFD4956B)
                                BotDifficulty.MEDIUM -> MaterialTheme.colorScheme.primary
                                BotDifficulty.HARD -> Color(0xFFE05555)
                            }
                            Text(
                                text = diff.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = diffColor
                            )
                        }
                    }
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun GridSizeSlider(
    selectedSize: Int,
    onSizeSelected: (Int) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(selectedSize.toFloat()) }
    
    LaunchedEffect(selectedSize) {
        sliderValue = selectedSize.toFloat()
    }

    Column {
        Text(
            text = "${selectedSize}x${selectedSize}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = {
                val newSize = sliderValue.roundToInt()
                onSizeSelected(newSize)
            },
            valueRange = Constants.GRID_SIZES.first().toFloat()..Constants.GRID_SIZES.last().toFloat(),
            steps = Constants.GRID_SIZES.size - 2,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${Constants.GRID_SIZES.first()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${Constants.GRID_SIZES.last()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerCountSlider(
    numPlayers: Int,
    onPlayersChanged: (Int) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(numPlayers.toFloat()) }
    
    LaunchedEffect(numPlayers) {
        sliderValue = numPlayers.toFloat()
    }

    Column {
        Text(
            text = "$numPlayers Players",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = {
                val newCount = sliderValue.roundToInt()
                onPlayersChanged(newCount)
            },
            valueRange = 2f..8f,
            steps = 5,  // 2, 3, 4, 5, 6, 7, 8
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "2",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "8",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Color preview
        Text(
            text = "Player Colors:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (i in 0 until numPlayers) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PlayerColors[i % PlayerColors.size]),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${i + 1}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GridSizeSelector(
    selectedSize: Int,
    onSizeSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Constants.GRID_SIZES.forEach { size ->
            FilterChip(
                selected = selectedSize == size,
                onClick = { onSizeSelected(size) },
                label = {
                    Text(
                        "${size}x${size}",
                        fontWeight = if (selectedSize == size) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DifficultySlider(
    selected: BotDifficulty,
    onSelected: (BotDifficulty) -> Unit
) {
    val difficulties = BotDifficulty.entries
    var sliderValue by remember { mutableFloatStateOf(selected.ordinal.toFloat()) }

    LaunchedEffect(selected) {
        sliderValue = selected.ordinal.toFloat()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackInactiveColor = MaterialTheme.colorScheme.primaryContainer
    val thumbRadiusDp = 16.dp
    val trackHeightDp = 16.dp
    val density = LocalDensity.current

    Column {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pointerInput(Unit) {
                    val thumbRadiusPx = with(density) { thumbRadiusDp.toPx() }
                    detectTapGestures { offset ->
                        val trackWidth = size.width - 2 * thumbRadiusPx
                        val fraction = ((offset.x - thumbRadiusPx) / trackWidth).coerceIn(0f, 1f)
                        val newValue = fraction * 2f
                        val snapped = newValue.roundToInt().coerceIn(0, 2)
                        sliderValue = snapped.toFloat()
                        onSelected(difficulties[snapped])
                    }
                }
                .pointerInput(Unit) {
                    val thumbRadiusPx = with(density) { thumbRadiusDp.toPx() }
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val snapped = sliderValue.roundToInt().coerceIn(0, 2)
                            sliderValue = snapped.toFloat()
                            onSelected(difficulties[snapped])
                        }
                    ) { _, dragAmount ->
                        val trackWidth = size.width - 2 * thumbRadiusPx
                        val delta = (dragAmount / trackWidth) * 2f
                        sliderValue = (sliderValue + delta).coerceIn(0f, 2f)
                    }
                }
        ) {
            val thumbRadiusPx = thumbRadiusDp.toPx()
            val trackHeightPx = trackHeightDp.toPx()
            val centerY = size.height / 2
            val trackLeft = thumbRadiusPx
            val trackRight = size.width - thumbRadiusPx
            val trackWidth = trackRight - trackLeft
            val fraction = (sliderValue / 2f).coerceIn(0f, 1f)
            val thumbX = trackLeft + fraction * trackWidth
            val trackCornerRadius = CornerRadius(trackHeightPx / 2, trackHeightPx / 2)

            // Inactive track (full)
            drawRoundRect(
                color = trackInactiveColor,
                topLeft = Offset(trackLeft, centerY - trackHeightPx / 2),
                size = Size(trackWidth, trackHeightPx),
                cornerRadius = trackCornerRadius
            )

            // Active track
            if (thumbX > trackLeft) {
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.84f),
                    topLeft = Offset(trackLeft, centerY - trackHeightPx / 2),
                    size = Size(thumbX - trackLeft, trackHeightPx),
                    cornerRadius = trackCornerRadius
                )
            }

            // Thumb circle
            drawCircle(
                color = primaryColor,
                radius = thumbRadiusPx,
                center = Offset(thumbX, centerY)
            )
            // Thumb white border
            drawCircle(
                color = Color.White,
                radius = thumbRadiusPx,
                center = Offset(thumbX, centerY),
                style = Stroke(width = 3.5.dp.toPx())
            )
        }
    }
}

@Composable
private fun Raised3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mainColor: Color = Color(0xFFD4956B),
    shadowColor: Color = Color(0xFFB07A52),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shadowHeight = 6.dp
    val yOffset by animateDpAsState(
        targetValue = if (isPressed) shadowHeight else 0.dp,
        animationSpec = tween(durationMillis = 80),
        label = "buttonPress"
    )

    Box(
        modifier = modifier
            .height(66.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Shadow / bottom layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(18.dp))
                .background(shadowColor)
        )

        // Main button layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .offset(y = yOffset)
                .clip(RoundedCornerShape(18.dp))
                .background(mainColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}