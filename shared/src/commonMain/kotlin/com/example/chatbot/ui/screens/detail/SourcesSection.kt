package com.example.chatbot.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.example.chatbot.domain.model.Notebook
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.action_retry
import com.example.chatbot.resources.detail_add_source
import com.example.chatbot.resources.detail_load_error
import com.example.chatbot.resources.detail_source_limit
import com.example.chatbot.resources.meta_separator
import com.example.chatbot.resources.sources_count
import com.example.chatbot.ui.components.DmErrorText
import com.example.chatbot.ui.components.DmLoadErrorCard
import com.example.chatbot.ui.components.DmPrimaryButton
import com.example.chatbot.ui.components.DmSurfaceCard
import com.example.chatbot.ui.components.UploadProgressCard
import com.example.chatbot.ui.screens.notebooks.UploadUiState
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.theme.Dimens
import com.example.chatbot.ui.theme.DocuMindTheme
import com.example.chatbot.ui.utils.formatFileSize
import com.example.chatbot.ui.utils.message
import com.example.chatbot.ui.utils.uploadStatusText
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Top of the Document tab: the notebook's sources and "Add source" (SPEC RF-02, RF-04).
 * Built from the design system components; the Figma file has no design for it.
 */
@Composable
fun SourcesSection(
    uiState: NotebookDetailUiState,
    onAddSource: () -> Unit,
    onRetryLoad: () -> Unit,
    onRetryUpload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimens.space10)) {
        when (val state = uiState.notebook) {
            NotebookDetailState.Loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            is NotebookDetailState.Error -> DmLoadErrorCard(
                message = stringResource(Res.string.detail_load_error),
                onRetry = onRetryLoad,
            )
            is NotebookDetailState.Loaded -> SourcesCard(
                notebook = state.notebook,
                limitReached = uiState.limitReached,
                canAddSource = uiState.canAddSource,
                uploading = uiState.upload?.isRunning == true,
                onAddSource = onAddSource,
            )
        }
        uiState.pickError?.let { DmErrorText(it.message()) }
        uiState.upload?.let { upload ->
            UploadProgressCard(
                fileName = upload.fileName,
                statusText = uploadStatusText(upload.progress),
                progress = upload.progress,
                errorMessage = upload.error?.message(),
                retryLabel = stringResource(Res.string.action_retry),
                onRetry = onRetryUpload,
            )
        }
    }
}

@Composable
private fun SourcesCard(
    notebook: Notebook,
    limitReached: Boolean,
    canAddSource: Boolean,
    uploading: Boolean,
    onAddSource: () -> Unit,
) {
    DmSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.space12),
    ) {
        Text(
            text = pluralStringResource(Res.plurals.sources_count, notebook.sourceCount, notebook.sourceCount) +
                stringResource(Res.string.meta_separator) + formatFileSize(notebook.totalSizeBytes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (limitReached) {
            Text(
                text = stringResource(Res.string.detail_source_limit),
                style = DocuMindTheme.typography.meta,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        } else {
            DmPrimaryButton(
                text = stringResource(Res.string.detail_add_source),
                onClick = onAddSource,
                enabled = canAddSource,
                loading = uploading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun SourcesSectionPreview() {
    ChatbotTheme {
        SourcesSection(
            uiState = NotebookDetailUiState(
                title = "Annual Report 2024",
                notebook = NotebookDetailState.Loaded(Notebook("1", "Annual Report 2024", 0, 2, 3_355_443)),
                upload = UploadUiState(fileName = "Appendix.pdf", progress = 0.3f),
            ),
            onAddSource = {},
            onRetryLoad = {},
            onRetryUpload = {},
        )
    }
}
