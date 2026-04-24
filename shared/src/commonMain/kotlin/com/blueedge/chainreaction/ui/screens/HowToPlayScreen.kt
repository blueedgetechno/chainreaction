package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import org.jetbrains.compose.resources.painterResource
import chainreaction.shared.generated.resources.Res
import chainreaction.shared.generated.resources.ic_keyboard_arrow_left
import chainreaction.shared.generated.resources.ic_keyboard_arrow_right
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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

private data class TutorialExplosionMove(
        val fromRow: Int,
        val fromCol: Int,
        val toRow: Int,
        val toCol: Int,
        val playerId: Int
)

private data class TutorialFrame(
        val cells: Map<Pair<Int, Int>, Pair<Int, Int>>,
        val durationMs: Long,
        val explosionMoves: List<TutorialExplosionMove> = emptyList()
)

private val tutorialSteps =
        listOf(
                TutorialStep(
                        animationIndex = 0,
                        titleKey = "Tap to Place",
                        descriptionKey =
                                "Tap any cell to place a dot.\nOnce a cell has 4 dots, it explodes!"
                ),
                TutorialStep(
                        animationIndex = 1,
                        titleKey = "Capture Cells",
                        descriptionKey =
                                "When your dots explode into a neighbor, you take over that cell — even if it belongs to someone else!"
                ),
                TutorialStep(
                        animationIndex = 2,
                        titleKey = "Win the Game",
                        descriptionKey =
                                "Eliminate every opponent by\ncapturing all their cells. Last player standing wins!"
                )
        )

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    val portraitScrollState = rememberScrollState()
    val landscapeScrollState = rememberScrollState()

    BoxWithConstraints(
            modifier =
                    Modifier.fillMaxSize()
                            .background(
                                    Brush.verticalGradient(
                                            colors =
                                                    listOf(
                                                            MaterialTheme.colorScheme
                                                                    .primaryContainer,
                                                            MaterialTheme.colorScheme.background
                                                    )
                                    )
                            ),
            contentAlignment = Alignment.TopCenter
    ) {
        val isLandscape = maxWidth > maxHeight
        val outerColumnModifier =
                if (isLandscape) {
                    Modifier.fillMaxSize().verticalScroll(landscapeScrollState)
                } else {
                    Modifier.fillMaxSize()
                }

        // Outer column: full-screen scrollable in landscape so side empty space scrolls too
        Column(modifier = outerColumnModifier, horizontalAlignment = Alignment.CenterHorizontally) {
            val middleColumnModifier =
                    if (isLandscape) {
                        Modifier.widthIn(max = 480.dp).fillMaxWidth().padding(24.dp)
                    } else {
                        Modifier.weight(1f).fillMaxWidth().padding(24.dp)
                    }
            Column(
                    modifier = middleColumnModifier,
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val contentColumnModifier =
                        if (isLandscape) {
                            Modifier
                        } else {
                            Modifier.weight(1f).verticalScroll(portraitScrollState)
                        }
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
                                modifier =
                                        Modifier.matchParentSize()
                                                .offset(y = cardShadowOffset)
                                                .clip(RoundedCornerShape(24.dp))
                                                .background(Color(0xFFD0D0D0))
                        )
                        // Main card
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth()
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
                                                slideInHorizontally { it } togetherWith
                                                        slideOutHorizontally { -it }
                                            } else {
                                                slideInHorizontally { -it } togetherWith
                                                        slideOutHorizontally { it }
                                            }
                                        },
                                        label = "tutorial_page",
                                        modifier =
                                                Modifier.fillMaxWidth().pointerInput(Unit) {
                                                    var totalDrag = 0f
                                                    detectHorizontalDragGestures(
                                                            onDragStart = { totalDrag = 0f },
                                                            onDragEnd = {
                                                                if (totalDrag < -100 &&
                                                                                currentPage <
                                                                                        tutorialSteps
                                                                                                .size -
                                                                                                1
                                                                ) {
                                                                    currentPage++
                                                                } else if (totalDrag > 100 &&
                                                                                currentPage > 0
                                                                ) {
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
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .aspectRatio(1f)
                                                                .clip(RoundedCornerShape(16.dp))
                                                                .background(
                                                                        MaterialTheme.colorScheme
                                                                                .background
                                                                )
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
                                                painter =
                                                        painterResource(Res.drawable.ic_keyboard_arrow_left),
                                                contentDescription = "Previous",
                                                tint =
                                                        if (currentPage > 0)
                                                                MaterialTheme.colorScheme.primary
                                                        else
                                                                MaterialTheme.colorScheme.onSurface
                                                                        .copy(alpha = 0.2f),
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
                                                    modifier =
                                                            Modifier.size(
                                                                            if (isSelected) 10.dp
                                                                            else 8.dp
                                                                    )
                                                                    .clip(CircleShape)
                                                                    .background(
                                                                            if (isSelected)
                                                                                    MaterialTheme
                                                                                            .colorScheme
                                                                                            .primary
                                                                            else
                                                                                    MaterialTheme
                                                                                            .colorScheme
                                                                                            .onSurface
                                                                                            .copy(
                                                                                                    alpha =
                                                                                                            0.3f
                                                                                            )
                                                                    )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                            onClick = {
                                                if (currentPage < tutorialSteps.size - 1)
                                                        currentPage++
                                            },
                                            enabled = currentPage < tutorialSteps.size - 1
                                    ) {
                                        Icon(
                                                painter =
                                                        painterResource(Res.drawable.ic_keyboard_arrow_right),
                                                contentDescription = "Next",
                                                tint =
                                                        if (currentPage < tutorialSteps.size - 1)
                                                                MaterialTheme.colorScheme.primary
                                                        else
                                                                MaterialTheme.colorScheme.onSurface
                                                                        .copy(alpha = 0.2f),
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
                                modifier =
                                        Modifier.matchParentSize()
                                                .offset(y = cardShadowOffset2)
                                                .clip(RoundedCornerShape(24.dp))
                                                .background(Color(0xFFD0D0D0))
                        )
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth()
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
                                        text =
                                                buildAnnotatedString {
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
 * Animated 5×5 mini game board used in the tutorial. Cycles through pre-defined frame sequences on
 * loop to illustrate game mechanics. Explosion frames include move data so a Canvas overlay
 * animates dots translating from the source cell to each neighbor, matching the real game.
 */
@Composable
private fun TutorialMiniBoard(tutorialIndex: Int, modifier: Modifier = Modifier) {
    val frameSequences = remember {
        listOf(
                // Tutorial 0: "Tap to Place" — alternating turns, blue center + red corners
                listOf(
                        TutorialFrame(emptyMap(), 600L),
                        // Blue places 1 dot at center
                        TutorialFrame(mapOf((2 to 2) to (1 to 1)), 700L),
                        // Red places 1 dot at top-left corner
                        TutorialFrame(mapOf((2 to 2) to (1 to 1), (0 to 0) to (2 to 1)), 700L),
                        // Blue adds 2nd dot at center
                        TutorialFrame(mapOf((2 to 2) to (1 to 2), (0 to 0) to (2 to 1)), 700L),
                        // Red places 1 dot at bottom-right corner
                        TutorialFrame(
                                mapOf(
                                        (2 to 2) to (1 to 2),
                                        (0 to 0) to (2 to 1),
                                        (4 to 4) to (2 to 1)
                                ),
                                700L
                        ),
                        // Blue adds 3rd dot at center
                        TutorialFrame(
                                mapOf(
                                        (2 to 2) to (1 to 3),
                                        (0 to 0) to (2 to 1),
                                        (4 to 4) to (2 to 1)
                                ),
                                700L
                        ),
                        // Red places 1 dot at top-right corner
                        TutorialFrame(
                                mapOf(
                                        (2 to 2) to (1 to 3),
                                        (0 to 0) to (2 to 1),
                                        (4 to 4) to (2 to 1),
                                        (0 to 4) to (2 to 1)
                                ),
                                700L
                        ),
                        // Blue adds 4th dot — critical mass!
                        TutorialFrame(
                                mapOf(
                                        (2 to 2) to (1 to 4),
                                        (0 to 0) to (2 to 1),
                                        (4 to 4) to (2 to 1),
                                        (0 to 4) to (2 to 1)
                                ),
                                400L
                        ),
                        // Explosion: center splits to 4 neighbors, red corners stay
                        TutorialFrame(
                                mapOf(
                                        (1 to 2) to (1 to 1),
                                        (3 to 2) to (1 to 1),
                                        (2 to 1) to (1 to 1),
                                        (2 to 3) to (1 to 1),
                                        (0 to 0) to (2 to 1),
                                        (4 to 4) to (2 to 1),
                                        (0 to 4) to (2 to 1)
                                ),
                                1200L,
                                listOf(
                                        TutorialExplosionMove(2, 2, 1, 2, 1),
                                        TutorialExplosionMove(2, 2, 3, 2, 1),
                                        TutorialExplosionMove(2, 2, 2, 1, 1),
                                        TutorialExplosionMove(2, 2, 2, 3, 1)
                                )
                        )
                ),
                // Tutorial 1: "Capture Cells" — blue places at (3,2) triggering a chain reaction
                listOf(
                        // Initial board layout
                        TutorialFrame(
                                mapOf(
                                        (1 to 2) to (1 to 1),
                                        (2 to 0) to (2 to 1),
                                        (2 to 1) to (1 to 2),
                                        (2 to 3) to (1 to 1),
                                        (3 to 0) to (2 to 1),
                                        (3 to 1) to (2 to 3),
                                        (3 to 2) to (1 to 3),
                                        (4 to 0) to (2 to 1),
                                        (4 to 2) to (2 to 1)
                                ),
                                800L
                        ),
                        // Blue places at (3,2): B3 → B4 (critical mass!)
                        TutorialFrame(
                                mapOf(
                                        (1 to 2) to (1 to 1),
                                        (2 to 0) to (2 to 1),
                                        (2 to 1) to (1 to 2),
                                        (2 to 3) to (1 to 1),
                                        (3 to 0) to (2 to 1),
                                        (3 to 1) to (2 to 3),
                                        (3 to 2) to (1 to 4),
                                        (4 to 0) to (2 to 1),
                                        (4 to 2) to (2 to 1)
                                ),
                                400L
                        ),
                        // (3,2) explodes → dots travel to neighbors; (3,1) captured R3→B4
                        TutorialFrame(
                                mapOf(
                                        (1 to 2) to (1 to 1),
                                        (2 to 0) to (2 to 1),
                                        (2 to 1) to (1 to 2),
                                        (2 to 2) to (1 to 1),
                                        (2 to 3) to (1 to 1),
                                        (3 to 0) to (2 to 1),
                                        (3 to 1) to (1 to 4),
                                        (3 to 3) to (1 to 1),
                                        (4 to 0) to (2 to 1),
                                        (4 to 2) to (1 to 2)
                                ),
                                300L,
                                listOf(
                                        TutorialExplosionMove(3, 2, 2, 2, 1),
                                        TutorialExplosionMove(3, 2, 4, 2, 1),
                                        TutorialExplosionMove(3, 2, 3, 1, 1),
                                        TutorialExplosionMove(3, 2, 3, 3, 1)
                                )
                        ),
                        // Chain: (3,1) explodes → captures (3,0) from red, spreads further
                        TutorialFrame(
                                mapOf(
                                        (1 to 2) to (1 to 1),
                                        (2 to 0) to (2 to 1),
                                        (2 to 1) to (1 to 3),
                                        (2 to 2) to (1 to 1),
                                        (2 to 3) to (1 to 1),
                                        (3 to 0) to (1 to 2),
                                        (3 to 2) to (1 to 1),
                                        (3 to 3) to (1 to 1),
                                        (4 to 0) to (2 to 1),
                                        (4 to 1) to (1 to 1),
                                        (4 to 2) to (1 to 2)
                                ),
                                1500L,
                                listOf(
                                        TutorialExplosionMove(3, 1, 2, 1, 1),
                                        TutorialExplosionMove(3, 1, 4, 1, 1),
                                        TutorialExplosionMove(3, 1, 3, 0, 1),
                                        TutorialExplosionMove(3, 1, 3, 2, 1)
                                )
                        )
                ),
                // Tutorial 2: "Win the Game" — P1 captures P2's last cell
                listOf(
                        // Initial board layout ((3,0) has R2, bottom row empty except corner)
                        TutorialFrame(
                                mapOf(
                                        (1 to 2) to (1 to 1),
                                        (2 to 0) to (2 to 1),
                                        (2 to 1) to (1 to 2),
                                        (2 to 3) to (1 to 1),
                                        (3 to 0) to (2 to 2),
                                        (3 to 1) to (2 to 3),
                                        (3 to 2) to (1 to 3),
                                        (4 to 0) to (2 to 1)
                                ),
                                800L
                        ),
                        // Blue places at (3,2): B3 → B4 (critical mass!)
                        TutorialFrame(
                                mapOf(
                                        (1 to 2) to (1 to 1),
                                        (2 to 0) to (2 to 1),
                                        (2 to 1) to (1 to 2),
                                        (2 to 3) to (1 to 1),
                                        (3 to 0) to (2 to 2),
                                        (3 to 1) to (2 to 3),
                                        (3 to 2) to (1 to 4),
                                        (4 to 0) to (2 to 1)
                                ),
                                400L
                        ),
                        // (3,2) explodes → dots travel to neighbors; (3,1) captured R3→B4
                        TutorialFrame(
                                mapOf(
                                        (1 to 2) to (1 to 1),
                                        (2 to 0) to (2 to 1),
                                        (2 to 1) to (1 to 2),
                                        (2 to 2) to (1 to 1),
                                        (2 to 3) to (1 to 1),
                                        (3 to 0) to (2 to 2),
                                        (3 to 1) to (1 to 4),
                                        (3 to 3) to (1 to 1),
                                        (4 to 0) to (2 to 1),
                                        (4 to 2) to (1 to 1)
                                ),
                                300L,
                                listOf(
                                        TutorialExplosionMove(3, 2, 2, 2, 1),
                                        TutorialExplosionMove(3, 2, 4, 2, 1),
                                        TutorialExplosionMove(3, 2, 3, 1, 1),
                                        TutorialExplosionMove(3, 2, 3, 3, 1)
                                )
                        ),
                        // Chain: (3,1) explodes → captures (3,0) R2→B3 (critical!) and (4,1)
                        TutorialFrame(
                                mapOf(
                                        (1 to 2) to (1 to 1),
                                        (2 to 0) to (2 to 1),
                                        (2 to 1) to (1 to 3),
                                        (2 to 2) to (1 to 1),
                                        (2 to 3) to (1 to 1),
                                        (3 to 0) to (1 to 3),
                                        (3 to 2) to (1 to 1),
                                        (3 to 3) to (1 to 1),
                                        (4 to 0) to (2 to 1),
                                        (4 to 1) to (1 to 1),
                                        (4 to 2) to (1 to 1)
                                ),
                                300L,
                                listOf(
                                        TutorialExplosionMove(3, 1, 2, 1, 1),
                                        TutorialExplosionMove(3, 1, 4, 1, 1),
                                        TutorialExplosionMove(3, 1, 3, 0, 1),
                                        TutorialExplosionMove(3, 1, 3, 2, 1)
                                )
                        ),
                        // Chain: (3,0) B3 explodes → captures (2,0) and (4,0) from red
                        TutorialFrame(
                                mapOf(
                                        (1 to 2) to (1 to 1),
                                        (2 to 0) to (1 to 2),
                                        (2 to 1) to (1 to 3),
                                        (2 to 2) to (1 to 1),
                                        (2 to 3) to (1 to 1),
                                        (3 to 1) to (1 to 1),
                                        (3 to 2) to (1 to 1),
                                        (3 to 3) to (1 to 1),
                                        (4 to 0) to (1 to 2),
                                        (4 to 1) to (1 to 1),
                                        (4 to 2) to (1 to 1)
                                ),
                                300L,
                                listOf(
                                        TutorialExplosionMove(3, 0, 2, 0, 1),
                                        TutorialExplosionMove(3, 0, 4, 0, 1),
                                        TutorialExplosionMove(3, 0, 3, 1, 1)
                                )
                        ),
                        // Chain: (4,0) corner B2 explodes → all red eliminated!
                        TutorialFrame(
                                mapOf(
                                        (1 to 2) to (1 to 1),
                                        (2 to 0) to (1 to 2),
                                        (2 to 1) to (1 to 3),
                                        (2 to 2) to (1 to 1),
                                        (2 to 3) to (1 to 1),
                                        (3 to 0) to (1 to 1),
                                        (3 to 1) to (1 to 1),
                                        (3 to 2) to (1 to 1),
                                        (3 to 3) to (1 to 1),
                                        (4 to 1) to (1 to 2),
                                        (4 to 2) to (1 to 1)
                                ),
                                1500L,
                                listOf(
                                        TutorialExplosionMove(4, 0, 3, 0, 1),
                                        TutorialExplosionMove(4, 0, 4, 1, 1)
                                )
                        )
                )
        )
    }

    val frames = frameSequences[tutorialIndex]
    // What the grid currently renders (updated by the animation loop)
    var displayCells by remember(tutorialIndex) { mutableStateOf(frames[0].cells) }
    var prevDisplayCells by remember(tutorialIndex) { mutableStateOf(frames[0].cells) }
    var activeExplosionMoves by
            remember(tutorialIndex) { mutableStateOf(emptyList<TutorialExplosionMove>()) }
    val explosionProgress = remember(tutorialIndex) { Animatable(0f) }

    LaunchedEffect(tutorialIndex) {
        var frameIndex = 0
        displayCells = frames[0].cells
        prevDisplayCells = frames[0].cells
        activeExplosionMoves = emptyList()
        while (true) {
            val frame = frames[frameIndex]
            if (frame.explosionMoves.isNotEmpty()) {
                // Phase 1: empty source cells, animate dots travelling to targets
                val sourceCells = frame.explosionMoves.map { it.fromRow to it.fromCol }.toSet()
                prevDisplayCells = displayCells
                displayCells = displayCells.filterKeys { it !in sourceCells }
                activeExplosionMoves = frame.explosionMoves
                explosionProgress.snapTo(0f)
                explosionProgress.animateTo(1f, tween(300))
                activeExplosionMoves = emptyList()
                // Phase 2: show result board, animate dot count changes on receiving cells
                prevDisplayCells = displayCells
                displayCells = frame.cells
                delay(200L) // gap between splitting waves
            } else {
                prevDisplayCells = displayCells
                displayCells = frame.cells
            }
            delay(frame.durationMs)
            frameIndex = (frameIndex + 1) % frames.size
        }
    }

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until 5) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0 until 5) {
                        val key = row to col
                        val current = displayCells[key]
                        val prev = prevDisplayCells[key]
                        val ownerId = current?.first ?: 0
                        val dots = current?.second ?: 0
                        val prevOwnerId = prev?.first ?: 0
                        val prevDots = prev?.second ?: 0
                        val previousDots = if (prevOwnerId == ownerId) prevDots else if (dots > 0) -1 else 0
                        val ownerColor =
                                if (ownerId > 0) PlayerColors[ownerId - 1] else Color.Transparent

                        GridCell(
                                cellState =
                                        CellState(
                                                ownerId = ownerId,
                                                dots = dots,
                                                previousDots = previousDots
                                        ),
                                ownerColor = ownerColor,
                                currentPlayerColor = ownerColor,
                                isExploding = false,
                                isCurrentPlayer = false,
                                onClick = {},
                                modifier = Modifier.weight(1f).fillMaxSize()
                        )
                    }
                }
            }
        }

        // Explosion movement overlay: dots translating from source to target cells
        if (activeExplosionMoves.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSizePx = size.width / 5f
                val progress = explosionProgress.value
                val circleRadius = cellSizePx * 0.38f
                val dotRadius = circleRadius * 0.2f

                for (move in activeExplosionMoves) {
                    val fromCenter =
                            Offset(
                                    x = (move.fromCol + 0.5f) * cellSizePx,
                                    y = (move.fromRow + 0.5f) * cellSizePx
                            )
                    val toCenter =
                            Offset(
                                    x = (move.toCol + 0.5f) * cellSizePx,
                                    y = (move.toRow + 0.5f) * cellSizePx
                            )
                    val currentPos =
                            Offset(
                                    x = fromCenter.x + (toCenter.x - fromCenter.x) * progress,
                                    y = fromCenter.y + (toCenter.y - fromCenter.y) * progress
                            )

                    val moveColor = PlayerColors[move.playerId - 1]
                    drawCircle(color = moveColor, radius = circleRadius, center = currentPos)
                    drawCircle(color = Color.White, radius = dotRadius, center = currentPos)
                }
            }
        }
    }
}

@Composable
private fun ModeDiffItem(topic: String, simpleText: String, classicText: String) {
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
                    modifier =
                            Modifier.padding(top = 6.dp)
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
                    modifier =
                            Modifier.padding(top = 6.dp)
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
