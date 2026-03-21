package com.blueedge.chainreaction.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.annotation.DrawableRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blueedge.chainreaction.audio.SoundManager

/**
 * A raised 3D button used throughout the app.
 *
 * For simple single-line centered text, just pass [text].
 * For a main-menu style button with optional icon and subtitle, also pass [topText] and [icon].
 * The button automatically switches to the larger menu layout when [topText] or [icon] is provided.
 */
@Composable
fun Raised3DButton(
    text: String,
    onClick: () -> Unit,
    mainColor: Color = Color(0xFFE8E8E8),
    shadowColor: Color = Color(0xFFBBBBBB),
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    topText: String = "",
    @DrawableRes icon: Int? = null
) {
    val isMenuStyle = topText.isNotEmpty() || icon != null
    val totalHeight = if (isMenuStyle) 86.dp else 66.dp
    val buttonHeight = if (isMenuStyle) 80.dp else 60.dp
    val cornerRadius = if (isMenuStyle) 20.dp else 18.dp

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
            .height(totalHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    SoundManager.playBop()
                    onClick()
                }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Shadow / bottom layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(cornerRadius))
                .background(shadowColor)
        )
        // Main button layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .offset(y = yOffset)
                .clip(RoundedCornerShape(cornerRadius))
                .background(mainColor),
            contentAlignment = Alignment.Center
        ) {
            if (isMenuStyle) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        if (topText.isNotEmpty()) {
                            Text(
                                text = topText,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                    }
                }
            } else {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * A compact raised 3D button for secondary actions (e.g. "How to Play", "Terms", "Privacy Policy").
 * Uses the same shadow-layer + press-animation pattern as [Raised3DButton].
 */
@Composable
fun SmallRaised3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mainColor: Color = Color(0xFFE8E8E8),
    shadowColor: Color = Color(0xFFBBBBBB),
    textColor: Color = Color(0xFF5A5A5A),
    @DrawableRes icon: Int? = null,
    iconTint: Color = textColor
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shadowHeight = 4.dp
    val yOffset by animateDpAsState(
        targetValue = if (isPressed) shadowHeight else 0.dp,
        animationSpec = tween(durationMillis = 80),
        label = "smallButtonPress"
    )
    val cornerRadius = 12.dp
    val buttonHeight = 40.dp
    val totalHeight = buttonHeight + shadowHeight

    Box(
        modifier = modifier
            .height(totalHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    SoundManager.playBop()
                    onClick()
                }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Shadow layer
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(buttonHeight)
                .clip(RoundedCornerShape(cornerRadius))
                .background(shadowColor)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Spacer(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Transparent
            )
        }
        // Main layer
        Row(
            modifier = Modifier
                .height(buttonHeight)
                .offset(y = yOffset)
                .clip(RoundedCornerShape(cornerRadius))
                .background(mainColor)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
