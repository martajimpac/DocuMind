package com.example.chatbot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.chatbot.R

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private fun inter(size: Float, weight: FontWeight = FontWeight.Normal, lineHeight: Float? = null) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight?.em ?: TextStyle.Default.lineHeight,
    letterSpacing = 0.sp,
)

/** Material slots mapped to the Figma type scale. */
val Typography = Typography(
    headlineMedium = inter(28f, FontWeight.SemiBold),          // "Your library"
    titleLarge = inter(20f, FontWeight.SemiBold),              // brand name, sheet title
    titleMedium = inter(16f, FontWeight.SemiBold),             // top bar title, card title
    titleSmall = inter(15f, FontWeight.SemiBold),              // section heading, sheet document name
    bodyLarge = inter(15f, lineHeight = 1.55f),                // chat and passage reading text
    bodyMedium = inter(14f),                                   // field text and placeholders
    bodySmall = inter(13f),                                    // subtitles
    labelLarge = inter(15f, FontWeight.SemiBold),              // primary button
    labelMedium = inter(14f, FontWeight.SemiBold),             // list item title, assistant name
    labelSmall = inter(13f, FontWeight.SemiBold),              // tabs, citation title, links, avatar
)

/** Styles with no Material slot: small metadata and the printed PDF page. */
@Immutable
data class DocuMindTypography(
    val caption: TextStyle = inter(12f),
    val meta: TextStyle = inter(11f),
    val fine: TextStyle = inter(10f),
    val coverTitle: TextStyle = inter(7f, FontWeight.Bold),
    val coverLabel: TextStyle = inter(7f),
    val pageBrand: TextStyle = inter(9f, FontWeight.Bold),
    val pageEyebrow: TextStyle = inter(8f, FontWeight.SemiBold),
    val pageCaption: TextStyle = inter(8f),
    val pageTitle: TextStyle = inter(25f, FontWeight.SemiBold, lineHeight = 1.1f),
    val pageBody: TextStyle = inter(10.5f, lineHeight = 1.5f),
    val pageHeading: TextStyle = inter(12f, FontWeight.SemiBold),
    val pagePassage: TextStyle = inter(11f, lineHeight = 1.55f),
    val pageTable: TextStyle = inter(9f),
    val pageTableStrong: TextStyle = inter(9f, FontWeight.SemiBold),
)
