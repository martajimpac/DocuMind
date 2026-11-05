package com.example.chatbot.domain.model

/** A notebook of the signed-in user with a summary of its PDF sources. */
data class Notebook(
    val id: String,
    val title: String,
    val createdAtEpochMillis: Long,
    val sourceCount: Int,
    val totalSizeBytes: Long,
) {
    val canAddSource: Boolean get() = sourceCount < MAX_SOURCES_PER_NOTEBOOK

    companion object {
        /** SPEC RF-02. The database enforces the same limit. */
        const val MAX_SOURCES_PER_NOTEBOOK = 20
    }
}

/** A file chosen by the user. The bytes are only read once the size has been checked. */
class PickedPdf(val fileName: String, val bytes: ByteArray)

enum class NotebookError {
    Network,

    /** The notebook already has the maximum number of sources. */
    LimitReached,

    /** The server rejected the file (type or size). */
    InvalidFile,
    Unknown,
}

sealed interface NotebookResult<out T> {
    data class Success<T>(val value: T) : NotebookResult<T>
    data class Failure(val error: NotebookError) : NotebookResult<Nothing>
}

/** Progress of an upload. The flow ends after [Success] or [Failure]. */
sealed interface UploadStatus<out T> {
    /** Sent fraction of the file, from 0 to 1. */
    data class Progress(val fraction: Float) : UploadStatus<Nothing>
    data class Success<T>(val value: T) : UploadStatus<T>
    data class Failure(val error: NotebookError) : UploadStatus<Nothing>
}
