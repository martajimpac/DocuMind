package com.example.chatbot.ui.utils

import java.text.NumberFormat

actual fun formatPercent(fraction: Double): String = NumberFormat.getPercentInstance().format(fraction)
