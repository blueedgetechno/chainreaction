package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blueedge.chainreaction.data.BotDifficulty
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.GameMode
import com.blueedge.chainreaction.data.GameVariant
import com.blueedge.chainreaction.ui.components.CustomSlider
import com.blueedge.chainreaction.ui.components.Raised3DButton
import com.blueedge.chainreaction.ui.theme.PlayerColors
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
import com.blueedge.chainreaction.ui.theme.SecondaryActionShadow
import com.blueedge.chainreaction.utils.Constants

@Composable
fun GameSetupScreen(
    gameMode: GameMode,
    onStartGame: () -> Unit,
    onBack: () -> Unit,
    onHowToPlay: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var localGridSize by remember { mutableIntStateOf(GameConfig.gridSize) }
    var numPlayers by remember { mutableIntStateOf(GameConfig.numPlayers.coerceIn(2, 6)) }
    var botDifficulty by remember { mutableStateOf(GameConfig.botDifficulty) }
    var gameVariant by remember { mutableStateOf(GameConfig.gameVariant) }

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
        // Mode header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (gameMode == GameMode.LOCAL_MULTIPLAYER) Icons.Filled.People else Icons.Filled.SmartToy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = if (gameMode == GameMode.LOCAL_MULTIPLAYER) "Play w/ Friends" else "Play Solo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.size(6.dp))

        // Game Variant Tab Switch (Simple / Classic)
        SectionCard(
            title = "Mode:",
            animatedValue = gameVariant.name.lowercase().replaceFirstChar { it.uppercase() },
            valueColor = if (gameVariant == GameVariant.SIMPLE) MaterialTheme.colorScheme.primary else Color(0xFFE09B40),
            trailingAction = {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onHowToPlay
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "How to play",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            val simpleColor = MaterialTheme.colorScheme.primary
            val classicColor = Color(0xFFE09B40)
            val isSimple = gameVariant == GameVariant.SIMPLE

            // Animate pill position (0f = left/Simple, 1f = right/Classic)
            val pillPosition by animateFloatAsState(
                targetValue = if (isSimple) 0f else 1f,
                animationSpec = tween(durationMillis = 300),
                label = "pill_slide"
            )
            // Animate pill color
            val pillColor by animateColorAsState(
                targetValue = if (isSimple) simpleColor else classicColor,
                animationSpec = tween(durationMillis = 300),
                label = "pill_color"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                // Sliding pill indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            translationX = pillPosition * size.width
                        }
                        .clip(RoundedCornerShape(10.dp))
                        .background(pillColor)
                )
                // Text labels on top
                Row(modifier = Modifier.fillMaxSize()) {
                    GameVariant.entries.forEach { variant ->
                        val textColor by animateColorAsState(
                            targetValue = if (gameVariant == variant) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(durationMillis = 300),
                            label = "text_color_${variant.name}"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { gameVariant = variant },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = variant.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        // Grid Size Section (shared by both modes)
        SectionCard(
            title = "Grid Size:",
            animatedValue = "${localGridSize}x${localGridSize}",
            valueColor = MaterialTheme.colorScheme.primary
        ) {
            CustomSlider(
                value = Constants.GRID_SIZES.indexOf(localGridSize).coerceAtLeast(0),
                onValueChange = { index -> localGridSize = Constants.GRID_SIZES[index] },
                valueCount = Constants.GRID_SIZES.size,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (gameMode == GameMode.LOCAL_MULTIPLAYER) {
            // Number of Players Section - slider with color preview
            SectionCard(
                title = "Players:",
                animatedValue = "$numPlayers",
                valueColor = MaterialTheme.colorScheme.primary
            ) {
                PlayerCountContent(numPlayers) { numPlayers = it }
            }
        } else {
            // VS_BOT mode - Difficulty slider
            SectionCard(
                title = "Difficulty:",
                animatedValue = botDifficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                valueColor = when (botDifficulty) {
                    BotDifficulty.EASY -> SecondaryActionColor
                    BotDifficulty.MEDIUM -> MaterialTheme.colorScheme.primary
                    BotDifficulty.HARD -> Color(0xFFE05555)
                }
            ) {
                val difficulties = BotDifficulty.entries
                CustomSlider(
                    value = botDifficulty.ordinal,
                    onValueChange = { index -> botDifficulty = difficulties[index] },
                    valueCount = difficulties.size,
                    modifier = Modifier.fillMaxWidth()
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
                mainColor = SecondaryActionColor,
                shadowColor = SecondaryActionShadow
            )
            Raised3DButton(
                text = "Play",
                onClick = {
                    GameConfig.apply {
                        this.gameMode = gameMode
                        this.gameVariant = gameVariant
                        this.gridSize = localGridSize
                        if (gameMode == GameMode.VS_BOT) {
                            this.numPlayers = 2
                            this.player1Name = "Player 1"
                            this.player1ColorIndex = 0  // Blue
                            this.player2Name = "Bot"
                            this.player2ColorIndex = 1  // Red
                            this.botDifficulty = botDifficulty
                        } else {
                            this.numPlayers = numPlayers
                            this.player1Name = "Player 1"
                            this.player1ColorIndex = 0
                            this.player2Name = "Player 2"
                            this.player2ColorIndex = 1
                        }
                    }
                    GameConfig.save(context)
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
    animatedValue: String,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    trailingAction: @Composable (() -> Unit)? = null,
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
                // Animated "Title: Value" header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    AnimatedContent(
                        targetState = animatedValue,
                        transitionSpec = {
                            ContentTransform(
                                targetContentEnter = slideInVertically { -it } + fadeIn(),
                                initialContentExit = slideOutVertically { it } + fadeOut()
                            )
                        },
                        label = "section_value"
                    ) { value ->
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = valueColor
                        )
                    }
                    if (trailingAction != null) {
                        Spacer(modifier = Modifier.weight(1f))
                        trailingAction()
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerCountContent(
    numPlayers: Int,
    onPlayersChanged: (Int) -> Unit
) {
    Column {
        CustomSlider(
            value = numPlayers - 2,
            onValueChange = { index -> onPlayersChanged(index + 2) },
            valueCount = 5,  // 2, 3, 4, 5, 6
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (i in 0 until 6) {
                val isVisible = i < numPlayers
                AnimatedVisibility(
                    visible = isVisible,
                    enter = scaleIn(initialScale = 0f) + fadeIn(),
                    exit = scaleOut(targetScale = 0f) + fadeOut()
                ) {
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
}



