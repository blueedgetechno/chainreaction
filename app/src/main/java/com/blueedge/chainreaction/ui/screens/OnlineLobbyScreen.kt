package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blueedge.chainreaction.ui.components.Raised3DButton
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
import com.blueedge.chainreaction.ui.theme.SecondaryActionShadow

enum class LobbyMode {
    MENU, CREATE, JOIN, RANDOM
}

@Composable
fun OnlineLobbyScreen(
    onBack: () -> Unit,
    onRoomCreated: (roomCode: String) -> Unit,
    onRoomJoined: (roomCode: String) -> Unit,
    onRandomMatch: () -> Unit
) {
    var lobbyMode by remember { mutableStateOf(LobbyMode.MENU) }
    var roomCodeInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var createdRoomCode by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Online",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Play with anyone, anywhere",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF888888)
            )

            Spacer(modifier = Modifier.height(40.dp))

            when (lobbyMode) {
                LobbyMode.MENU -> {
                    // Create Room button
                    Raised3DButton(
                        text = "CREATE ROOM",
                        topText = "HOST A",
                        onClick = {
                            lobbyMode = LobbyMode.CREATE
                            isLoading = true
                            onRoomCreated("") // Signal to ViewModel to create room
                        },
                        mainColor = MaterialTheme.colorScheme.primary,
                        shadowColor = Color(0xFF2E8DAD),
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Group
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Join Room button
                    Raised3DButton(
                        text = "ROOM",
                        topText = "JOIN A",
                        onClick = { lobbyMode = LobbyMode.JOIN },
                        mainColor = Color(0xFF5CB85C),
                        shadowColor = Color(0xFF449D44),
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Login
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Random Match button
                    Raised3DButton(
                        text = "STRANGER",
                        topText = "PLAY A",
                        onClick = {
                            lobbyMode = LobbyMode.RANDOM
                            isLoading = true
                            onRandomMatch()
                        },
                        mainColor = Color(0xFFF0AD4E),
                        shadowColor = Color(0xFFD49A3E),
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Casino
                    )
                }

                LobbyMode.CREATE -> {
                    CreateRoomView(
                        roomCode = createdRoomCode,
                        isLoading = isLoading,
                        onBack = {
                            lobbyMode = LobbyMode.MENU
                            isLoading = false
                        },
                        onCopyCode = {
                            if (createdRoomCode.isNotEmpty()) {
                                clipboardManager.setText(AnnotatedString(createdRoomCode))
                            }
                        }
                    )
                }

                LobbyMode.JOIN -> {
                    JoinRoomView(
                        roomCode = roomCodeInput,
                        onRoomCodeChange = { input ->
                            roomCodeInput = input.uppercase().take(6)
                            errorMessage = ""
                        },
                        errorMessage = errorMessage,
                        isLoading = isLoading,
                        onJoin = {
                            if (roomCodeInput.length == 6) {
                                focusManager.clearFocus()
                                isLoading = true
                                errorMessage = ""
                                onRoomJoined(roomCodeInput)
                            } else {
                                errorMessage = "Enter a 6-character room code"
                            }
                        },
                        onBack = {
                            lobbyMode = LobbyMode.MENU
                            isLoading = false
                            errorMessage = ""
                        }
                    )
                }

                LobbyMode.RANDOM -> {
                    RandomMatchView(
                        onBack = {
                            lobbyMode = LobbyMode.MENU
                            isLoading = false
                        }
                    )
                }
            }
        }

        // Back button
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 12.dp)
                .size(44.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable {
                    if (lobbyMode == LobbyMode.MENU) {
                        onBack()
                    } else {
                        lobbyMode = LobbyMode.MENU
                        isLoading = false
                        errorMessage = ""
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CreateRoomView(
    roomCode: String,
    isLoading: Boolean,
    onBack: () -> Unit,
    onCopyCode: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Your Room Code",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (roomCode.isNotEmpty()) {
            // Room code display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F5F5))
                    .clickable { onCopyCode() }
                    .padding(horizontal = 32.dp, vertical = 20.dp)
            ) {
                Text(
                    text = roomCode,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 8.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Share this code with your friend",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Waiting indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.alpha(pulseAlpha)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Waiting for opponent...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF666666)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Raised3DButton(
            text = "Cancel",
            onClick = onBack,
            mainColor = SecondaryActionColor,
            shadowColor = SecondaryActionShadow,
            textColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun JoinRoomView(
    roomCode: String,
    onRoomCodeChange: (String) -> Unit,
    errorMessage: String,
    isLoading: Boolean,
    onJoin: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Enter Room Code",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = roomCode,
            onValueChange = onRoomCodeChange,
            placeholder = {
                Text(
                    "ABC123",
                    color = Color(0xFFCCCCCC),
                    letterSpacing = 4.sp
                )
            },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(onGo = { onJoin() }),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFDDDDDD),
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        AnimatedVisibility(
            visible = errorMessage.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = errorMessage,
                color = Color(0xFFEA695E),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                color = Color(0xFF5CB85C)
            )
        } else {
            Raised3DButton(
                text = "Join",
                onClick = onJoin,
                mainColor = Color(0xFF5CB85C),
                shadowColor = Color(0xFF449D44),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Raised3DButton(
            text = "Back",
            onClick = onBack,
            mainColor = SecondaryActionColor,
            shadowColor = SecondaryActionShadow,
            textColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RandomMatchView(
    onBack: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "search")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "searchPulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "🔍",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Finding Opponent",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.alpha(pulseAlpha)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color(0xFFF0AD4E)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Searching for a match...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF666666)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Raised3DButton(
            text = "Cancel",
            onClick = onBack,
            mainColor = SecondaryActionColor,
            shadowColor = SecondaryActionShadow,
            textColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
