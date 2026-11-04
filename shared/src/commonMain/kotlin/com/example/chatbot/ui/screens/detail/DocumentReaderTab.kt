package com.example.chatbot.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.cd_next_page
import com.example.chatbot.resources.cd_page_of
import com.example.chatbot.resources.cd_previous_page
import com.example.chatbot.resources.cd_zoom
import com.example.chatbot.resources.ic_chevron_left
import com.example.chatbot.resources.ic_chevron_right
import com.example.chatbot.resources.ic_zoom_in
import com.example.chatbot.resources.page_brand
import com.example.chatbot.resources.page_footer
import com.example.chatbot.resources.page_intro
import com.example.chatbot.resources.page_passage
import com.example.chatbot.resources.page_publication
import com.example.chatbot.resources.page_revenue_heading
import com.example.chatbot.resources.page_section
import com.example.chatbot.resources.page_table_enterprise
import com.example.chatbot.resources.page_table_other
import com.example.chatbot.resources.page_table_title
import com.example.chatbot.resources.page_title
import com.example.chatbot.resources.reader_page_total
import com.example.chatbot.resources.reader_zoom_level
import com.example.chatbot.ui.components.DmEdge
import com.example.chatbot.ui.components.DmElevation
import com.example.chatbot.ui.components.DmIconButton
import com.example.chatbot.ui.components.DmSurfaceCard
import com.example.chatbot.ui.components.HighlightedPassage
import com.example.chatbot.ui.components.hairlineEdge
import com.example.chatbot.ui.utils.formatPercent
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.theme.Dimens
import com.example.chatbot.ui.theme.DocuMindTheme

// Sample page from the design: page 12 of 24, revenue mix 2023 vs 2024.
private const val SAMPLE_PAGE = 12
private const val SAMPLE_PAGE_COUNT = 24
private const val PREVIOUS_YEAR = 2023
private const val CURRENT_YEAR = 2024

private data class RevenueRow(val label: StringResource, val previous: Double, val current: Double)

private val revenueRows = listOf(
    RevenueRow(Res.string.page_table_enterprise, previous = 0.54, current = 0.62),
    RevenueRow(Res.string.page_table_other, previous = 0.46, current = 0.38),
)

/** Document tab: sample PDF page and reader controls. Paging and zoom are not functional yet. */
@Composable
fun DocumentReaderTab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.space16),
        verticalArrangement = Arrangement.spacedBy(Dimens.space10),
    ) {
        SamplePdfPage()
        ReaderControls()
    }
}

@Composable
private fun SamplePdfPage(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val type = DocuMindTheme.typography
    DmSurfaceCard(
        modifier = modifier.fillMaxWidth().heightIn(min = Dimens.pdfPageMinHeight),
        contentPadding = PaddingValues(
            start = Dimens.space28,
            end = Dimens.space28,
            top = Dimens.space25,
            bottom = Dimens.space20,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.space17),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.page_brand), style = type.pageBrand, color = colors.primary, modifier = Modifier.weight(1f))
            Text(stringResource(Res.string.page_publication), style = type.pageCaption, color = colors.onSurfaceVariant)
        }
        Box(Modifier.fillMaxWidth().height(Dimens.hairline).background(colors.outline))
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.space9)) {
            Text(stringResource(Res.string.page_section), style = type.pageEyebrow, color = colors.onSurfaceVariant)
            Text(stringResource(Res.string.page_title), style = type.pageTitle, color = colors.onSurface)
            Text(stringResource(Res.string.page_intro), style = type.pageBody, color = colors.onSurface)
        }
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.space9)) {
            Text(stringResource(Res.string.page_revenue_heading), style = type.pageHeading, color = colors.onSurface)
            HighlightedPassage(
                text = stringResource(Res.string.page_passage),
                style = type.pagePassage,
                contentPadding = PaddingValues(Dimens.space10),
            )
        }
        RevenueTable()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.page_footer), style = type.pageCaption, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(SAMPLE_PAGE.toString(), style = type.pageTableStrong, color = colors.onSurface)
        }
    }
}

