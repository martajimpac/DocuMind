package com.example.chatbot.ui.utils

import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

actual fun formatDecimal(value: Double, maxFractionDigits: Int): String =
    NumberFormat.getNumberInstance().apply { maximumFractionDigits = maxFractionDigits }.format(value)

actual fun formatDate(epochMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))
