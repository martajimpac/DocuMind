package com.example.chatbot.ui.screens.notebooks

import com.example.chatbot.domain.model.NotebookError
import com.example.chatbot.domain.model.NotebookResult
import com.example.chatbot.domain.model.UploadStatus
import com.example.chatbot.domain.validation.PdfError
import com.example.chatbot.domain.validation.PdfValidator
import com.example.chatbot.testing.FakeNotebookRepository
import com.example.chatbot.testing.MainDispatcherTest
import com.example.chatbot.testing.notebook
import com.example.chatbot.testing.pdf
import com.example.chatbot.ui.files.PdfPickResult
import com.example.chatbot.domain.model.PickedPdf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotebooksViewModelTest : MainDispatcherTest() {

    private val repository = FakeNotebookRepository()

    private fun createViewModel() = NotebooksViewModel(repository, PdfValidator())

    // --- List (RF-05, RF-06) ---

    @Test
    fun `shows the notebooks in the order the repository returns them (CA-06)`() = runTest {
        repository.notebooks = NotebookResult.Success(listOf(notebook("c"), notebook("b"), notebook("a")))
        val viewModel = createViewModel()
        assertEquals(NotebookListState.Loading, viewModel.uiState.value.list)

        advanceUntilIdle()

        assertEquals(NotebookListState.Content(listOf(notebook("c"), notebook("b"), notebook("a"))), viewModel.uiState.value.list)
    }

    @Test
    fun `no notebooks shows the empty state (CA-07)`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(NotebookListState.Empty, viewModel.uiState.value.list)
    }

    @Test
    fun `a failed load shows the error and retry loads again (CA-04)`() = runTest {
        repository.notebooks = NotebookResult.Failure(NotebookError.Network)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(NotebookListState.Error(NotebookError.Network), viewModel.uiState.value.list)

        repository.notebooks = NotebookResult.Success(listOf(notebook("a")))
        viewModel.onRetryLoad()
        advanceUntilIdle()

        assertEquals(NotebookListState.Content(listOf(notebook("a"))), viewModel.uiState.value.list)
    }

    @Test
    fun `refreshing keeps the current list visible until the new one arrives`() = runTest {
        repository.notebooks = NotebookResult.Success(listOf(notebook("a")))
        val viewModel = createViewModel()
        advanceUntilIdle()

        repository.listGate = CompletableDeferred()
        repository.notebooks = NotebookResult.Success(listOf(notebook("b"), notebook("a")))
        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(NotebookListState.Content(listOf(notebook("a"))), viewModel.uiState.value.list)

        repository.listGate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(NotebookListState.Content(listOf(notebook("b"), notebook("a"))), viewModel.uiState.value.list)
    }

    // --- Create (RF-01, RF-03, RF-04) ---

    @Test
    fun `a valid pdf shows progress, then opens the new notebook and adds it first (CA-01)`() = runTest {
        repository.notebooks = NotebookResult.Success(listOf(notebook("old")))
        repository.nextUploadResult = UploadStatus.Success(notebook("new", title = "Report"))
        repository.uploadGate = CompletableDeferred()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPdfPicked(PdfPickResult.Picked(pdf("Report.pdf")))
        advanceUntilIdle()
        assertEquals(UploadUiState(fileName = "Report.pdf", progress = 0.5f), viewModel.uiState.value.upload)
        assertFalse(viewModel.uiState.value.canStartUpload)

        repository.uploadGate!!.complete(Unit)
        advanceUntilIdle()

        assertEquals(NotebooksEvent.OpenNotebook("new", "Report"), viewModel.events.first())
        assertNull(viewModel.uiState.value.upload)
        assertTrue(viewModel.uiState.value.canStartUpload)
        assertEquals(
            NotebookListState.Content(listOf(notebook("new", title = "Report"), notebook("old"))),
            viewModel.uiState.value.list,
        )
    }

    @Test
    fun `a file that is not a pdf is rejected without uploading (CA-03)`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPdfPicked(PdfPickResult.Picked(PickedPdf("fake.pdf", "hello".encodeToByteArray())))
        advanceUntilIdle()

        assertEquals(PickError.Invalid(PdfError.NotPdf), viewModel.uiState.value.pickError)
        assertTrue(repository.createCalls.isEmpty())
    }

    @Test
    fun `a file rejected by the picker shows the reason (CA-03)`() = runTest {
        val viewModel = createViewModel()
        viewModel.onPdfPicked(PdfPickResult.Rejected(PdfError.TooLarge))
        assertEquals(PickError.Invalid(PdfError.TooLarge), viewModel.uiState.value.pickError)

        viewModel.onPdfPicked(PdfPickResult.ReadFailed)
        assertEquals(PickError.ReadFailed, viewModel.uiState.value.pickError)
        assertTrue(repository.createCalls.isEmpty())
    }

    @Test
    fun `picking again while an upload runs starts nothing (CA-05)`() = runTest {
        repository.uploadGate = CompletableDeferred()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPdfPicked(PdfPickResult.Picked(pdf("one.pdf")))
        viewModel.onPdfPicked(PdfPickResult.Picked(pdf("two.pdf")))
        advanceUntilIdle()

        assertEquals(listOf("one.pdf"), repository.createCalls.map { it.fileName })
    }

    @Test
    fun `a failed upload shows the error and retry uploads the same file again (CA-04)`() = runTest {
        repository.nextUploadResult = UploadStatus.Failure(NotebookError.Network)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPdfPicked(PdfPickResult.Picked(pdf("Report.pdf")))
        advanceUntilIdle()
        assertEquals(
            UploadUiState(fileName = "Report.pdf", progress = 0.5f, error = NotebookError.Network),
            viewModel.uiState.value.upload,
        )
        assertTrue(viewModel.uiState.value.canStartUpload)

        repository.nextUploadResult = UploadStatus.Success(notebook("new"))
        viewModel.onRetryUpload()
        advanceUntilIdle()

        assertEquals(2, repository.createCalls.size)
        assertEquals(NotebooksEvent.OpenNotebook("new", "Notebook new"), viewModel.events.first())
        assertNull(viewModel.uiState.value.upload)
    }

    // --- Back during an upload (CA-12) ---

    @Test
    fun `back during an upload asks first and No keeps the upload`() = runTest {
        repository.uploadGate = CompletableDeferred()
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onPdfPicked(PdfPickResult.Picked(pdf()))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.interceptBack)
        viewModel.onBackPressed()
        assertTrue(viewModel.uiState.value.showCancelDialog)

        viewModel.onCancelDialogDismissed()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showCancelDialog)
        assertEquals(0, repository.cancelledUploads)

        repository.uploadGate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(NotebooksEvent.OpenNotebook("new", "Notebook new"), viewModel.events.first())
    }

    @Test
    fun `confirming the cancel stops the upload, adds nothing and leaves the screen`() = runTest {
        repository.uploadGate = CompletableDeferred()
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onPdfPicked(PdfPickResult.Picked(pdf()))
        advanceUntilIdle()

        viewModel.onBackPressed()
        viewModel.onCancelUploadConfirmed()
        advanceUntilIdle()

        assertEquals(1, repository.cancelledUploads)
        assertNull(viewModel.uiState.value.upload)
        assertFalse(viewModel.uiState.value.showCancelDialog)
        assertEquals(NotebookListState.Empty, viewModel.uiState.value.list)
        assertEquals(NotebooksEvent.Leave, viewModel.events.first())
    }

    @Test
    fun `back is not intercepted when no upload is running`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.interceptBack)
    }
}
