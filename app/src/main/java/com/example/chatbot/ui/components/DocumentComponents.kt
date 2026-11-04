package com.example.chatbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.example.chatbot.R
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.theme.Dimens
import com.example.chatbot.ui.theme.DocuMindTheme

/** White document header: back, title + subtitle, more options. Draws behind the status bar. */
@Composable
fun DocumentTopBar(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .hairlineEdge(DmEdge.Bottom)
            .windowInsetsPadding(WindowInsets.statusBars)
            .heightIn(min = Dimens.topBarHeight)
            .padding(horizontal = Dimens.space8),
        horizontalArrangement = Arrangement.spacedBy(Dimens.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DmIconButton(
            icon = painterResource(R.drawable.ic_arrow_left),
            contentDescription = stringResource(R.string.cd_back),
            onClick = onBackClick,
        )
        Column(
            modifier = Modifier.weight(1f).padding(vertical = Dimens.space8),
            verticalArrangement = Arrangement.spacedBy(Dimens.space4),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DmIconButton(
            icon = painterResource(R.drawable.ic_more_vertical),
            contentDescription = stringResource(R.string.cd_more_options),
            onClick = onMoreClick,
        )
    }
}

/** One entry of [DocumentTabBar]. */
data class DocumentTabItem(val label: String, val icon: Painter)

/** Bottom tab bar of the document detail. Stateless: the caller owns the selection. */
@Composable
fun DocumentTabBar(
    tabs: List<DocumentTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .hairlineEdge(DmEdge.Top)
            .windowInsetsPadding(WindowInsets.navigationBars)
            // At least 72 dp, but grows with large fonts so every tab keeps the same height.
            .heightIn(min = Dimens.tabBarHeight)
            .height(IntrinsicSize.Min)
            .padding(horizontal = Dimens.space16, vertical = Dimens.space8)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.space8),
    ) {
        tabs.forEachIndexed { index, tab ->
            DocumentTab(
                item = tab,
                selected = index == selectedIndex,
                onClick = { onTabSelected(index) },
            )
        }
    }
}

@Composable
private fun RowScope.DocumentTab(
    item: DocumentTabItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(shape)
            .background(if (selected) colors.background else colors.surface)
            .border(Dimens.hairline, colors.outline, shape)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = Dimens.space12, vertical = Dimens.space8),
        verticalArrangement = Arrangement.spacedBy(Dimens.space6, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DmIconTile(
            icon = item.icon,
            style = if (selected) DmIconTileStyle.Accent else DmIconTileStyle.Neutral,
            shape = DocuMindTheme.shapes.pill,
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.primary else colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Decorative PDF thumbnail of a document card. */
@Composable
fun DocumentCover(
    title: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val type = DocuMindTheme.typography
    // The cover is a decorative picture of a page (hidden from accessibility services),
    // so its tiny print keeps a fixed size instead of following the font size setting.
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1f)) {
        Column(
            modifier = modifier
                .clearAndSetSemantics {}
                .size(width = Dimens.coverWidth, height = Dimens.coverHeight)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(colors.primaryContainer)
                .padding(Dimens.coverPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.coverGap),
        ) {
            Text(text = title, style = type.coverTitle, color = colors.primary, maxLines = 1, softWrap = false)
            Box(Modifier.size(width = Dimens.coverRuleLong, height = Dimens.coverRule).background(colors.primary))
            Box(Modifier.size(width = Dimens.coverRuleShort, height = Dimens.coverRule).background(colors.outline))
            Text(text = stringResource(R.string.cover_label), style = type.coverLabel, color = colors.onSurfaceVariant, maxLines = 1, softWrap = false)
        }
    }
}

/** Library card for a document: cover, title, metadata, optional activity and chevron. */
@Composable
fun DocumentListItem(
    title: String,
    coverTitle: String,
    metadata: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activity: String? = null,
) {
    DmSurfaceCard(
        modifier = modifier.fillMaxWidth().heightIn(min = Dimens.documentCardMinHeight),
        contentPadding = PaddingValues(Dimens.space12),
        verticalArrangement = Arrangement.Center,
        onClick = onClick,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DocumentCover(title = coverTitle)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.space7),
            ) {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = metadata, style = DocuMindTheme.typography.meta, color = MaterialTheme.colorScheme.onSurfaceVariant)
                activity?.let {
                    Text(text = it, style = DocuMindTheme.typography.meta, color = DocuMindTheme.colors.success)
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.iconL),
            )
        }
    }
}

