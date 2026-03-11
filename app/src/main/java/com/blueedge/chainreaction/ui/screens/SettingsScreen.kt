package com.blueedge.chainreaction.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.blueedge.chainreaction.BuildConfig
// import com.blueedge.chainreaction.R
import com.blueedge.chainreaction.audio.SoundManager
import com.blueedge.chainreaction.data.AppFont
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
import com.blueedge.chainreaction.ui.theme.SecondaryActionShadow
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.Strings
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
    val context = LocalContext.current
    
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
            )
    ) {
        val isLandscape = maxWidth > maxHeight
        var showRestartConfirmation by remember { mutableStateOf(false) }
        var showExitConfirmation by remember { mutableStateOf(false) }
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
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = if (isInGame) Strings.gameSettings else Strings.settings,
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
                    label = Strings.music,
                    enabled = GameConfig.musicEnabled,
                    onToggle = {
                        GameConfig.musicEnabled = !GameConfig.musicEnabled
                        SoundManager.onMusicToggled()
                    }
                )
                SoundToggleButton(
                    iconEnabled = Icons.Rounded.VolumeUp,
                    iconDisabled = Icons.Rounded.VolumeOff,
                    label = Strings.sound,
                    enabled = GameConfig.soundEnabled,
                    onToggle = { GameConfig.soundEnabled = !GameConfig.soundEnabled }
                )
                // SoundToggleButton(
                //     iconEnabled = ImageVector.vectorResource(R.drawable.ic_vibration),
                //     iconDisabled = ImageVector.vectorResource(R.drawable.ic_vibration),
                //     label = "Vibration",
                //     enabled = GameConfig.vibrationEnabled,
                //     onToggle = { GameConfig.vibrationEnabled = !GameConfig.vibrationEnabled }
                // )
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
                            selectedValue = Strings.languageDisplayName(GameConfig.language),
                            options = Strings.supportedLanguageDisplay,
                            onSelected = {
                                GameConfig.language = Strings.languageKeyFromDisplay(it)
                                GameConfig.save(context)
                            }
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
                                GameConfig.appFont = AppFont.entries.firstOrNull { it.displayName == selected } ?: AppFont.DEFAULT
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
                onRestart?.let {
                    Raised3DButton(
                        text = Strings.restartGame,
                        onClick = { showRestartConfirmation = true },
                        mainColor = Color(0xFF41AFD4),
                        shadowColor = Color(0xFF2E8DAD),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                onHowToPlay?.let {
                    Raised3DButton(
                        text = Strings.howToPlay,
                        onClick = it,
                        mainColor = Color(0xFF4CAF50),
                        shadowColor = Color(0xFF3D8B40),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                onExitToMenu?.let {
                    Raised3DButton(
                        text = Strings.exitToMenu,
                        onClick = { showExitConfirmation = true },
                        mainColor = Color(0xFFEA695E),
                        shadowColor = Color(0xFFC55550),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (!isInGame) {
                // Terms & Privacy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    SmallRaised3DButton(
                        text = Strings.termsOfService,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chainreaction.blueedge.me/terms.html"))
                            context.startActivity(intent)
                        }
                    )
                    SmallRaised3DButton(
                        text = Strings.privacyPolicy,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chainreaction.blueedge.me/privacy.html"))
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${Strings.version} ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = Strings.madeWithLove,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Back button (always at the bottom)
            Raised3DButton(
                text = Strings.back,
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                mainColor = SecondaryActionColor,
                shadowColor = SecondaryActionShadow
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
        } // end outer scroll column

        // Restart confirmation dialog
        if (showRestartConfirmation) {
            SettingsConfirmationDialog(
                title = Strings.restartGameQ,
                message = Strings.restartMessage,
                confirmText = Strings.restart,
                confirmColor = Color(0xFF41AFD4),
                confirmShadowColor = Color(0xFF2E8DAD),
                onConfirm = {
                    showRestartConfirmation = false
                    onRestart?.invoke()
                },
                onDismiss = { showRestartConfirmation = false }
            )
        }

        // Exit to menu confirmation dialog
        if (showExitConfirmation) {
            SettingsConfirmationDialog(
                title = Strings.exitToMenuQ,
                message = Strings.exitMessage,
                confirmText = Strings.exit,
                confirmColor = Color(0xFFEA695E),
                confirmShadowColor = Color(0xFFC55550),
                onConfirm = {
                    showExitConfirmation = false
                    onExitToMenu?.invoke()
                },
                onDismiss = { showExitConfirmation = false }
            )
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

@Composable
private fun SettingsConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    confirmShadowColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button with shadow
                    val cancelInteractionSource = remember { MutableInteractionSource() }
                    val isCancelPressed by cancelInteractionSource.collectIsPressedAsState()
                    val cancelShadowHeight = 4.dp
                    val cancelYOffset by animateDpAsState(
                        targetValue = if (isCancelPressed) cancelShadowHeight else 0.dp,
                        animationSpec = tween(durationMillis = 80),
                        label = "cancelPress"
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .offset(y = cancelShadowHeight)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFB0B0B0))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .offset(y = cancelYOffset)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE0E0E0))
                                .clickable(
                                    interactionSource = cancelInteractionSource,
                                    indication = null,
                                    onClick = onDismiss
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = Strings.cancel,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }
                    }

                    // Confirm button with shadow
                    val confirmInteractionSource = remember { MutableInteractionSource() }
                    val isConfirmPressed by confirmInteractionSource.collectIsPressedAsState()
                    val confirmShadowHeight = 4.dp
                    val confirmYOffset by animateDpAsState(
                        targetValue = if (isConfirmPressed) confirmShadowHeight else 0.dp,
                        animationSpec = tween(durationMillis = 80),
                        label = "confirmPress"
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .offset(y = confirmShadowHeight)
                                .clip(RoundedCornerShape(16.dp))
                                .background(confirmShadowColor)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .offset(y = confirmYOffset)
                                .clip(RoundedCornerShape(16.dp))
                                .background(confirmColor)
                                .clickable(
                                    interactionSource = confirmInteractionSource,
                                    indication = null,
                                    onClick = onConfirm
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = confirmText,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}