package com.example.chatbot.ui.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun formatDecimal(value: Double, maxFractionDigits: Int): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        maximumFractionDigits = maxFractionDigits.toULong()
    }
    return formatter.stringFromNumber(NSNumber(value)) ?: value.toString()
}

actual fun formatDate(epochMillis: Long): String {
    val formatter = NSDateFormatter().apply {
        dateStyle = NSDateFormatterMediumStyle
        timeStyle = NSDateFormatterNoStyle
    }
    return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0))
}
