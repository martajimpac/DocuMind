package com.example.chatbot.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Corner radii from the Figma frames. */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // document cover
    small = RoundedCornerShape(12.dp),       // citation card, passage, icon buttons
    medium = RoundedCornerShape(16.dp),      // document card, tab, PDF page
    large = RoundedCornerShape(20.dp),       // "Create new notebook" card
    extraLarge = RoundedCornerShape(28.dp),  // bottom sheet
)

/** Shapes with no Material slot. */
@Immutable
data class DocuMindShapes(
    val pill: Shape = CircleShape,
    val tile: Shape = RoundedCornerShape(10.dp),
    val sheet: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val userBubble: Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 4.dp, bottomStart = 20.dp),
)
