package com.example.chatbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.theme.Dimens
import com.example.chatbot.ui.theme.DocuMindTheme

/** Elevation levels of the design: barely-there shadows (Figma: 2–4 dp offset, 3–4 % black). */
enum class DmElevation { None, Low, High }

/** Applies the design's soft shadow. */
@Composable
fun Modifier.softShadow(shape: Shape, elevation: DmElevation = DmElevation.Low): Modifier {
    if (elevation == DmElevation.None) return this
    val color = DocuMindTheme.colors.shadow
    val (radius, offset) = when (elevation) {
        DmElevation.High -> Dimens.shadowLarge to Dimens.shadowOffsetLarge
        else -> Dimens.shadowSmall to Dimens.shadowOffsetSmall
    }
    return this.dropShadow(shape, Shadow(radius = radius, color = color, offset = DpOffset(0.dp, offset)))
}

/**
 * White surface with a hairline border and soft shadow: cards, bubbles, PDF page, tiles.
 * Pass [onClick] to make the whole card tappable.
 */
@Composable
fun DmSurfaceCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    color: Color = MaterialTheme.colorScheme.surface,
    elevation: DmElevation = DmElevation.Low,
    contentPadding: PaddingValues = PaddingValues(Dimens.space16),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .softShadow(shape, elevation)
            .clip(shape)
            .background(color)
            .border(Dimens.hairline, MaterialTheme.colorScheme.outline, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

/** Quoted passage from a document: violet tint with a violet bar on the leading edge. */
@Composable
fun HighlightedPassage(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    contentPadding: PaddingValues = PaddingValues(Dimens.space16),
) {
    val shape = MaterialTheme.shapes.small
    val accent = MaterialTheme.colorScheme.primary
    Text(
        text = text,
        style = style,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .leadingBar(accent)
            .padding(contentPadding),
    )
}

/** Draws the passage bar; the parent clip rounds it with the passage corners, as in the Figma. */
private fun Modifier.leadingBar(color: Color): Modifier = drawBehind {
    drawRect(color = color, size = Size(Dimens.highlightBar.toPx(), size.height))
}

enum class DmEdge { Top, Bottom }

/** Hairline divider drawn on one edge of a bar (top bar bottom edge, bottom bars top edge). */
@Composable
fun Modifier.hairlineEdge(edge: DmEdge): Modifier {
    val color = MaterialTheme.colorScheme.outline
    return drawBehind {
        val stroke = Dimens.hairline.toPx()
        val y = if (edge == DmEdge.Top) 0f else size.height - stroke
        drawRect(color = color, topLeft = Offset(0f, y), size = Size(size.width, stroke))
    }
}

/** Chat message written by the user: white bubble aligned to the end. */
@Composable
fun UserMessageBubble(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        DmSurfaceCard(
            modifier = Modifier.fillMaxWidth(Dimens.bubbleMaxWidthFraction).wrapContentWidth(Alignment.End),
            shape = DocuMindTheme.shapes.userBubble,
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/**
 * Cream backdrop behind the status and navigation bars, so the system icons stay legible
 * when a cream screen scrolls underneath them (edge-to-edge protection).
 */
@Composable
fun SystemBarsProtection(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.background.copy(alpha = SYSTEM_BAR_SCRIM_ALPHA)
    Box(modifier = modifier.fillMaxSize()) {
        Spacer(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(color),
        )
        Spacer(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .background(color),
        )
    }
}

private const val SYSTEM_BAR_SCRIM_ALPHA = 0.92f

@Preview
@Composable
private fun SurfacesPreview() {
    ChatbotTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.space24),
            verticalArrangement = Arrangement.spacedBy(Dimens.space16),
        ) {
            DmSurfaceCard(modifier = Modifier.fillMaxWidth(), onClick = {}) {
                Text("Surface card", style = MaterialTheme.typography.titleMedium)
            }
            UserMessageBubble(text = "What drove revenue growth in 2024?")
            HighlightedPassage(text = "Revenue reached \$12.8 million in 2024, an increase of 18% year over year.")
        }
    }
}
