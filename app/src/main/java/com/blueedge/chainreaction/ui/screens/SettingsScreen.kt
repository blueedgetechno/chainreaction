package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blueedge.chainreaction.R
import com.blueedge.chainreaction.audio.SoundManager
import com.blueedge.chainreaction.data.AppFont
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
import com.blueedge.chainreaction.ui.theme.SecondaryActionShadow
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.ui.components.Raised3DButton
import com.blueedge.chainreaction.ui.components.SmallRaised3DButton

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    isInGame: Boolean = false,
    onHowToPlay: (() -> Unit)? = null,
    onRestart: (() -> Unit)? = null,
    onExitToMenu: (() -> Unit)? = null
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
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = if (isInGame) "Game Settings" else "Settings",
                style = if (isInGame) MaterialTheme.typography.displaySmall
                else MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Music, Sound & Vibration icon toggle buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SoundToggleButton(
                    iconEnabled = Icons.Rounded.MusicNote,
                    iconDisabled = Icons.Rounded.MusicOff,
                    label = "Music",
                    enabled = GameConfig.musicEnabled,
                    onToggle = {
                        GameConfig.musicEnabled = !GameConfig.musicEnabled
                        SoundManager.onMusicToggled()
                    }
                )
                SoundToggleButton(
                    iconEnabled = Icons.Rounded.VolumeUp,
                    iconDisabled = Icons.Rounded.VolumeOff,
                    label = "Sound",
                    enabled = GameConfig.soundEnabled,
                    onToggle = { GameConfig.soundEnabled = !GameConfig.soundEnabled }
                )
                SoundToggleButton(
                    iconEnabled = ImageVector.vectorResource(R.drawable.ic_vibration),
                    iconDisabled = ImageVector.vectorResource(R.drawable.ic_vibration),
                    label = "Vibration",
                    enabled = GameConfig.vibrationEnabled,
                    onToggle = { GameConfig.vibrationEnabled = !GameConfig.vibrationEnabled }
                )
            }

            if (!isInGame) {
                Spacer(modifier = Modifier.height(32.dp))

                // Settings card (only for normal settings)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        // Language row
                        SettingsDropdownRow(
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Translate,
                                        contentDescription = "Language",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            selectedValue = GameConfig.language,
                            options = listOf("English", "Hindi", "Spanish", "French", "German", "Japanese"),
                            onSelected = { GameConfig.language = it }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Font row
                        SettingsDropdownRow(
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Aa",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            selectedValue = GameConfig.appFont.displayName,
                            options = AppFont.entries.map { it.displayName },
                            onSelected = { selected ->
                                GameConfig.appFont = AppFont.entries.first { it.displayName == selected }
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // In-game buttons
            if (isInGame) {
                // Resume Game button (proxy for back)
                onBack?.let {
                    Raised3DButton(
                        text = "Resume Game",
                        onClick = it,
                        mainColor = SecondaryActionColor,
                        shadowColor = SecondaryActionShadow,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                onRestart?.let {
                    Raised3DButton(
                        text = "Restart Game",
                        onClick = it,
                        mainColor = Color(0xFF41AFD4),
                        shadowColor = Color(0xFF2E8DAD),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                onExitToMenu?.let {
                    Raised3DButton(
                        text = "Exit to Menu",
                        onClick = it,
                        mainColor = Color(0xFFEA695E),
                        shadowColor = Color(0xFFC55550),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                onHowToPlay?.let {
                    Raised3DButton(
                        text = "How to Play",
                        onClick = it,
                        mainColor = Color(0xFF4CAF50),
                        shadowColor = Color(0xFF3D8B40),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // Back button
                onBack?.let {
                    Raised3DButton(
                        text = "Back",
                        onClick = it,
                        modifier = Modifier.fillMaxWidth(),
                        mainColor = SecondaryActionColor,
                        shadowColor = SecondaryActionShadow
                    )
                }
            }

            if (!isInGame) {
                Spacer(modifier = Modifier.height(36.dp))

                // Terms & Privacy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SmallRaised3DButton(
                        text = "Terms of Service",
                        onClick = { }
                    )
                    SmallRaised3DButton(
                        text = "Privacy Policy",
                        onClick = { }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Version 1.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SoundToggleButton(
    iconEnabled: ImageVector,
    iconDisabled: ImageVector,
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
        label = "toggleBg"
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
                .size(72.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (enabled) iconEnabled else iconDisabled,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(36.dp)
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
private fun SettingsDropdownRow(
    leadingContent: @Composable () -> Unit,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var parentWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { parentWidthPx = it.size.width }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = { expanded = true },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingContent()
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = selectedValue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(density) { parentWidthPx.toDp() }),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 2.dp
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontWeight = if (option == selectedValue) FontWeight.Bold else FontWeight.Normal,
                            color = if (option == selectedValue) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

