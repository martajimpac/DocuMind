package com.example.chatbot.ui.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.example.chatbot.domain.model.PickedPdf
import com.example.chatbot.domain.validation.PdfValidator
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypePDF
import platform.darwin.NSObject
import platform.posix.memcpy

// Not compiled on Windows (PLAN "Limitaciones del entorno"): needs a Mac to build and test.
@Composable
actual fun rememberPdfPicker(onResult: (PdfPickResult) -> Unit): () -> Unit {
    val latestOnResult by rememberUpdatedState(onResult)
    // Kept in composition: UIKit holds the delegate weakly.
    val delegate = remember { PdfPickerDelegate { latestOnResult(it) } }
    return remember(delegate) {
        {
            val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypePDF), asCopy = true)
            picker.delegate = delegate
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private class PdfPickerDelegate(
    private val onResult: (PdfPickResult) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        val data = NSData.dataWithContentsOfURL(url)
        if (data == null) {
            onResult(PdfPickResult.ReadFailed)
            return
        }
        PdfValidator().validateSize(data.length.toLong())?.let {
            onResult(PdfPickResult.Rejected(it))
            return
        }
        onResult(PdfPickResult.Picked(PickedPdf(fileName = url.lastPathComponent.orEmpty(), bytes = data.toByteArray())))
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val result = ByteArray(length.toInt())
    if (result.isNotEmpty()) result.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return result
}
