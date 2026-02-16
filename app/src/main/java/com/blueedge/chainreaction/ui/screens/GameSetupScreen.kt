package com.blueedge.chainreaction.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
            SectionCard(title = "Difficulty") {
                DifficultySlider(
                    selected = botDifficulty,
                    onSelected = { botDifficulty = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Start Game Button
        Button(
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
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Text(
                "Play",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
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

    // Sync slider when external selection changes
    LaunchedEffect(selected) {
        sliderValue = selected.ordinal.toFloat()
    }

    Column {
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = {
                val snapped = sliderValue.roundToInt().coerceIn(0, 2)
                sliderValue = snapped.toFloat()
                onSelected(difficulties[snapped])
            },
            valueRange = 0f..2f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Labels row
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Easy",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected == BotDifficulty.EASY) FontWeight.Bold else FontWeight.Normal,
                color = if (selected == BotDifficulty.EASY) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Text(
                "Medium",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected == BotDifficulty.MEDIUM) FontWeight.Bold else FontWeight.Normal,
                color = if (selected == BotDifficulty.MEDIUM) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                "Hard",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected == BotDifficulty.HARD) FontWeight.Bold else FontWeight.Normal,
                color = if (selected == BotDifficulty.HARD) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Description with grid size info
        // val description = when (selected) {
        //     BotDifficulty.EASY -> "Random moves � great for learning (5�5 grid)"
        //     BotDifficulty.MEDIUM -> "Strategic blocking and expansion (7�7 grid)"
        //     BotDifficulty.HARD -> "Minimax AI � a real challenge! (10�10 grid)"
        // }
        // Text(
        //     text = description,
        //     style = MaterialTheme.typography.bodySmall,
        //     color = MaterialTheme.colorScheme.onSurfaceVariant,
        //     textAlign = TextAlign.Center,
        //     modifier = Modifier.fillMaxWidth()
        // )
    }
}