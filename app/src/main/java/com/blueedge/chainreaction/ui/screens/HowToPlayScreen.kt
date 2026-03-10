package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.blueedge.chainreaction.data.CellState
import com.blueedge.chainreaction.data.Strings
import com.blueedge.chainreaction.ui.components.GridCell
import com.blueedge.chainreaction.ui.components.Raised3DButton
import com.blueedge.chainreaction.ui.theme.PlayerColors
import com.blueedge.chainreaction.ui.theme.SecondaryActionColor
import com.blueedge.chainreaction.ui.theme.SecondaryActionShadow
import kotlinx.coroutines.delay

private data class TutorialStep(
    val animationIndex: Int,
    val titleKey: String,
    val descriptionKey: String
)

private val tutorialSteps = listOf(
    TutorialStep(
        animationIndex = 0,
        titleKey = "Tap to Place",
        descriptionKey = "Tap any cell to place a dot.\nOnce a cell has 4 dots, it explodes!"
    ),
    TutorialStep(
        animationIndex = 1,
        titleKey = "Capture Cells",
        descriptionKey = "When your dots explode into a neighbor, you take over that cell — even if it belongs to someone else!"
    ),
    TutorialStep(
        animationIndex = 2,
        titleKey = "Win the Game",
        descriptionKey = "Eliminate every opponent by\ncapturing all their cells. Last player standing wins!"
    )
)

