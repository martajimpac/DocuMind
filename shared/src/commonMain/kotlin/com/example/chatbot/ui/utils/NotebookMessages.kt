package com.example.chatbot.ui.utils

import androidx.compose.runtime.Composable
import com.example.chatbot.domain.model.NotebookError
import com.example.chatbot.domain.validation.PdfError
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.notebook_error_invalid_file
import com.example.chatbot.resources.notebook_error_limit
import com.example.chatbot.resources.notebook_error_network
import com.example.chatbot.resources.notebook_error_unknown
import com.example.chatbot.resources.pdf_error_empty
import com.example.chatbot.resources.pdf_error_not_pdf
import com.example.chatbot.resources.pdf_error_read_failed
import com.example.chatbot.resources.pdf_error_too_large
import com.example.chatbot.resources.upload_status_progress
import com.example.chatbot.ui.screens.notebooks.PickError
import org.jetbrains.compose.resources.stringResource

/** User-facing text for notebook errors, shared by the library and the notebook detail. */
@Composable
fun NotebookError.message(): String = stringResource(
    when (this) {
        NotebookError.Network -> Res.string.notebook_error_network
        NotebookError.LimitReached -> Res.string.notebook_error_limit
        NotebookError.InvalidFile -> Res.string.notebook_error_invalid_file
        NotebookError.Unknown -> Res.string.notebook_error_unknown
    },
)

@Composable
fun PickError.message(): String = stringResource(
    when (this) {
        PickError.ReadFailed -> Res.string.pdf_error_read_failed
        is PickError.Invalid -> when (error) {
            PdfError.Empty -> Res.string.pdf_error_empty
            PdfError.TooLarge -> Res.string.pdf_error_too_large
            PdfError.NotPdf -> Res.string.pdf_error_not_pdf
        }
    },
)

@Composable
fun uploadStatusText(progress: Float): String =
    stringResource(Res.string.upload_status_progress, formatPercent(progress.coerceIn(0f, 1f).toDouble()))
