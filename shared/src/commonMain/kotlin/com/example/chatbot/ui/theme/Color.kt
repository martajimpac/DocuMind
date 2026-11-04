package com.example.chatbot.ui.theme

import androidx.compose.ui.graphics.Color

// DocuMind "Soft Intelligence" palette (Figma style guide, node 16:159).
// Screens never use these directly: they read MaterialTheme.colorScheme or DocuMindTheme.colors.

internal val Cream = Color(0xFFF7F6F2)
internal val White = Color(0xFFFFFFFF)
internal val Ink = Color(0xFF202124)
internal val Violet = Color(0xFF7771E8)
internal val VioletTint = Color(0xFFF2F0F8)
internal val Muted = Color(0xFF6B6B66)
internal val Hairline = Color(0xFFE8E6E0)
internal val Success = Color(0xFF4D9275)

// Not in the Figma file: Material 3 baseline error red, readable on White and Cream (form errors).
internal val Danger = Color(0xFFB3261E)
internal val Handle = Color(0xFFD7D5CF)

// Soft shadow tint used by cards (Figma: black at 3–4 % alpha).
internal val ShadowTint = Color(0x0A000000)
