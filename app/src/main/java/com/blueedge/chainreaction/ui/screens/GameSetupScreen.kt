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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blueedge.chainreaction.data.BotDifficulty
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.GameMode
import com.blueedge.chainreaction.ui.theme.PlayerColorNames
import com.blueedge.chainreaction.ui.theme.PlayerColors
import com.blueedge.chainreaction.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSetupScreen(
    gameMode: GameMode,
    onStartGame: () -> Unit,
    onBack: () -> Unit
) {
    var gridSize by remember { mutableIntStateOf(GameConfig.gridSize) }
    var player1Name by remember { mutableStateOf(GameConfig.player1Name) }
    var player1ColorIndex by remember { mutableIntStateOf(GameConfig.player1ColorIndex) }
    var player2Name by remember {
        mutableStateOf(
            if (gameMode == GameMode.VS_BOT) "Bot" else GameConfig.player2Name
        )
    }
    var player2ColorIndex by remember { mutableIntStateOf(GameConfig.player2ColorIndex) }
    var botDifficulty by remember { mutableStateOf(GameConfig.botDifficulty) }

    // Auto-adjust player 2 color if it conflicts with player 1
    LaunchedEffect(player1ColorIndex) {
        if (player2ColorIndex == player1ColorIndex) {
            player2ColorIndex = (player1ColorIndex + 1) % PlayerColors.size
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (gameMode == GameMode.LOCAL_MULTIPLAYER) "Local Multiplayer" else "Play vs Bot"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Grid Size Section
            SectionCard(title = "Grid Size") {
                GridSizeSelector(
                    selectedSize = gridSize,
                    onSizeSelected = { gridSize = it }
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

            // Player 2 / Bot Section
            if (gameMode == GameMode.LOCAL_MULTIPLAYER) {
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
                SectionCard(title = "Bot Settings") {
                    DifficultySelector(
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
                        this.gridSize = gridSize
                        this.player1Name = player1Name.ifBlank { "Player 1" }
                        this.player1ColorIndex = player1ColorIndex
                        this.player2Name = player2Name.ifBlank {
                            if (gameMode == GameMode.VS_BOT) "Bot" else "Player 2"
                        }
                        this.player2ColorIndex = player2ColorIndex
                        this.botDifficulty = botDifficulty
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
                    "START GAME",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
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
                        text = "✓",
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
private fun DifficultySelector(
    selected: BotDifficulty,
    onSelected: (BotDifficulty) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BotDifficulty.entries.forEach { difficulty ->
            val (label, description) = when (difficulty) {
                BotDifficulty.EASY -> "Easy" to "Random moves — great for learning"
                BotDifficulty.MEDIUM -> "Medium" to "Strategic blocking and expansion"
                BotDifficulty.HARD -> "Hard" to "Minimax AI — a real challenge!"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(difficulty) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected == difficulty)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (selected == difficulty) 4.dp else 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (selected == difficulty) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
