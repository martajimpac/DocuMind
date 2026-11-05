package com.example.chatbot.data.repository

import com.example.chatbot.data.remote.NOTEBOOK_COLUMNS
import com.example.chatbot.data.remote.decodeNotebookRows
import com.example.chatbot.data.remote.toNotebook
import com.example.chatbot.domain.model.Notebook
import com.example.chatbot.domain.model.NotebookError
import com.example.chatbot.domain.model.NotebookResult
import com.example.chatbot.domain.model.PickedPdf
import com.example.chatbot.domain.model.UploadStatus
import com.example.chatbot.domain.repository.NotebookRepository
import com.example.chatbot.domain.validation.PdfValidator
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.uploadAsFlow
import io.ktor.http.ContentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import io.github.jan.supabase.storage.UploadStatus as StorageUploadStatus

private const val BUCKET = "documents"
private const val SOURCE_LIMIT_ERROR = "source_limit_reached"

@OptIn(ExperimentalUuidApi::class)
private fun randomId(): String = Uuid.random().toString()

/**
 * Notebooks in Postgres and PDFs in Storage (PLAN "Solución propuesta"). The file goes up first;
 * then one SQL function creates the rows in a single transaction, so a failure never leaves a
 * notebook without sources or a source without a file.
 */
class SupabaseNotebookRepository(
    private val postgrest: Postgrest,
    private val storage: Storage,
    private val auth: Auth,
    private val validator: PdfValidator,
    private val newId: () -> String = ::randomId,
) : NotebookRepository {

    override suspend fun getNotebooks(): NotebookResult<List<Notebook>> = runQuery {
        val result = postgrest.from("notebooks").select(Columns.raw(NOTEBOOK_COLUMNS)) {
            order("created_at", Order.DESCENDING)
        }
        decodeNotebookRows(result.data).map { it.toNotebook() }
    }

    override suspend fun getNotebook(id: String): NotebookResult<Notebook> = runQuery { fetchNotebook(id) }

    override fun createNotebook(pdf: PickedPdf): Flow<UploadStatus<Notebook>> {
        val notebookId = newId()
        return uploadAndRegister(notebookId, pdf) { documentId ->
            postgrest.rpc(
                "create_notebook_with_document",
                buildJsonObject {
                    put("p_notebook_id", notebookId)
                    put("p_title", validator.notebookTitle(pdf.fileName))
                    put("p_document_id", documentId)
                    put("p_file_name", validator.storedFileName(pdf.fileName))
                },
            )
        }
    }

    override fun addDocument(notebookId: String, pdf: PickedPdf): Flow<UploadStatus<Notebook>> =
        uploadAndRegister(notebookId, pdf) { documentId ->
            postgrest.rpc(
                "add_document",
                buildJsonObject {
                    put("p_notebook_id", notebookId)
                    put("p_document_id", documentId)
                    put("p_file_name", validator.storedFileName(pdf.fileName))
                },
            )
        }

    private fun uploadAndRegister(
        notebookId: String,
        pdf: PickedPdf,
        register: suspend (documentId: String) -> Unit,
    ): Flow<UploadStatus<Notebook>> = channelFlow {
        val userId = auth.currentUserOrNull()?.id
        if (userId == null) {
            send(UploadStatus.Failure(NotebookError.Unknown))
            return@channelFlow
        }
        val documentId = newId()
        // Same layout the SQL function expects: {user_id}/{notebook_id}/{document_id}.pdf
        val path = "$userId/$notebookId/$documentId.pdf"
        val bucket = storage.from(BUCKET)

        // While true, a failure or cancellation removes the file. It turns false once the
        // registration request is sent: its outcome may be unknown (for example, the response
        // is lost), and a stray file is better than a source whose file was deleted.
        var removeFileOnFailure = true
        try {
            bucket.uploadAsFlow(path, pdf.bytes) {
                contentType = ContentType.Application.Pdf
                upsert = false
            }.collect { status ->
                if (status is StorageUploadStatus.Progress && status.contentLength > 0) {
                    send(UploadStatus.Progress(status.totalBytesSend.toFloat() / status.contentLength))
                }
            }
            send(UploadStatus.Progress(1f))
            removeFileOnFailure = false
            try {
                register(documentId)
            } catch (e: RestException) {
                // The server answered with an error, so no rows were created.
                removeFileOnFailure = true
                throw e
            }
            send(UploadStatus.Success(fetchNotebook(notebookId)))
        } catch (e: CancellationException) {
            if (removeFileOnFailure) removeQuietly(path)
            throw e
        } catch (e: Exception) {
            if (removeFileOnFailure) removeQuietly(path)
            send(UploadStatus.Failure(e.toNotebookError()))
        }
    }

    private suspend fun fetchNotebook(id: String): Notebook {
        val result = postgrest.from("notebooks").select(Columns.raw(NOTEBOOK_COLUMNS)) {
            filter { eq("id", id) }
        }
        return decodeNotebookRows(result.data).single().toNotebook()
    }

    private suspend fun removeQuietly(path: String) {
        withContext(NonCancellable) {
            try {
                storage.from(BUCKET).delete(path)
            } catch (_: Exception) {
                // Best effort: the file may not exist, or the network may be gone. It stays invisible to the user.
            }
        }
    }

    private suspend fun <T> runQuery(block: suspend () -> T): NotebookResult<T> = try {
        NotebookResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        NotebookResult.Failure(e.toNotebookError())
    }

    private fun Exception.toNotebookError(): NotebookError = when (this) {
        is HttpRequestException -> NotebookError.Network
        is PostgrestRestException ->
            if (SOURCE_LIMIT_ERROR in error || SOURCE_LIMIT_ERROR in (message ?: "")) NotebookError.LimitReached
            else NotebookError.Unknown
        // Storage rejects files over the bucket limit or with another type.
        is RestException -> if (statusCode in INVALID_FILE_STATUS) NotebookError.InvalidFile else NotebookError.Unknown
        else -> NotebookError.Unknown
    }

    private companion object {
        val INVALID_FILE_STATUS = setOf(400, 413, 415)
    }
}
