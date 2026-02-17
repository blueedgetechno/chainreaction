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
    var player1Name by remember { mutableStateOf(GameConfig.player1Name) }
    var player1ColorIndex by remember { mutableIntStateOf(GameConfig.player1ColorIndex) }
    var player2Name by remember { mutableStateOf(GameConfig.player2Name) }
    var player2ColorIndex by remember { mutableIntStateOf(GameConfig.player2ColorIndex) }
    var botDifficulty by remember { mutableStateOf(GameConfig.botDifficulty) }

    // Auto-adjust player 2 color if it conflicts with player 1
    LaunchedEffect(player1ColorIndex) {
        if (player2ColorIndex == player1ColorIndex) {
            player2ColorIndex = (player1ColorIndex + 1) % PlayerColors.size
        }
    }

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
            // Grid Size Section
            SectionCard(title = "Grid Size") {
                GridSizeSelector(
                    selectedSize = localGridSize,
                    onSizeSelected = { localGridSize = it }
                )
            }

            // Player 1 Section
            SectionCard(title = "Player 1") {
                OutlinedTextField(
                    value = player1Name,
                    onValueChange = { player1Name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorPicker(
                    selectedIndex = player1ColorIndex,
                    disabledIndex = player2ColorIndex,
                    onColorSelected = { player1ColorIndex = it }
                )
            }

            // Player 2 Section
            SectionCard(title = "Player 2") {
                OutlinedTextField(
                    value = player2Name,
                    onValueChange = { player2Name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorPicker(
                    selectedIndex = player2ColorIndex,
                    disabledIndex = player1ColorIndex,
                    onColorSelected = { player2ColorIndex = it }
                )
            }
        } else {
            // VS_BOT mode - only show difficulty slider
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
                        if (gameMode == GameMode.VS_BOT) {
                            this.gridSize = when (botDifficulty) {
                                BotDifficulty.EASY -> 5
                                BotDifficulty.MEDIUM -> 7
                                BotDifficulty.HARD -> 10
                            }
                            this.player1Name = "Player 1"
                            this.player1ColorIndex = 0  // Blue
                            this.player2Name = "Bot"
                            this.player2ColorIndex = 1  // Red
                            this.botDifficulty = botDifficulty
                        } else {
                            this.gridSize = localGridSize
                            this.player1Name = player1Name.ifBlank { "Player 1" }
                            this.player1ColorIndex = player1ColorIndex
                            this.player2Name = player2Name.ifBlank { "Player 2" }
                            this.player2ColorIndex = player2ColorIndex
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
private fun ColorPicker(
    selectedIndex: Int,
    disabledIndex: Int,
    onColorSelected: (Int) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PlayerColors.forEachIndexed { index, color ->
            val isSelected = index == selectedIndex
            val isDisabled = index == disabledIndex

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDisabled) color.copy(alpha = 0.3f) else color
                    )
                    .then(
                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        else Modifier
                    )
                    .clickable(enabled = !isDisabled) { onColorSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Text(
                        text = "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (selectedIndex in PlayerColorNames.indices) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Selected: ${PlayerColorNames[selectedIndex]}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

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