@Composable
fun HowToPlayScreen(
    onBack: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val portraitScrollState = rememberScrollState()
    val landscapeScrollState = rememberScrollState()

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
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        val isLandscape = maxWidth > maxHeight
        val outerColumnModifier = if (isLandscape) {
            Modifier.fillMaxSize().verticalScroll(landscapeScrollState)
        } else {
            Modifier.fillMaxSize()
        }
        val middleColumnModifier = if (isLandscape) {
            Modifier.widthIn(max = 480.dp).fillMaxWidth().padding(24.dp)
        } else {
            Modifier.weight(1f).fillMaxWidth().padding(24.dp)
        }
        val contentColumnModifier = if (isLandscape) {
            Modifier
        } else {
            Modifier.weight(1f).verticalScroll(portraitScrollState)
        }

        // Outer column: full-screen scrollable in landscape so side empty space scrolls too
        Column(
            modifier = outerColumnModifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = middleColumnModifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scrollable content area (portrait: scrollable+weight; landscape: plain column)
                Column(
                    modifier = contentColumnModifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = Strings.howToPlay,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tutorial mini-board pager card
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
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AnimatedContent(
                                    targetState = currentPage,
                                    transitionSpec = {
                                        if (targetState > initialState) {
                                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                                        } else {
                                            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                                        }
                                    },
                                    label = "tutorial_page",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pointerInput(Unit) {
                                            var totalDrag = 0f
                                            detectHorizontalDragGestures(
                                                onDragStart = { totalDrag = 0f },
                                                onDragEnd = {
                                                    if (totalDrag < -100 && currentPage < tutorialSteps.size - 1) {
                                                        currentPage++
                                                    } else if (totalDrag > 100 && currentPage > 0) {
                                                        currentPage--
                                                    }
                                                    totalDrag = 0f
                                                },
                                                onDragCancel = { totalDrag = 0f },
                                                onHorizontalDrag = { _, dragAmount ->
                                                    totalDrag += dragAmount
                                                }
                                            )
                                        }
                                ) { page ->
                                    val step = tutorialSteps[page]
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Mini animated game board instead of GIF
                                        TutorialMiniBoard(
                                            tutorialIndex = step.animationIndex,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.background)
                                                .padding(8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = Strings.tr(step.titleKey),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = Strings.tr(step.descriptionKey),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Navigation arrows + page indicator dots
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (currentPage > 0) currentPage-- },
                                        enabled = currentPage > 0
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                            contentDescription = "Previous",
                                            tint = if (currentPage > 0)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(tutorialSteps.size) { index ->
                                            val isSelected = currentPage == index
                                            Box(
                                                modifier = Modifier
                                                    .size(if (isSelected) 10.dp else 8.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected)
                                                            MaterialTheme.colorScheme.primary
                                                        else
                                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                    )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = { if (currentPage < tutorialSteps.size - 1) currentPage++ },
                                        enabled = currentPage < tutorialSteps.size - 1
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Next",
                                            tint = if (currentPage < tutorialSteps.size - 1)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
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
                                            append(Strings.simple)
                                        }
                                        withStyle(SpanStyle(color = Color.Gray)) {
                                            append(" vs ")
                                        }
                                        withStyle(SpanStyle(color = classicColor)) {
                                            append(Strings.classic)
                                        }
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )

                                ModeDiffItem(
                                    topic = Strings.firstMove,
                                    simpleText = Strings.firstMoveSimple,
                                    classicText = Strings.firstMoveClassic
                                )

                                ModeDiffItem(
                                    topic = Strings.placement,
                                    simpleText = Strings.placementSimple,
                                    classicText = Strings.placementClassic
                                )

                                ModeDiffItem(
                                    topic = Strings.criticalMass,
                                    simpleText = Strings.criticalMassSimple,
                                    classicText = Strings.criticalMassClassic
                                )

                                ModeDiffItem(
                                    topic = Strings.explosions,
                                    simpleText = Strings.explosionsSimple,
                                    classicText = Strings.explosionsClassic
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // In landscape: back button scrolls with content (non-sticky)
                    if (isLandscape) {
                        Raised3DButton(
                            text = Strings.back,
                            onClick = onBack,
                            mainColor = SecondaryActionColor,
                            shadowColor = SecondaryActionShadow,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                } // end scrollable content column

                // In portrait: back button is sticky at the bottom
                if (!isLandscape) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Raised3DButton(
                        text = Strings.back,
                        onClick = onBack,
                        mainColor = SecondaryActionColor,
                        shadowColor = SecondaryActionShadow,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            } // end constrained content column
        } // end outer full-screen column
    }
}

/**
 * Animated 5×5 mini game board used in the tutorial.
 * Cycles through pre-defined frame sequences on loop to illustrate game mechanics.
 */
@Composable
private fun TutorialMiniBoard(
    tutorialIndex: Int,
    modifier: Modifier = Modifier
) {
    // Each frame: Map<(row,col), (ownerId,dots)> paired with duration in ms
    val frameSequences = remember {
        listOf(
            // Tutorial 0: "Tap to Place" — place dots at centre until it explodes
            listOf(
                emptyMap<Pair<Int, Int>, Pair<Int, Int>>() to 600L,
                mapOf((2 to 2) to (1 to 1)) to 700L,
                mapOf((2 to 2) to (1 to 2)) to 700L,
                mapOf((2 to 2) to (1 to 3)) to 700L,
                mapOf((2 to 2) to (1 to 4)) to 400L, // critical mass → explosion!
                mapOf(
                    (1 to 2) to (1 to 1),
                    (3 to 2) to (1 to 1),
                    (2 to 1) to (1 to 1),
                    (2 to 3) to (1 to 1)
                ) to 1000L
            ),
            // Tutorial 1: "Capture Cells" — corner explosion captures opponent cell
            listOf(
                mapOf((0 to 1) to (2 to 1)) to 700L,
                mapOf((0 to 0) to (1 to 1), (0 to 1) to (2 to 1)) to 800L,
                mapOf((0 to 0) to (1 to 2), (0 to 1) to (2 to 1)) to 600L, // about to explode!
                mapOf(
                    (0 to 1) to (1 to 2), // P2's cell captured by P1
                    (1 to 0) to (1 to 1)
                ) to 1200L
            ),
            // Tutorial 2: "Win the Game" — P1 captures P2's last cell
            listOf(
                mapOf(
                    (0 to 0) to (1 to 1),
                    (2 to 2) to (1 to 1),
                    (3 to 1) to (1 to 1),
                    (3 to 3) to (1 to 1),
                    (0 to 1) to (2 to 1) // P2's last cell
                ) to 800L,
                mapOf(
                    (0 to 0) to (1 to 2), // corner, about to explode!
                    (2 to 2) to (1 to 1),
                    (3 to 1) to (1 to 1),
                    (3 to 3) to (1 to 1),
                    (0 to 1) to (2 to 1)
                ) to 600L,
                mapOf( // P1 wins — P2 eliminated!
                    (0 to 1) to (1 to 2), // captured from P2
                    (1 to 0) to (1 to 1),
                    (2 to 2) to (1 to 1),
                    (3 to 1) to (1 to 1),
                    (3 to 3) to (1 to 1)
                ) to 1500L
            )
        )
    }

    val frames = frameSequences[tutorialIndex]
    var frameIndex by remember(tutorialIndex) { mutableIntStateOf(0) }
    var prevFrameIndex by remember(tutorialIndex) { mutableIntStateOf(0) }

    LaunchedEffect(tutorialIndex) {
        frameIndex = 0
        prevFrameIndex = 0
        while (true) {
            delay(frames[frameIndex].second)
            prevFrameIndex = frameIndex
            frameIndex = (frameIndex + 1) % frames.size
        }
    }

    val currentCells = frames[frameIndex].first
    val prevCells = frames[prevFrameIndex].first

    Column(modifier = modifier) {
        for (row in 0 until 5) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0 until 5) {
                    val key = row to col
                    val current = currentCells[key]
                    val prev = prevCells[key]
                    val ownerId = current?.first ?: 0
                    val dots = current?.second ?: 0
                    val prevOwnerId = prev?.first ?: 0
                    val prevDots = prev?.second ?: 0
                    // When ownership changes (capture or cell cleared), start dot animation from 0
                    // so new dots appear rather than sliding from the previous owner's count.
                    val previousDots = if (prevOwnerId == ownerId) prevDots else 0
                    val ownerColor = if (ownerId > 0) PlayerColors[ownerId - 1] else Color.Transparent

                    GridCell(
                        cellState = CellState(
                            ownerId = ownerId,
                            dots = dots,
                            previousDots = previousDots
                        ),
                        ownerColor = ownerColor,
                        currentPlayerColor = ownerColor,
                        isExploding = false,
                        isCurrentPlayer = ownerId > 0,
                        onClick = {},
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    )
                }
            }
        }
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

