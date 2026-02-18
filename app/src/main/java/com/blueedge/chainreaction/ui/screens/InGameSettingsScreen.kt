package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InGameSettingsScreen(
    onHowToPlay: () -> Unit,
    onRestart: () -> Unit,
    onExitToMenu: () -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Game Settings",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(40.dp))

            // How to Play button
            SettingsButton(
                text = "How to Play",
                onClick = onHowToPlay,
                mainColor = Color(0xFFD4956B),
                shadowColor = Color(0xFFB07A52),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Restart button
            SettingsButton(
                text = "Restart Game",
                onClick = onRestart,
                mainColor = Color(0xFF41AFD4),
                shadowColor = Color(0xFF2E8DAD),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Exit to Menu button
            SettingsButton(
                text = "Exit to Menu",
                onClick = onExitToMenu,
                mainColor = Color(0xFFEA695E),
                shadowColor = Color(0xFFC55550),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Back button
            SettingsButton(
                text = "Resume Game",
                onClick = onBack,
                mainColor = Color(0xFF4CAF50),
                shadowColor = Color(0xFF3D8B40),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mainColor: Color = Color(0xFFD4956B),
    shadowColor: Color = Color(0xFFB07A52)
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
