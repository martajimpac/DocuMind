package com.example.chatbot.ui.files

import androidx.compose.runtime.Composable
import com.example.chatbot.domain.model.PickedPdf
import com.example.chatbot.domain.validation.PdfError

sealed interface PdfPickResult {
    class Picked(val pdf: PickedPdf) : PdfPickResult

    /** Rejected from the file metadata, before its bytes were read (for example, too large). */
    data class Rejected(val error: PdfError) : PdfPickResult

    /** The file could not be read from the device. */
    data object ReadFailed : PdfPickResult
}

/**
 * Returns a function that opens the system file picker filtered to PDF. [onResult] is not
 * called when the user cancels the picker. No storage permission is needed.
 */
@Composable
expect fun rememberPdfPicker(onResult: (PdfPickResult) -> Unit): () -> Unit
