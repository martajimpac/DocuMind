package com.example.chatbot.domain.repository

import com.example.chatbot.domain.model.Notebook
import com.example.chatbot.domain.model.NotebookResult
import com.example.chatbot.domain.model.PickedPdf
import com.example.chatbot.domain.model.UploadStatus
import kotlinx.coroutines.flow.Flow

interface NotebookRepository {

    /** The user's notebooks, newest first. */
    suspend fun getNotebooks(): NotebookResult<List<Notebook>>

    suspend fun getNotebook(id: String): NotebookResult<Notebook>

    /**
     * Uploads [pdf] and creates a notebook with it as the first source. If the upload fails or
     * the collector is cancelled, nothing is left behind: no notebook and no stored file.
     */
    fun createNotebook(pdf: PickedPdf): Flow<UploadStatus<Notebook>>

    /** Same guarantees as [createNotebook]. Emits the updated notebook. */
    fun addDocument(notebookId: String, pdf: PickedPdf): Flow<UploadStatus<Notebook>>
}