@Composable
private fun RevenueTable() {
    val type = DocuMindTheme.typography
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier = Modifier.fillMaxWidth()) {
        TableRow(
            label = stringResource(Res.string.page_table_title),
            previous = PREVIOUS_YEAR.toString(),
            current = CURRENT_YEAR.toString(),
            labelStyle = type.pageTableStrong,
            previousColor = muted,
            minHeight = Dimens.tableHeaderHeight,
        )
        revenueRows.forEach { row ->
            TableRow(
                label = stringResource(row.label),
                previous = formatPercent(row.previous),
                current = formatPercent(row.current),
                labelStyle = type.pageTable,
                previousColor = MaterialTheme.colorScheme.onSurface,
                minHeight = Dimens.tableRowHeight,
            )
        }
    }
}

@Composable
private fun TableRow(
    label: String,
    previous: String,
    current: String,
    labelStyle: TextStyle,
    previousColor: Color,
    minHeight: Dp,
) {
    val type = DocuMindTheme.typography
    val ink = MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .hairlineEdge(DmEdge.Bottom),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = labelStyle, color = ink, modifier = Modifier.weight(1f))
        Text(previous, style = type.pageTable, color = previousColor, textAlign = TextAlign.End, modifier = Modifier.width(Dimens.tableColumnWidth))
        Text(current, style = type.pageTableStrong, color = ink, textAlign = TextAlign.End, modifier = Modifier.width(Dimens.tableColumnWidth))
    }
}

@Composable
private fun ReaderControls(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val pageOf = stringResource(Res.string.cd_page_of, SAMPLE_PAGE, SAMPLE_PAGE_COUNT)
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = Dimens.minTouchTarget),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DmIconButton(
            icon = painterResource(Res.drawable.ic_chevron_left),
            contentDescription = stringResource(Res.string.cd_previous_page),
            onClick = {},
            iconSize = Dimens.iconXxl,
        )
        // Page selector (not functional yet).
        DmSurfaceCard(
            modifier = Modifier
                .heightIn(min = Dimens.minTouchTarget)
                .semantics { contentDescription = pageOf },
            shape = MaterialTheme.shapes.small,
            elevation = DmElevation.None,
            contentPadding = PaddingValues(horizontal = Dimens.space14 + Dimens.hairline),
            verticalArrangement = Arrangement.Center,
            onClick = {},
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = MaterialTheme.typography.labelSmall.fontWeight)) { append(SAMPLE_PAGE.toString()) }
                    withStyle(SpanStyle(color = colors.onSurfaceVariant)) {
                        append(stringResource(Res.string.reader_page_total, SAMPLE_PAGE_COUNT))
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface,
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
        DmIconButton(
            icon = painterResource(Res.drawable.ic_chevron_right),
            contentDescription = stringResource(Res.string.cd_next_page),
            onClick = {},
            iconSize = Dimens.iconXxl,
        )
        Box(Modifier.size(width = Dimens.hairline, height = Dimens.controlDividerHeight).background(colors.outline))
        Row(
            modifier = Modifier
                .widthIn(min = Dimens.zoomControlWidth)
                .heightIn(min = Dimens.minTouchTarget)
                .clip(MaterialTheme.shapes.small)
                .clickable(role = Role.Button, onClickLabel = stringResource(Res.string.cd_zoom), onClick = {}),
            horizontalArrangement = Arrangement.spacedBy(Dimens.space6, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(Res.drawable.ic_zoom_in), contentDescription = null, tint = colors.onSurface, modifier = Modifier.size(Dimens.iconXl))
            Text(stringResource(Res.string.reader_zoom_level), style = DocuMindTheme.typography.meta, color = colors.onSurfaceVariant)
        }
    }
}

@Preview
@Composable
private fun DocumentReaderTabPreview() {
    ChatbotTheme {
        DocumentReaderTab(modifier = Modifier.background(MaterialTheme.colorScheme.background))
    }
}
