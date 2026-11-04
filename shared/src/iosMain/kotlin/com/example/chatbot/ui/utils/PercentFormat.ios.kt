package com.example.chatbot.ui.utils

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterPercentStyle

actual fun formatPercent(fraction: Double): String {
    val formatter = NSNumberFormatter().apply { numberStyle = NSNumberFormatterPercentStyle }
    return formatter.stringFromNumber(NSNumber(fraction)) ?: "${(fraction * 100).toInt()}%"
}
