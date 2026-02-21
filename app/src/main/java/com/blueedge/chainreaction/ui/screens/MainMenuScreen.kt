package com.blueedge.chainreaction.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blueedge.chainreaction.ui.components.Raised3DButton
@Composable
fun MainMenuScreen(
    onLocalMultiplayer: () -> Unit,
    onPlayVsBot: () -> Unit,
    onSettings: () -> Unit,
    onHowToPlay: () -> Unit
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
        // Settings icon top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .clickable { onSettings() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "CHAIN",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 4.sp
            )
            Text(
                text = "REACTION",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // How to Play button
            Raised3DButton(
                text = "HOW TO PLAY",
                onClick = onHowToPlay,
                mainColor = Color(0xFFD4956B),
                shadowColor = Color(0xFFB07A52),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Description removed (moved to How to Play page)

            Spacer(modifier = Modifier.height(48.dp))

            // Local Multiplayer button
            Raised3DButton(
                text = "FRIEND",
                topText = "PLAY VS.",
                onClick = onLocalMultiplayer,
                mainColor = MaterialTheme.colorScheme.primary,
                shadowColor = Color(0xFF2E8DAD),
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Play vs Bot button
            Raised3DButton(
                text = "BOT",
                topText = "PLAY VS.",
                onClick = onPlayVsBot,
                mainColor = MaterialTheme.colorScheme.tertiary,
                shadowColor = Color(0xFFA8524E),
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.SmartToy
            )
        }
    }
}


