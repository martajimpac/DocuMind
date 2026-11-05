package com.example.chatbot.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.action_retry
import com.example.chatbot.resources.upload_cancel_body
import com.example.chatbot.resources.upload_cancel_confirm
import com.example.chatbot.resources.upload_cancel_dismiss
import com.example.chatbot.resources.upload_cancel_title
import com.example.chatbot.ui.theme.Dimens
import org.jetbrains.compose.resources.stringResource

/** Load failure with a retry action. Used by the library and the notebook detail. */
@Composable
fun DmLoadErrorCard(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    DmSurfaceCard(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .weight(1f)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
            DmTextButton(text = stringResource(Res.string.action_retry), onClick = onRetry)
        }
    }
}

@Composable
fun DmErrorText(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Assertive },
    )
}

/** CA-12: confirm before cancelling an upload in progress. */
@Composable
fun CancelUploadDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(Res.string.upload_cancel_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.upload_cancel_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = { DmTextButton(text = stringResource(Res.string.upload_cancel_confirm), onClick = onConfirm) },
        dismissButton = { DmTextButton(text = stringResource(Res.string.upload_cancel_dismiss), onClick = onDismiss) },
    )
}
