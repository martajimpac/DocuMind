package com.example.chatbot.data.remote

import com.example.chatbot.domain.model.Notebook
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Columns selected for the notebook list. The sources are embedded by PostgREST. */
const val NOTEBOOK_COLUMNS = "id,title,created_at,documents(size_bytes)"

@Serializable
data class NotebookRow(
    val id: String,
    val title: String,
    @SerialName("created_at") val createdAt: String,
    val documents: List<DocumentSizeRow> = emptyList(),
)

@Serializable
data class DocumentSizeRow(@SerialName("size_bytes") val sizeBytes: Long)

@OptIn(ExperimentalTime::class)
fun NotebookRow.toNotebook(): Notebook = Notebook(
    id = id,
    title = title,
    createdAtEpochMillis = Instant.parse(createdAt).toEpochMilliseconds(),
    sourceCount = documents.size,
    totalSizeBytes = documents.sumOf { it.sizeBytes },
)

private val json = Json { ignoreUnknownKeys = true }

fun decodeNotebookRows(body: String): List<NotebookRow> = json.decodeFromString(body)
