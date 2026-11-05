package com.example.chatbot.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.theme.Dimens
import com.example.chatbot.ui.theme.DocuMindTheme

/**
 * State of a file upload, shared by the library and the notebook detail (SPEC RF-04).
 * Shows a progress bar while [errorMessage] is null; otherwise the error and a retry action.
 * The status line is a live region so screen readers announce changes.
 */
@Composable
fun UploadProgressCard(
    fileName: String,
    statusText: String,
    progress: Float,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null,
) {
    DmSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.space10),
    ) {
        Text(
            text = fileName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (errorMessage == null) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = statusText,
                style = DocuMindTheme.typography.meta,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.space8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { liveRegion = LiveRegionMode.Assertive },
                )
                if (retryLabel != null && onRetry != null) {
                    DmTextButton(text = retryLabel, onClick = onRetry)
                }
            }
        }
    }
}

@Preview
@Composable
private fun UploadProgressCardPreview() {
    ChatbotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.space12)) {
            UploadProgressCard(fileName = "Annual report 2024.pdf", statusText = "Uploading… 62%", progress = 0.62f)
            UploadProgressCard(
                fileName = "Annual report 2024.pdf",
                statusText = "",
                progress = 0f,
                errorMessage = "No connection. Check your network and try again.",
                retryLabel = "Retry",
                onRetry = {},
            )
        }
    }
}
