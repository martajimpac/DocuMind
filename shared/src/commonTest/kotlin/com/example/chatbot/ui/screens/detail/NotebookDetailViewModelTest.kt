package com.example.chatbot.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import com.example.chatbot.domain.model.Notebook
import com.example.chatbot.domain.model.NotebookError
import com.example.chatbot.domain.model.NotebookResult
import com.example.chatbot.domain.model.PickedPdf
import com.example.chatbot.domain.model.UploadStatus
import com.example.chatbot.domain.validation.PdfError
import com.example.chatbot.domain.validation.PdfValidator
import com.example.chatbot.testing.FakeNotebookRepository
import com.example.chatbot.testing.MainDispatcherTest
import com.example.chatbot.testing.notebook
import com.example.chatbot.testing.pdf
import com.example.chatbot.ui.files.PdfPickResult
import com.example.chatbot.ui.screens.notebooks.PickError
import com.example.chatbot.ui.screens.notebooks.UploadUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotebookDetailViewModelTest : MainDispatcherTest() {

    private val repository = FakeNotebookRepository()

    private fun createViewModel(id: String = "nb") = NotebookDetailViewModel(
        SavedStateHandle(mapOf("notebookId" to id, "title" to "Report")),
        repository,
        PdfValidator(),
    )

    @Test
    fun `shows the title at once and then loads the notebook by id`() = runTest {
        repository.notebookById = { NotebookResult.Success(notebook(it, sources = 3)) }
        val viewModel = createViewModel("nb-7")
        assertEquals("Report", viewModel.uiState.value.title)
        assertEquals(NotebookDetailState.Loading, viewModel.uiState.value.notebook)

        advanceUntilIdle()

        assertEquals(NotebookDetailState.Loaded(notebook("nb-7", sources = 3)), viewModel.uiState.value.notebook)
        assertTrue(viewModel.uiState.value.canAddSource)
    }

    @Test
    fun `a failed load shows the error and retry loads again`() = runTest {
        repository.notebookById = { NotebookResult.Failure(NotebookError.Network) }
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(NotebookDetailState.Error(NotebookError.Network), viewModel.uiState.value.notebook)
        assertFalse(viewModel.uiState.value.canAddSource)

        repository.notebookById = { NotebookResult.Success(notebook(it)) }
        viewModel.onRetryLoad()
        advanceUntilIdle()
        assertEquals(NotebookDetailState.Loaded(notebook("nb")), viewModel.uiState.value.notebook)
    }

    @Test
    fun `adding a valid pdf uploads to this notebook and updates the source count (CA-02)`() = runTest {
        repository.notebookById = { NotebookResult.Success(notebook(it, sources = 1, size = 1_000)) }
        repository.nextUploadResult = UploadStatus.Success(notebook("nb", sources = 2, size = 3_000))
        repository.uploadGate = CompletableDeferred()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPdfPicked(PdfPickResult.Picked(pdf("Second.pdf")))
        advanceUntilIdle()
        assertEquals(UploadUiState("Second.pdf", 0.5f), viewModel.uiState.value.upload)
        assertFalse(viewModel.uiState.value.canAddSource)

        repository.uploadGate!!.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf("nb"), repository.addCalls.map { it.first })
        assertNull(viewModel.uiState.value.upload)
        assertEquals(NotebookDetailState.Loaded(notebook("nb", sources = 2, size = 3_000)), viewModel.uiState.value.notebook)
    }

    @Test
    fun `a notebook with 20 sources cannot add more (CA-11)`() = runTest {
        repository.notebookById = { NotebookResult.Success(notebook(it, sources = Notebook.MAX_SOURCES_PER_NOTEBOOK)) }
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canAddSource)
        assertTrue(viewModel.uiState.value.limitReached)

        viewModel.onPdfPicked(PdfPickResult.Picked(pdf()))
        advanceUntilIdle()
        assertTrue(repository.addCalls.isEmpty())
    }

    @Test
    fun `when the server reports the limit the notebook is reloaded (CA-11)`() = runTest {
        var sources = 19
        repository.notebookById = { NotebookResult.Success(notebook(it, sources = sources)) }
        repository.nextUploadResult = UploadStatus.Failure(NotebookError.LimitReached)
        val viewModel = createViewModel()
        advanceUntilIdle()

        sources = 20 // another device added the 20th source meanwhile
        viewModel.onPdfPicked(PdfPickResult.Picked(pdf()))
        advanceUntilIdle()

        assertEquals(NotebookError.LimitReached, viewModel.uiState.value.upload?.error)
        assertTrue(viewModel.uiState.value.limitReached)
    }

    @Test
    fun `an invalid file is rejected without uploading (CA-03)`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPdfPicked(PdfPickResult.Picked(PickedPdf("empty.pdf", ByteArray(0))))
        assertEquals(PickError.Invalid(PdfError.Empty), viewModel.uiState.value.pickError)
        assertTrue(repository.addCalls.isEmpty())
    }

    @Test
    fun `a failed upload can be retried with the same file (CA-04)`() = runTest {
        repository.nextUploadResult = UploadStatus.Failure(NotebookError.Network)
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onPdfPicked(PdfPickResult.Picked(pdf("Second.pdf")))
        advanceUntilIdle()
        assertEquals(NotebookError.Network, viewModel.uiState.value.upload?.error)

        repository.nextUploadResult = UploadStatus.Success(notebook("nb", sources = 2))
        viewModel.onRetryUpload()
        advanceUntilIdle()

        assertEquals(listOf("Second.pdf", "Second.pdf"), repository.addCalls.map { it.second.fileName })
        assertNull(viewModel.uiState.value.upload)
    }

    @Test
    fun `back during an upload asks first, No keeps it and Yes cancels and leaves (CA-12)`() = runTest {
        repository.uploadGate = CompletableDeferred()
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.interceptBack)

        viewModel.onPdfPicked(PdfPickResult.Picked(pdf()))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.interceptBack)

        viewModel.onBackPressed()
        assertTrue(viewModel.uiState.value.showCancelDialog)
        viewModel.onCancelDialogDismissed()
        advanceUntilIdle()
        assertEquals(0, repository.cancelledUploads)

        viewModel.onBackPressed()
        viewModel.onCancelUploadConfirmed()
        advanceUntilIdle()

        assertEquals(1, repository.cancelledUploads)
        assertNull(viewModel.uiState.value.upload)
        assertEquals(NotebookDetailEvent.Leave, viewModel.events.first())
    }
}
