package com.example.chatbot.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** RF-01, RF-03 / CA-03. */
class PdfValidatorTest {

    private val validator = PdfValidator()

    private fun pdfOfSize(size: Int) = ByteArray(size).also { "%PDF-1.7".encodeToByteArray().copyInto(it) }

    @Test
    fun `size zero is empty and over 20 MB is too large`() {
        assertEquals(PdfError.Empty, validator.validateSize(0))
        assertEquals(PdfError.TooLarge, validator.validateSize(PdfValidator.MAX_SIZE_BYTES + 1))
        assertNull(validator.validateSize(PdfValidator.MAX_SIZE_BYTES))
        assertNull(validator.validateSize(1))
    }

    @Test
    fun `content must start with the PDF header`() {
        assertEquals(PdfError.NotPdf, validator.validateContent("hello world".encodeToByteArray()))
        assertEquals(PdfError.NotPdf, validator.validateContent("%PD".encodeToByteArray()))
        assertNull(validator.validateContent(pdfOfSize(1024)))
    }

    @Test
    fun `content checks empty and size before the header`() {
        assertEquals(PdfError.Empty, validator.validateContent(ByteArray(0)))
        assertEquals(PdfError.TooLarge, validator.validateContent(pdfOfSize(PdfValidator.MAX_SIZE_BYTES.toInt() + 1)))
        assertNull(validator.validateContent(pdfOfSize(PdfValidator.MAX_SIZE_BYTES.toInt())))
    }

    @Test
    fun `notebook title is the file name without the pdf extension`() {
        assertEquals("Annual report 2024", validator.notebookTitle("Annual report 2024.pdf"))
        assertEquals("Contract", validator.notebookTitle("Contract.PDF"))
        assertEquals("notes.v2", validator.notebookTitle("  notes.v2.pdf "))
        assertEquals("no-extension", validator.notebookTitle("no-extension"))
    }

    @Test
    fun `notebook title is never blank and fits 200 characters`() {
        assertEquals(".pdf", validator.notebookTitle(".pdf"))
        assertEquals(PdfValidator.FALLBACK_TITLE, validator.notebookTitle("   "))
        assertEquals(200, validator.notebookTitle("a".repeat(300) + ".pdf").length)
    }

    @Test
    fun `stored file name fits 255 characters and is never blank`() {
        assertEquals("report.pdf", validator.storedFileName(" report.pdf "))
        assertEquals(255, validator.storedFileName("b".repeat(400) + ".pdf").length)
        assertEquals(PdfValidator.FALLBACK_FILE_NAME, validator.storedFileName(""))
    }
}
