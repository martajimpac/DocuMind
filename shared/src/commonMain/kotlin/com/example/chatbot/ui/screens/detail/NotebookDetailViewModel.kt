package com.example.chatbot.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.domain.model.Notebook
import com.example.chatbot.domain.model.NotebookError
import com.example.chatbot.domain.model.NotebookResult
import com.example.chatbot.domain.model.PickedPdf
import com.example.chatbot.domain.model.UploadStatus
import com.example.chatbot.domain.repository.NotebookRepository
import com.example.chatbot.domain.validation.PdfValidator
import com.example.chatbot.ui.files.PdfPickResult
import com.example.chatbot.ui.screens.notebooks.PickError
import com.example.chatbot.ui.screens.notebooks.UploadUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface NotebookDetailState {
    data object Loading : NotebookDetailState
    data class Loaded(val notebook: Notebook) : NotebookDetailState
    data class Error(val error: NotebookError) : NotebookDetailState
}

data class NotebookDetailUiState(
    val title: String,
    val notebook: NotebookDetailState = NotebookDetailState.Loading,
    val upload: UploadUiState? = null,
    val pickError: PickError? = null,
    val showCancelDialog: Boolean = false,
) {
    private val loaded: Notebook? get() = (notebook as? NotebookDetailState.Loaded)?.notebook
    val limitReached: Boolean get() = loaded?.canAddSource == false
    val canAddSource: Boolean get() = loaded?.canAddSource == true && upload?.isRunning != true
    val interceptBack: Boolean get() = upload?.isRunning == true
}

sealed interface NotebookDetailEvent {
    data object Leave : NotebookDetailEvent
}

/**
 * Notebook detail: loads the notebook by id and adds PDF sources to it (SPEC RF-02, RF-04).
 * The route arguments arrive through [SavedStateHandle] under their own names.
 */
class NotebookDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: NotebookRepository,
    private val validator: PdfValidator,
) : ViewModel() {

    private val notebookId: String = checkNotNull(savedStateHandle["notebookId"])

    private val _uiState = MutableStateFlow(NotebookDetailUiState(title = savedStateHandle["title"] ?: ""))
    val uiState: StateFlow<NotebookDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<NotebookDetailEvent>(Channel.BUFFERED)
    val events: Flow<NotebookDetailEvent> = _events.receiveAsFlow()

    private var uploadJob: Job? = null

    // Kept in memory only, for "Retry". Too large for SavedStateHandle.
    private var pendingPdf: PickedPdf? = null

    init {
        load()
    }

    fun onRetryLoad() {
        _uiState.update { it.copy(notebook = NotebookDetailState.Loading) }
        load()
    }

    fun onPdfPicked(result: PdfPickResult) {
        if (!_uiState.value.canAddSource) return
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
        if (_uiState.value.canAddSource) startUpload(pdf)
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
        viewModelScope.launch { _events.send(NotebookDetailEvent.Leave) }
    }

    private fun load() {
        viewModelScope.launch { reloadNotebook() }
    }

    private suspend fun reloadNotebook() {
        val state = when (val result = repository.getNotebook(notebookId)) {
            is NotebookResult.Success -> NotebookDetailState.Loaded(result.value)
            is NotebookResult.Failure -> NotebookDetailState.Error(result.error)
        }
        _uiState.update { it.copy(notebook = state) }
    }

    private fun startUpload(pdf: PickedPdf) {
        pendingPdf = pdf
        _uiState.update { it.copy(upload = UploadUiState(fileName = pdf.fileName, progress = 0f), pickError = null) }
        uploadJob = viewModelScope.launch {
            repository.addDocument(notebookId, pdf).collect { status ->
                when (status) {
                    is UploadStatus.Progress -> _uiState.update { state ->
                        state.copy(upload = state.upload?.copy(progress = status.fraction))
                    }
                    is UploadStatus.Failure -> {
                        _uiState.update { state -> state.copy(upload = state.upload?.copy(error = status.error)) }
                        // The server is the source of truth for the limit: show it as reached.
                        if (status.error == NotebookError.LimitReached) reloadNotebook()
                    }
                    is UploadStatus.Success -> {
                        pendingPdf = null
                        _uiState.update { it.copy(upload = null, notebook = NotebookDetailState.Loaded(status.value)) }
                    }
                }
            }
        }
    }
}
