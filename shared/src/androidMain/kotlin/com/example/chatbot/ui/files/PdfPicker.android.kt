package com.example.chatbot.ui.files

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.example.chatbot.domain.model.PickedPdf
import com.example.chatbot.domain.validation.PdfValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

private const val PDF_MIME_TYPE = "application/pdf"

@Composable
actual fun rememberPdfPicker(onResult: (PdfPickResult) -> Unit): () -> Unit {
    val resolver = LocalContext.current.contentResolver
    val scope = rememberCoroutineScope()
    val latestOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch { latestOnResult(readPdf(resolver, uri)) }
    }
    return remember(launcher) { { launcher.launch(arrayOf(PDF_MIME_TYPE)) } }
}

private suspend fun readPdf(resolver: ContentResolver, uri: Uri): PdfPickResult = withContext(Dispatchers.IO) {
    try {
        var name: String? = null
        var size: Long? = null
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) name = cursor.getString(0)
                if (!cursor.isNull(1)) size = cursor.getLong(1)
            }
        }
        // Reject by size before loading the file into memory.
        size?.let { known -> PdfValidator().validateSize(known)?.let { return@withContext PdfPickResult.Rejected(it) } }

        val bytes = resolver.openInputStream(uri)?.use { it.readAtMost(PdfValidator.MAX_SIZE_BYTES + 1) }
            ?: return@withContext PdfPickResult.ReadFailed
        PdfPickResult.Picked(PickedPdf(fileName = name ?: uri.lastPathSegment.orEmpty(), bytes = bytes))
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        PdfPickResult.ReadFailed
    }
}

/** Reads up to [limit] bytes, so a provider that reports no size cannot exhaust memory. */
private fun InputStream.readAtMost(limit: Long): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0L
    while (total < limit) {
        val read = read(buffer, 0, minOf(buffer.size.toLong(), limit - total).toInt())
        if (read == -1) break
        output.write(buffer, 0, read)
        total += read
    }
    return output.toByteArray()
}
