package com.example.chatbot.ui.screens.detail

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.app_name
import com.example.chatbot.resources.cd_copy_answer
import com.example.chatbot.resources.cd_send_question
import com.example.chatbot.resources.chat_disclaimer
import com.example.chatbot.resources.chat_input_placeholder
import com.example.chatbot.resources.chat_sample_answer_body
import com.example.chatbot.resources.chat_sample_answer_intro
import com.example.chatbot.resources.chat_sample_answer_lead
import com.example.chatbot.resources.chat_sample_answer_reason
import com.example.chatbot.resources.chat_sample_question
import com.example.chatbot.resources.citation_page_section
import com.example.chatbot.resources.citation_view_passage
import com.example.chatbot.resources.doc_annual_report
import com.example.chatbot.resources.ic_arrow_up
import com.example.chatbot.resources.ic_copy
import com.example.chatbot.resources.ic_sparkles
import com.example.chatbot.resources.page_title
import com.example.chatbot.ui.components.CitationCard
import com.example.chatbot.ui.components.CitedParagraph
import com.example.chatbot.ui.components.DmEdge
import com.example.chatbot.ui.components.DmIconButton
import com.example.chatbot.ui.components.DmIconButtonStyle
import com.example.chatbot.ui.components.DmIconTile
import com.example.chatbot.ui.components.DmPillField
import com.example.chatbot.ui.components.DmPillFieldStyle
import com.example.chatbot.ui.components.UserMessageBubble
import com.example.chatbot.ui.components.hairlineEdge
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.theme.Dimens
import com.example.chatbot.ui.theme.DocuMindTheme

/** Entries of the local, sample conversation. */
sealed interface ChatEntry {
    val id: String

    /** Sample question from the design, read from resources so it follows the device language. */
    data object SampleQuestion : ChatEntry {
        override val id = "sample-question"
    }

    /** Sample cited answer from the design. */
    data object SampleAnswer : ChatEntry {
        override val id = "sample-answer"
    }

    /** Question typed by the user in this session. */
    data class UserQuestion(override val id: String, val text: String) : ChatEntry
}

// The sample answer cites page 12 of the sample document.
private const val SAMPLE_CITATION = 1
private const val SAMPLE_CITED_PAGE = 12

@Composable
fun ChatTab(modifier: Modifier = Modifier) {
    var messageText by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(listOf<ChatEntry>(ChatEntry.SampleQuestion, ChatEntry.SampleAnswer)) }

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(
                start = Dimens.screenPadding,
                end = Dimens.screenPadding,
                top = Dimens.space24,
                bottom = Dimens.space20,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.space24 + Dimens.space2),
        ) {
            items(entries, key = { it.id }) { entry ->
                when (entry) {
                    ChatEntry.SampleQuestion -> UserMessageBubble(text = stringResource(Res.string.chat_sample_question))
                    ChatEntry.SampleAnswer -> SampleAnswer()
                    is ChatEntry.UserQuestion -> UserMessageBubble(text = entry.text)
                }
            }
        }

        MessageComposer(
            value = messageText,
            onValueChange = { messageText = it },
            onSend = {
                if (messageText.isNotBlank()) {
                    entries = entries + ChatEntry.UserQuestion(id = newEntryId(), text = messageText)
                    messageText = ""
                }
            },
        )
    }
}

@Composable
private fun SampleAnswer(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimens.space16)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DmIconTile(icon = painterResource(Res.drawable.ic_sparkles))
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        CitedParagraph(body = stringResource(Res.string.chat_sample_answer_intro))
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.space14)) {
            CitedParagraph(
                lead = stringResource(Res.string.chat_sample_answer_lead),
                body = stringResource(Res.string.chat_sample_answer_body),
                citationNumber = SAMPLE_CITATION,
            )
            CitedParagraph(
                body = stringResource(Res.string.chat_sample_answer_reason),
                citationNumber = SAMPLE_CITATION,
            )
        }
        CitationCard(
            citationNumber = SAMPLE_CITATION,
            documentTitle = stringResource(Res.string.doc_annual_report),
            location = stringResource(Res.string.citation_page_section, SAMPLE_CITED_PAGE, stringResource(Res.string.page_title)),
            actionLabel = stringResource(Res.string.citation_view_passage),
            // Opening the cited passage is out of scope for this redesign.
            onClick = {},
        )
        DmIconButton(
            icon = painterResource(Res.drawable.ic_copy),
            contentDescription = stringResource(Res.string.cd_copy_answer),
            onClick = {},
            style = DmIconButtonStyle.Outlined,
        )
    }
}

@Composable
private fun MessageComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .hairlineEdge(DmEdge.Top)
            .padding(start = Dimens.space16, end = Dimens.space16, top = Dimens.space12, bottom = Dimens.space10),
        verticalArrangement = Arrangement.spacedBy(Dimens.space10),
    ) {
        DmPillField(
            value = value,
            onValueChange = onValueChange,
            placeholder = stringResource(Res.string.chat_input_placeholder),
            style = DmPillFieldStyle.Recessed,
            minHeight = Dimens.composerFieldHeight,
            singleLine = false,
            contentPadding = PaddingValues(start = Dimens.space16 + Dimens.space2, end = Dimens.space6),
            modifier = Modifier.fillMaxWidth(),
            trailing = {
                DmIconButton(
                    icon = painterResource(Res.drawable.ic_arrow_up),
                    contentDescription = stringResource(Res.string.cd_send_question),
                    onClick = onSend,
                    style = DmIconButtonStyle.Filled,
                    iconSize = Dimens.iconXl + Dimens.hairline,
                )
            },
        )
        Text(
            text = stringResource(Res.string.chat_disclaimer),
            style = DocuMindTheme.typography.fine,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun ChatTabPreview() {
    ChatbotTheme {
        ChatTab(modifier = Modifier.background(MaterialTheme.colorScheme.background))
    }
}

// Unique enough for list keys of locally typed questions.
@OptIn(ExperimentalTime::class)
private fun newEntryId(): String = Clock.System.now().toEpochMilliseconds().toString()
