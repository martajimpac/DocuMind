package com.example.chatbot.ui.screens.detail

import org.jetbrains.compose.resources.StringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.ic_sparkles
import com.example.chatbot.resources.lab_audio_summary
import com.example.chatbot.resources.lab_audio_summary_body
import com.example.chatbot.resources.lab_banner_body
import com.example.chatbot.resources.lab_banner_title
import com.example.chatbot.resources.lab_faq
import com.example.chatbot.resources.lab_faq_body
import com.example.chatbot.resources.lab_flashcards
import com.example.chatbot.resources.lab_flashcards_body
import com.example.chatbot.resources.lab_generate
import com.example.chatbot.resources.lab_key_ideas
import com.example.chatbot.resources.lab_key_ideas_body
import com.example.chatbot.resources.lab_quiz
import com.example.chatbot.resources.lab_quiz_body
import com.example.chatbot.resources.lab_study_guide
import com.example.chatbot.resources.lab_study_guide_body
import com.example.chatbot.resources.lab_tools_title
import com.example.chatbot.ui.components.DmIconTile
import com.example.chatbot.ui.components.DmIconTileStyle
import com.example.chatbot.ui.components.DmPrimaryButton
import com.example.chatbot.ui.components.DmSurfaceCard
import com.example.chatbot.ui.components.SectionHeader
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.theme.Dimens
import com.example.chatbot.ui.theme.DocuMindTheme

data class LabTool(
    val id: String,
    val title: StringResource,
    val description: StringResource,
    val icon: ImageVector,
)

private val labTools = listOf(
    LabTool("study-guide", Res.string.lab_study_guide, Res.string.lab_study_guide_body, Icons.Outlined.Psychology),
    LabTool("flashcards", Res.string.lab_flashcards, Res.string.lab_flashcards_body, Icons.Outlined.Style),
    LabTool("quiz", Res.string.lab_quiz, Res.string.lab_quiz_body, Icons.Outlined.Quiz),
    LabTool("audio-summary", Res.string.lab_audio_summary, Res.string.lab_audio_summary_body, Icons.Outlined.GraphicEq),
    LabTool("key-ideas", Res.string.lab_key_ideas, Res.string.lab_key_ideas_body, Icons.Outlined.Lightbulb),
    LabTool("faq", Res.string.lab_faq, Res.string.lab_faq_body, Icons.AutoMirrored.Outlined.HelpOutline),
)

private const val TWO_LINES = 2

@Composable
fun LabTab(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.space16),
        horizontalArrangement = Arrangement.spacedBy(Dimens.space12),
        verticalArrangement = Arrangement.spacedBy(Dimens.space12),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "banner") {
            DmSurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.space8),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DmIconTile(icon = painterResource(Res.drawable.ic_sparkles))
                    Text(
                        text = stringResource(Res.string.lab_banner_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = stringResource(Res.string.lab_banner_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }, key = "title") {
            SectionHeader(
                title = stringResource(Res.string.lab_tools_title),
                modifier = Modifier.padding(top = Dimens.space8),
            )
        }

        items(labTools, key = { it.id }) { tool ->
            LabToolCard(tool = tool)
        }
    }
}

@Composable
private fun LabToolCard(
    tool: LabTool,
    modifier: Modifier = Modifier,
) {
    DmSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Dimens.space14),
        verticalArrangement = Arrangement.spacedBy(Dimens.space10),
        onClick = {},
    ) {
        DmIconTile(
            icon = rememberVectorPainter(tool.icon),
            size = Dimens.toolTile,
            iconSize = Dimens.iconXl,
            style = DmIconTileStyle.Soft,
            shape = DocuMindTheme.shapes.pill,
        )
        Text(
            text = stringResource(tool.title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = TWO_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(tool.description),
            style = DocuMindTheme.typography.caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = TWO_LINES,
            maxLines = TWO_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        DmPrimaryButton(
            text = stringResource(Res.string.lab_generate),
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun LabTabPreview() {
    ChatbotTheme {
        LabTab(modifier = Modifier.background(MaterialTheme.colorScheme.background))
    }
}
