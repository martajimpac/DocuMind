package com.example.chatbot.domain.validation

enum class PdfError { Empty, TooLarge, NotPdf }

/**
 * Local checks for a picked file (SPEC RF-03). The bucket also enforces size and type on the
 * server; the "%PDF-" header is only checked here.
 */
class PdfValidator {

    /** Checked from the file metadata, before reading the bytes into memory. */
    fun validateSize(sizeBytes: Long): PdfError? = when {
        sizeBytes <= 0 -> PdfError.Empty
        sizeBytes > MAX_SIZE_BYTES -> PdfError.TooLarge
        else -> null
    }

    fun validateContent(bytes: ByteArray): PdfError? =
        validateSize(bytes.size.toLong()) ?: if (bytes.startsWith(PDF_HEADER)) null else PdfError.NotPdf

    /** SPEC RF-01: the notebook is named after the file, without ".pdf". */
    fun notebookTitle(fileName: String): String {
        val trimmed = fileName.trim()
        val withoutExtension = if (trimmed.length > PDF_EXTENSION.length && trimmed.endsWith(PDF_EXTENSION, ignoreCase = true)) {
            trimmed.dropLast(PDF_EXTENSION.length).trim()
        } else {
            trimmed
        }
        return withoutExtension.ifEmpty { FALLBACK_TITLE }.take(MAX_TITLE_LENGTH)
    }

    fun storedFileName(fileName: String): String =
        fileName.trim().ifEmpty { FALLBACK_FILE_NAME }.take(MAX_FILE_NAME_LENGTH)

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

    companion object {
        const val MAX_SIZE_BYTES: Long = 20L * 1024 * 1024

        // Same limits as the database checks on notebooks.title and documents.file_name.
        const val MAX_TITLE_LENGTH = 200
        const val MAX_FILE_NAME_LENGTH = 255

        const val FALLBACK_TITLE = "Untitled"
        const val FALLBACK_FILE_NAME = "document.pdf"

        private const val PDF_EXTENSION = ".pdf"
        private val PDF_HEADER = "%PDF-".encodeToByteArray()
    }
}
