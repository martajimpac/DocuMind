package com.example.chatbot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.inter_bold
import com.example.chatbot.resources.inter_medium
import com.example.chatbot.resources.inter_regular
import com.example.chatbot.resources.inter_semibold

/** Inter from the shared resources. Resource fonts can only be loaded inside composition. */
@Composable
fun interFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
    Font(Res.font.inter_semibold, FontWeight.SemiBold),
    Font(Res.font.inter_bold, FontWeight.Bold),
)

private fun FontFamily.style(size: Float, weight: FontWeight = FontWeight.Normal, lineHeight: Float? = null) = TextStyle(
    fontFamily = this,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight?.em ?: TextStyle.Default.lineHeight,
    letterSpacing = 0.sp,
)

/** Material slots mapped to the Figma type scale. */
fun materialTypography(inter: FontFamily) = Typography(
    headlineMedium = inter.style(28f, FontWeight.SemiBold),          // "Your library"
    titleLarge = inter.style(20f, FontWeight.SemiBold),              // brand name, sheet title
    titleMedium = inter.style(16f, FontWeight.SemiBold),             // top bar title, card title
    titleSmall = inter.style(15f, FontWeight.SemiBold),              // section heading, sheet document name
    bodyLarge = inter.style(15f, lineHeight = 1.55f),                // chat and passage reading text
    bodyMedium = inter.style(14f),                                   // field text and placeholders
    bodySmall = inter.style(13f),                                    // subtitles
    labelLarge = inter.style(15f, FontWeight.SemiBold),              // primary button
    labelMedium = inter.style(14f, FontWeight.SemiBold),             // list item title, assistant name
    labelSmall = inter.style(13f, FontWeight.SemiBold),              // tabs, citation title, links, avatar
)

/** Styles with no Material slot: small metadata and the printed PDF page. */
@Immutable
class DocuMindTypography(inter: FontFamily = FontFamily.Default) {
    val caption: TextStyle = inter.style(12f)
    val meta: TextStyle = inter.style(11f)
    val fine: TextStyle = inter.style(10f)
    val coverTitle: TextStyle = inter.style(7f, FontWeight.Bold)
    val coverLabel: TextStyle = inter.style(7f)
    val pageBrand: TextStyle = inter.style(9f, FontWeight.Bold)
    val pageEyebrow: TextStyle = inter.style(8f, FontWeight.SemiBold)
    val pageCaption: TextStyle = inter.style(8f)
    val pageTitle: TextStyle = inter.style(25f, FontWeight.SemiBold, lineHeight = 1.1f)
    val pageBody: TextStyle = inter.style(10.5f, lineHeight = 1.5f)
    val pageHeading: TextStyle = inter.style(12f, FontWeight.SemiBold)
    val pagePassage: TextStyle = inter.style(11f, lineHeight = 1.55f)
    val pageTable: TextStyle = inter.style(9f)
    val pageTableStrong: TextStyle = inter.style(9f, FontWeight.SemiBold)
}
