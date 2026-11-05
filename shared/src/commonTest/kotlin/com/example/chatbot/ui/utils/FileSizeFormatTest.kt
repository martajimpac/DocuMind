package com.example.chatbot.ui.utils

import kotlin.test.Test
import kotlin.test.assertEquals

/** RF-05: sizes shown as in the design ("940 KB", "2.4 MB"). */
class FileSizeFormatTest {

    @Test
    fun `bytes below one megabyte use kilobytes, rounded and at least 1`() {
        assertEquals(FileSize(1.0, SizeUnit.KB), fileSizeOf(1))
        assertEquals(FileSize(940.0, SizeUnit.KB), fileSizeOf(940L * 1024))
        assertEquals(FileSize(1023.0, SizeUnit.KB), fileSizeOf(1024L * 1024 - 600))
    }

    @Test
    fun `one megabyte and above use megabytes with one decimal`() {
        assertEquals(FileSize(1.0, SizeUnit.MB), fileSizeOf(1024L * 1024))
        assertEquals(FileSize(2.4, SizeUnit.MB), fileSizeOf((2.4 * 1024 * 1024).toLong()))
        assertEquals(FileSize(20.0, SizeUnit.MB), fileSizeOf(20L * 1024 * 1024))
    }
}
