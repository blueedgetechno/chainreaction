package com.blueedge.chainreaction.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blueedge.chainreaction.ui.components.Raised3DButton
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
import com.blueedge.chainreaction.ui.theme.SecondaryActionShadow

@Composable
fun HowToPlayScreen(
    onBack: () -> Unit
) {
    Box(
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
          ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "How to Play",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Instruction card
            val cardShadowOffset = 5.dp
            Box(modifier = Modifier.fillMaxWidth()) {
                // Shadow layer
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(y = cardShadowOffset)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFD0D0D0))
                )
                // Main card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InstructionItem(
                            emoji = "🎯",
                            title = "Objective",
                            description = "Capture all opponents cells on the grid to win the game!"
                        )
                        InstructionItem(
                            emoji = "👆",
                            title = "Place Dots",
                            description = "Tap on empty cell (on first move) or your own cells to add dots."
                        )
                        InstructionItem(
                            emoji = "💥",
                            title = "Explosion",
                            description = "When a cell reaches its critical mass (4 dots), it explodes! The dots spread to adjacent cells."
                        )
                        InstructionItem(
                            emoji = "⚡",
                            title = "Chain Reactions",
                            description = "Explosions can trigger more explosions, creating amazing chain reactions!"
                        )
                        InstructionItem(
                            emoji = "🎨",
                            title = "Capture",
                            description = "When your dots land on opponent cells, you capture them and change their color to yours."
                        )
                        InstructionItem(
                            emoji = "🏆",
                            title = "Victory",
                            description = "Eliminate all opponent cells from the board to claim victory!"
                        )
                    }
                }
            }

            // Simple vs Classic mode differences
            val cardShadowOffset2 = 5.dp
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(y = cardShadowOffset2)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFD0D0D0))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val simpleColor = Color(0xFF41AFD4)
                        val classicColor = Color(0xFFE09B40)
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = simpleColor)) {
                                    append("Simple")
                                }
                                withStyle(SpanStyle(color = Color.Gray)) {
                                    append(" vs ")
                                }
                                withStyle(SpanStyle(color = classicColor)) {
                                    append("Classic")
                                }
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )

                        ModeDiffItem(
                            topic = "First Move",
                            simpleText = "Place 3 dots on any empty cell.",
                            classicText = "Place 1 dot on any empty cell."
                        )

                        ModeDiffItem(
                            topic = "Placement",
                            simpleText = "You can place a dot on your own cells only (after first move).",
                            classicText = "You can place a dot anywhere — on empty cells or your own cells at anytime."
                        )

                        ModeDiffItem(
                            topic = "Critical Mass",
                            simpleText = "Every cell explodes at 4 dots, regardless of position.",
                            classicText = "Corners explode at 2, edges at 3, and interior cells at 4 dots."
                        )

                        ModeDiffItem(
                            topic = "Explosions",
                            simpleText = "Exploding cell is emptied completely.",
                            classicText = "Excess dots are preserved — only the critical mass is subtracted."
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
          }

            Spacer(modifier = Modifier.height(16.dp))

            // Back button
            Raised3DButton(
                text = "Back",
                onClick = onBack,
                mainColor = SecondaryActionColor,
                shadowColor = SecondaryActionShadow,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InstructionItem(
    emoji: String,
    title: String,
    description: String
) {
    Column {
        Text(
            text = "$title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun ModeDiffItem(
    topic: String,
    simpleText: String,
    classicText: String
) {
    val simpleColor = Color(0xFF41AFD4)
    val classicColor = SecondaryActionShadow

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = topic,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        // Simple line
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .width(8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(simpleColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = simpleText,
                style = MaterialTheme.typography.bodyMedium,
                color = simpleColor,
                fontWeight = FontWeight.Medium
            )
        }
        // Classic line
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .width(8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(classicColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = classicText,
                style = MaterialTheme.typography.bodyMedium,
                color = classicColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


