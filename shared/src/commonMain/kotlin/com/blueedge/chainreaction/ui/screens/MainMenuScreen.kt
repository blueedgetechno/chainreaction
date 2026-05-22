package com.blueedge.chainreaction.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
import org.jetbrains.compose.resources.painterResource
import chainreaction.shared.generated.resources.Res
import chainreaction.shared.generated.resources.banner
import chainreaction.shared.generated.resources.namelogoflat
import chainreaction.shared.generated.resources.ic_people
import chainreaction.shared.generated.resources.ic_smart_toy
import chainreaction.shared.generated.resources.ic_sports_esports
import chainreaction.shared.generated.resources.ic_settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.blueedge.chainreaction.data.Strings
import com.blueedge.chainreaction.ui.components.Raised3DButton
import com.blueedge.chainreaction.ui.components.SmallRaised3DButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainMenuScreen(
    onLocalMultiplayer: () -> Unit,
    onPlayVsBot: () -> Unit,
    onSettings: () -> Unit,
    onHowToPlay: () -> Unit
) {
    // Animation state: 0f = off-screen (below), 1f = final position
    val cardOffset = remember { Animatable(1f) }
    val buttonOffsets = remember { List(3) { Animatable(1f) } }

    LaunchedEffect(Unit) {
        // Card slides up with subtle bounce
        launch {
            cardOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        // Buttons slide up with staggered delay
        buttonOffsets.forEachIndexed { index, anim ->
            launch {
                delay(150L + index * 80L)
                anim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SecondaryActionColor)
    ) {
        val isLandscape = maxWidth > maxHeight
        val screenHeight = maxHeight

        if (isLandscape) {
            // --- Landscape layout: banner left, card right ---
            Row(modifier = Modifier.fillMaxSize()) {
                // Left half — banner image fills height
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.banner),
                        contentDescription = "Chain Reaction Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Right half — white card with buttons, scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            translationY = cardOffset.value * 150f
                        }
                        .clip(RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp))
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Show name logo on taller landscape screens (e.g. iPad)
                    if (screenHeight >= 600.dp) {
                        Image(
                            painter = painterResource(Res.drawable.namelogoflat),
                            contentDescription = "Chain Reaction",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    SmallRaised3DButton(
                        text = Strings.howToPlay,
                        onClick = onHowToPlay,
                        icon = painterResource(Res.drawable.ic_sports_esports),
                        modifier = Modifier.graphicsLayer {
                        translationY = buttonOffsets[0].value * 80f
                        alpha = 1f - buttonOffsets[0].value
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                    Raised3DButton(
                        text = Strings.friends,
                        topText = Strings.playWith,
                        onClick = onLocalMultiplayer,
                        mainColor = MaterialTheme.colorScheme.primary,
                        shadowColor = Color(0xFF2E8DAD),
                        modifier = Modifier.fillMaxWidth().graphicsLayer {
                            translationY = buttonOffsets[1].value * 80f
                            alpha = 1f - buttonOffsets[1].value
                        },
                        icon = painterResource(Res.drawable.ic_people)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Raised3DButton(
                        text = Strings.bot,
                        topText = Strings.playWith,
                        onClick = onPlayVsBot,
                        mainColor = MaterialTheme.colorScheme.tertiary,
                        shadowColor = Color(0xFFA8524E),
                        modifier = Modifier.fillMaxWidth().graphicsLayer {
                            translationY = buttonOffsets[2].value * 80f
                        },
                        icon = painterResource(Res.drawable.ic_smart_toy)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Settings icon top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 20.dp)
                    .size(44.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onSettings() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_settings),
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // --- Portrait layout (original) ---
            val painter = painterResource(Res.drawable.banner)
            val imageAspectRatio = painter.intrinsicSize.width / painter.intrinsicSize.height
            val imageDisplayHeight = with(LocalDensity.current) {
                (maxWidth / imageAspectRatio)
            }
            val cardOverlap = 100.dp
            val cardHeight = (maxHeight - imageDisplayHeight + cardOverlap).coerceAtLeast(0.dp)

            // Banner image at the top — sized to fit width
            Image(
                painter = painter,
                contentDescription = "Chain Reaction Banner",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )

            // Settings icon top-right (above the card)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 20.dp)
                    .size(44.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onSettings() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_settings),
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            // White card with rounded top corners — height = screen - image + overlap
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer {
                        translationY = cardOffset.value * 200f
                    }
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Name logo
                Image(
                    painter = painterResource(Res.drawable.namelogoflat),
                    contentDescription = "Chain Reaction",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // How to Play button
                SmallRaised3DButton(
                    text = Strings.howToPlay,
                    onClick = onHowToPlay,
                    icon = painterResource(Res.drawable.ic_sports_esports),
                    modifier = Modifier.graphicsLayer {
                        translationY = buttonOffsets[0].value * 80f
                        alpha = 1f - buttonOffsets[0].value
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Local Multiplayer button
                Raised3DButton(
                    text = Strings.friends,
                    topText = Strings.playWith,
                    onClick = onLocalMultiplayer,
                    mainColor = MaterialTheme.colorScheme.primary,
                    shadowColor = Color(0xFF2E8DAD),
                    modifier = Modifier.fillMaxWidth().graphicsLayer {
                        translationY = buttonOffsets[1].value * 80f
                        alpha = 1f - buttonOffsets[1].value
                    },
                    icon = painterResource(Res.drawable.ic_people)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Play vs Bot button
                Raised3DButton(
                    text = Strings.bot,
                    topText = Strings.playWith,
                    onClick = onPlayVsBot,
                    mainColor = MaterialTheme.colorScheme.tertiary,
                    shadowColor = Color(0xFFA8524E),
                    modifier = Modifier.fillMaxWidth().graphicsLayer {
                        translationY = buttonOffsets[2].value * 80f
                        alpha = 1f - buttonOffsets[2].value
                    },
                    icon = painterResource(Res.drawable.ic_smart_toy)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


