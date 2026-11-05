package com.example.chatbot.testing

import com.example.chatbot.domain.model.Notebook
import com.example.chatbot.domain.model.NotebookResult
import com.example.chatbot.domain.model.PickedPdf
import com.example.chatbot.domain.model.UploadStatus
import com.example.chatbot.domain.repository.NotebookRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun notebook(id: String, sources: Int = 1, size: Long = 1_000, title: String = "Notebook $id") =
    Notebook(id = id, title = title, createdAtEpochMillis = 0, sourceCount = sources, totalSizeBytes = size)

fun pdf(name: String = "Report.pdf") = PickedPdf(name, "%PDF-1.7 test".encodeToByteArray())

/**
 * In-memory NotebookRepository. Uploads emit 50% progress, then wait on [uploadGate] (if set)
 * before sending [nextUploadResult]. Cancelled uploads are counted in [cancelledUploads].
 */
class FakeNotebookRepository : NotebookRepository {

    var notebooks: NotebookResult<List<Notebook>> = NotebookResult.Success(emptyList())
    var notebookById: (String) -> NotebookResult<Notebook> = { NotebookResult.Success(notebook(it)) }
    var nextUploadResult: UploadStatus<Notebook> = UploadStatus.Success(notebook("new"))
    var uploadGate: CompletableDeferred<Unit>? = null
    var listGate: CompletableDeferred<Unit>? = null

    var listCalls = 0
    val createCalls = mutableListOf<PickedPdf>()
    val addCalls = mutableListOf<Pair<String, PickedPdf>>()
    var cancelledUploads = 0

    override suspend fun getNotebooks(): NotebookResult<List<Notebook>> {
        listCalls++
        listGate?.await()
        return notebooks
    }

    override suspend fun getNotebook(id: String): NotebookResult<Notebook> = notebookById(id)

    override fun createNotebook(pdf: PickedPdf): Flow<UploadStatus<Notebook>> {
        createCalls += pdf
        return upload()
    }

    override fun addDocument(notebookId: String, pdf: PickedPdf): Flow<UploadStatus<Notebook>> {
        addCalls += notebookId to pdf
        return upload()
    }

    private fun upload(): Flow<UploadStatus<Notebook>> = flow {
        emit(UploadStatus.Progress(0.5f))
        try {
            uploadGate?.await()
        } catch (e: CancellationException) {
            cancelledUploads++
            throw e
        }
        emit(nextUploadResult)
    }
}
