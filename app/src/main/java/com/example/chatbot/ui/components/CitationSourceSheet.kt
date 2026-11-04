package com.example.chatbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.chatbot.R
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.theme.Dimens
import com.example.chatbot.ui.theme.DocuMindTheme

/**
 * Content of the "Original source" bottom sheet (Figma 19:360).
 * Only shown in previews for now: no action in the app opens it yet (RF-06).
 */
@Composable
fun CitationSourceSheetContent(
    citationNumber: Int,
    documentTitle: String,
    documentMeta: String,
    sectionTitle: String,
    passage: String,
    onClose: () -> Unit,
    onViewOriginalPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = DocuMindTheme.shapes.sheet
    Column(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(shape, DmElevation.High)
            .clip(shape)
            .background(colors.surface),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(Dimens.space24),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(width = Dimens.sheetHandleWidth, height = Dimens.sheetHandleHeight)
                    .clip(DocuMindTheme.shapes.pill)
                    .background(DocuMindTheme.colors.sheetHandle),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.minTouchTarget)
                .padding(start = Dimens.space24, end = Dimens.space12),
            horizontalArrangement = Arrangement.spacedBy(Dimens.space10),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DmIconTile(size = Dimens.citationMarker, style = DmIconTileStyle.Soft) { color ->
                Text(stringResource(R.string.citation_marker, citationNumber), style = MaterialTheme.typography.labelSmall, color = color)
            }
            Text(
                text = stringResource(R.string.sheet_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            DmIconButton(
                icon = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.cd_close),
                onClick = onClose,
                iconSize = Dimens.iconXxl,
            )
        }
        Column(
            modifier = Modifier.padding(start = Dimens.space24, end = Dimens.space24, top = Dimens.space16),
            verticalArrangement = Arrangement.spacedBy(Dimens.space24),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DmIconTile(
                    icon = painterResource(R.drawable.ic_file_text),
                    size = Dimens.documentTile,
                    iconSize = Dimens.iconXxl,
                    style = DmIconTileStyle.Outlined,
                    shape = MaterialTheme.shapes.small,
                )
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.space6)) {
                    Text(documentTitle, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                    Text(documentMeta, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.space12)) {
                Text(sectionTitle, style = MaterialTheme.typography.labelMedium, color = colors.onSurface)
                HighlightedPassage(text = passage)
            }
        }
        DmPrimaryButton(
            text = stringResource(R.string.sheet_view_original),
            onClick = onViewOriginalPage,
            trailingIcon = painterResource(R.drawable.ic_arrow_up_right),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.space24, end = Dimens.space24, top = Dimens.space28, bottom = Dimens.space16),
        )
    }
}

@Preview(widthDp = 412)
@Composable
private fun CitationSourceSheetPreview() {
    ChatbotTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(top = Dimens.space24)) {
            CitationSourceSheetContent(
                citationNumber = 1,
                documentTitle = stringResource(R.string.doc_annual_report),
                documentMeta = stringResource(R.string.sheet_document_meta, 12, 24),
                sectionTitle = stringResource(R.string.page_title),
                passage = stringResource(R.string.page_passage),
                onClose = {},
                onViewOriginalPage = {},
            )
        }
    }
}
