package com.example.chatbot.data.remote

import com.example.chatbot.domain.model.Notebook
import kotlin.test.Test
import kotlin.test.assertEquals

/** RF-05 / CA-06: count and total size come from the embedded sources. */
class NotebookDtosTest {

    @Test
    fun `row maps to notebook with source count and total size`() {
        val row = NotebookRow(
            id = "nb-1",
            title = "Annual report",
            createdAt = "2026-11-04T22:21:03.952031+00:00",
            documents = listOf(DocumentSizeRow(1_000), DocumentSizeRow(2_500)),
        )

        assertEquals(
            Notebook(
                id = "nb-1",
                title = "Annual report",
                createdAtEpochMillis = 1_793_830_863_952,
                sourceCount = 2,
                totalSizeBytes = 3_500,
            ),
            row.toNotebook(),
        )
    }

    @Test
    fun `row decodes from the PostgREST response`() {
        val json = """[{"id":"nb-2","title":"T","created_at":"2026-11-04T00:00:00+00:00","documents":[{"size_bytes":42}]}]"""

        val notebooks = decodeNotebookRows(json).map { it.toNotebook() }

        assertEquals(listOf(Notebook("nb-2", "T", 1_793_750_400_000, 1, 42)), notebooks)
    }
}