/** Card linking an answer to the page of the document it cites. */
@Composable
fun CitationCard(
    citationNumber: Int,
    documentTitle: String,
    location: String,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    DmSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(Dimens.space14),
        verticalArrangement = Arrangement.spacedBy(Dimens.space10),
        onClick = onClick,
        onClickLabel = actionLabel,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_file_text), contentDescription = null, tint = accent, modifier = Modifier.size(Dimens.iconM))
            Spacer(Modifier.width(Dimens.space7))
            Text(
                text = stringResource(R.string.citation_marker, citationNumber) + "  " + documentTitle,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(painterResource(R.drawable.ic_arrow_up_right), contentDescription = null, tint = accent, modifier = Modifier.size(Dimens.iconL))
        }
        Text(text = location, style = DocuMindTheme.typography.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = actionLabel, style = MaterialTheme.typography.labelSmall, color = accent)
    }
}

/** Paragraph of an assistant answer: optional bold lead, body and a violet [n] citation marker. */
@Composable
fun CitedParagraph(
    body: String,
    modifier: Modifier = Modifier,
    lead: String? = null,
    citationNumber: Int? = null,
) {
    val emphasis = MaterialTheme.typography.titleSmall.fontWeight
    val accent = MaterialTheme.colorScheme.primary
    val marker = citationNumber?.let { stringResource(R.string.citation_marker, it) }
    Text(
        text = buildAnnotatedString {
            lead?.let {
                withStyle(SpanStyle(fontWeight = emphasis)) { append(it) }
                append(" ")
            }
            append(body)
            marker?.let {
                append(" ")
                withStyle(SpanStyle(fontWeight = emphasis, color = accent)) { append(it) }
            }
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

/** List heading with an optional count and a trailing action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The action keeps its natural width; the title and count share the rest and wrap by words.
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false),
            )
            count?.let {
                Spacer(Modifier.width(Dimens.space10))
                Text(
                    text = it.toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.titleSmall.lineHeight),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        action?.invoke()
    }
}

/** Small muted text action with a trailing icon ("Last opened ↓"). */
@Composable
fun DmInlineAction(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = Dimens.minTouchTarget)
            .clip(MaterialTheme.shapes.small)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = Dimens.space8),
        horizontalArrangement = Arrangement.spacedBy(Dimens.space4 + Dimens.hairline),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = DocuMindTheme.typography.caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f, fill = false),
        )
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(Dimens.iconS))
    }
}

@Preview
@Composable
private fun DocumentComponentsPreview() {
    ChatbotTheme {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(Dimens.space16),
        ) {
            DocumentTopBar(title = "Annual Report 2024", subtitle = "24 pages · Ready to chat", onBackClick = {}, onMoreClick = {})
            Column(
                modifier = Modifier.padding(horizontal = Dimens.space24),
                verticalArrangement = Arrangement.spacedBy(Dimens.space12),
            ) {
                SectionHeader(title = "Recent notebooks", count = 3) {
                    DmInlineAction(text = "Last opened", icon = painterResource(R.drawable.ic_arrow_down), onClick = {})
                }
                DocumentListItem(
                    title = "Annual Report 2024",
                    coverTitle = "N / 24",
                    metadata = "24 sources · 2.4 MB · Opened just now",
                    activity = "Continue your conversation",
                    onClick = {},
                )
                CitedParagraph(lead = "Enterprise customers led growth.", body = "They contributed 62% of revenue.", citationNumber = 1)
                CitationCard(
                    citationNumber = 1,
                    documentTitle = "Annual Report 2024",
                    location = "Page 12 · Financial performance",
                    actionLabel = "View highlighted passage",
                    onClick = {},
                )
            }
            DocumentTabBar(
                tabs = listOf(
                    DocumentTabItem("Chat", painterResource(R.drawable.ic_message_circle)),
                    DocumentTabItem("Document", painterResource(R.drawable.ic_file_text)),
                    DocumentTabItem("Lab", painterResource(R.drawable.ic_flask_round)),
                ),
                selectedIndex = 0,
                onTabSelected = {},
            )
        }
    }
}
