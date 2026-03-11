package com.blueedge.chainreaction.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.blueedge.chainreaction.R
import com.blueedge.chainreaction.data.Strings
import com.blueedge.chainreaction.ui.components.Raised3DButton
import com.blueedge.chainreaction.ui.components.SmallRaised3DButton

@Composable
fun MainMenuScreen(
    onLocalMultiplayer: () -> Unit,
    onPlayVsBot: () -> Unit,
    onSettings: () -> Unit,
    onHowToPlay: () -> Unit
) {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SecondaryActionColor)
    ) {
        val isLandscape = maxWidth > maxHeight

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
                        painter = painterResource(id = R.drawable.banner),
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
                        .clip(RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp))
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Name logo (hidden in landscape — banner already visible)
                    // Spacer to maintain consistent spacing
                    SmallRaised3DButton(
                        text = Strings.howToPlay,
                        onClick = onHowToPlay,
                        icon = R.drawable.ic_sports_esports
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Raised3DButton(
                        text = Strings.friends,
                        topText = Strings.playWith,
                        onClick = onLocalMultiplayer,
                        mainColor = MaterialTheme.colorScheme.primary,
                        shadowColor = Color(0xFF2E8DAD),
                        modifier = Modifier.fillMaxWidth(),
                        icon = R.drawable.ic_person
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Raised3DButton(
                        text = Strings.bot,
                        topText = Strings.playWith,
                        onClick = onPlayVsBot,
                        mainColor = MaterialTheme.colorScheme.tertiary,
                        shadowColor = Color(0xFFA8524E),
                        modifier = Modifier.fillMaxWidth(),
                        icon = R.drawable.ic_smart_toy
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
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // --- Portrait layout (original) ---
            val painter = painterResource(id = R.drawable.banner)
            val imageAspectRatio = painter.intrinsicSize.width / painter.intrinsicSize.height
            val imageDisplayHeight = with(LocalDensity.current) {
                (maxWidth / imageAspectRatio)
            }
            val cardOverlap = 32.dp
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
                    imageVector = Icons.Default.Settings,
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
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Name logo
                Image(
                    painter = painterResource(id = R.drawable.namelogoflat),
                    contentDescription = "Chain Reaction",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // How to Play button
                SmallRaised3DButton(
                    text = Strings.howToPlay,
                    onClick = onHowToPlay,
                    icon = R.drawable.ic_sports_esports
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Local Multiplayer button
                Raised3DButton(
                    text = Strings.friends,
                    topText = Strings.playWith,
                    onClick = onLocalMultiplayer,
                    mainColor = MaterialTheme.colorScheme.primary,
                    shadowColor = Color(0xFF2E8DAD),
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.ic_person
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Play vs Bot button
                Raised3DButton(
                    text = Strings.bot,
                    topText = Strings.playWith,
                    onClick = onPlayVsBot,
                    mainColor = MaterialTheme.colorScheme.tertiary,
                    shadowColor = Color(0xFFA8524E),
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.ic_smart_toy
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


