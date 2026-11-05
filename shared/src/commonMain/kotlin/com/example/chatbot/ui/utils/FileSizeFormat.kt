package com.example.chatbot.ui.utils

import kotlin.math.roundToLong

enum class SizeUnit { KB, MB }

data class FileSize(val value: Double, val unit: SizeUnit)

private const val BYTES_PER_KB = 1024.0
private const val BYTES_PER_MB = 1024.0 * 1024.0

/** Whole kilobytes (at least 1) below one megabyte, otherwise megabytes with one decimal. */
fun fileSizeOf(bytes: Long): FileSize {
    val kilobytes = (bytes / BYTES_PER_KB).roundToLong().coerceAtLeast(1)
    if (kilobytes < BYTES_PER_KB) return FileSize(kilobytes.toDouble(), SizeUnit.KB)
    val tenthsOfMb = (bytes / BYTES_PER_MB * 10).roundToLong()
    return FileSize(tenthsOfMb / 10.0, SizeUnit.MB)
}

/** "940 KB", "2.4 MB" or "2,4 MB" depending on the device locale. */
fun formatFileSize(bytes: Long): String {
    val size = fileSizeOf(bytes)
    return "${formatDecimal(size.value, maxFractionDigits = 1)} ${size.unit.name}"
}

/** Formats a number in the device locale with up to [maxFractionDigits] decimals. */
expect fun formatDecimal(value: Double, maxFractionDigits: Int): String
