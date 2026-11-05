package com.example.chatbot.ui.screens.notebooks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.domain.model.Notebook
import com.example.chatbot.domain.model.NotebookError
import com.example.chatbot.domain.model.NotebookResult
import com.example.chatbot.domain.model.PickedPdf
import com.example.chatbot.domain.model.UploadStatus
import com.example.chatbot.domain.repository.NotebookRepository
import com.example.chatbot.domain.validation.PdfError
import com.example.chatbot.domain.validation.PdfValidator
import com.example.chatbot.ui.files.PdfPickResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface NotebookListState {
    data object Loading : NotebookListState
    data object Empty : NotebookListState
    data class Content(val notebooks: List<Notebook>) : NotebookListState
    data class Error(val error: NotebookError) : NotebookListState
}

sealed interface PickError {
    data class Invalid(val error: PdfError) : PickError
    data object ReadFailed : PickError
}

data class UploadUiState(val fileName: String, val progress: Float, val error: NotebookError? = null) {
    val isRunning: Boolean get() = error == null
}

data class NotebooksUiState(
    val list: NotebookListState = NotebookListState.Loading,
    val upload: UploadUiState? = null,
    val pickError: PickError? = null,
    val showCancelDialog: Boolean = false,
) {
    val canStartUpload: Boolean get() = upload?.isRunning != true
    val interceptBack: Boolean get() = upload?.isRunning == true
}

sealed interface NotebooksEvent {
    data class OpenNotebook(val id: String, val title: String) : NotebooksEvent
    data object Leave : NotebooksEvent
}

/**
 * Library screen: the user's notebooks (SPEC RF-05, RF-06) and creating a notebook from a PDF
 * (RF-01, RF-04). The upload runs in [viewModelScope], so it survives rotation and going to the
 * background while the process lives; it is lost if the process dies (SPEC).
 */
class NotebooksViewModel(
    private val repository: NotebookRepository,
    private val validator: PdfValidator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotebooksUiState())
    val uiState: StateFlow<NotebooksUiState> = _uiState.asStateFlow()

    private val _events = Channel<NotebooksEvent>(Channel.BUFFERED)
    val events: Flow<NotebooksEvent> = _events.receiveAsFlow()

    private var loadJob: Job? = null
    private var uploadJob: Job? = null

    // Kept in memory only, for "Retry". Too large for SavedStateHandle.
    private var pendingPdf: PickedPdf? = null

    init {
        load()
    }

    /**
     * Reloads in place, for example when coming back from a notebook with new sources.
     * Ignored while a load is already running (such as the first one, started in init).
     */
    fun refresh() {
        if (loadJob?.isActive != true) load()
    }

    fun onRetryLoad() {
        _uiState.update { it.copy(list = NotebookListState.Loading) }
        load()
    }

    fun onPdfPicked(result: PdfPickResult) {
        if (!_uiState.value.canStartUpload) return
        when (result) {
            is PdfPickResult.Rejected -> _uiState.update { it.copy(pickError = PickError.Invalid(result.error)) }
            PdfPickResult.ReadFailed -> _uiState.update { it.copy(pickError = PickError.ReadFailed) }
            is PdfPickResult.Picked -> {
                val error = validator.validateContent(result.pdf.bytes)
                if (error != null) {
                    _uiState.update { it.copy(pickError = PickError.Invalid(error)) }
                } else {
                    startUpload(result.pdf)
                }
            }
        }
    }

    fun onRetryUpload() {
        val pdf = pendingPdf ?: return
        if (_uiState.value.canStartUpload) startUpload(pdf)
    }

    fun onBackPressed() {
        if (_uiState.value.interceptBack) _uiState.update { it.copy(showCancelDialog = true) }
    }

    fun onCancelDialogDismissed() {
        _uiState.update { it.copy(showCancelDialog = false) }
    }

    /** CA-12: the repository removes the partial file when its flow is cancelled. */
    fun onCancelUploadConfirmed() {
        uploadJob?.cancel()
        uploadJob = null
        pendingPdf = null
        _uiState.update { it.copy(upload = null, showCancelDialog = false) }
        viewModelScope.launch { _events.send(NotebooksEvent.Leave) }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val result = repository.getNotebooks()
            _uiState.update { state ->
                val list = when (result) {
                    is NotebookResult.Success ->
                        if (result.value.isEmpty()) NotebookListState.Empty else NotebookListState.Content(result.value)
                    // A failed refresh keeps the list already on screen.
                    is NotebookResult.Failure ->
                        state.list as? NotebookListState.Content ?: NotebookListState.Error(result.error)
                }
                state.copy(list = list)
            }
        }
    }

    private fun startUpload(pdf: PickedPdf) {
        pendingPdf = pdf
        _uiState.update { it.copy(upload = UploadUiState(fileName = pdf.fileName, progress = 0f), pickError = null) }
        uploadJob = viewModelScope.launch {
            repository.createNotebook(pdf).collect { status ->
                when (status) {
                    is UploadStatus.Progress -> _uiState.update { state ->
                        state.copy(upload = state.upload?.copy(progress = status.fraction))
                    }
                    is UploadStatus.Failure -> _uiState.update { state ->
                        state.copy(upload = state.upload?.copy(error = status.error))
                    }
                    is UploadStatus.Success -> {
                        val notebook = status.value
                        pendingPdf = null
                        _uiState.update { state -> state.copy(upload = null, list = state.list.withFirst(notebook)) }
                        _events.send(NotebooksEvent.OpenNotebook(notebook.id, notebook.title))
                    }
                }
            }
        }
    }

    private fun NotebookListState.withFirst(notebook: Notebook): NotebookListState {
        val others = (this as? NotebookListState.Content)?.notebooks.orEmpty().filterNot { it.id == notebook.id }
        return NotebookListState.Content(listOf(notebook) + others)
    }
}
