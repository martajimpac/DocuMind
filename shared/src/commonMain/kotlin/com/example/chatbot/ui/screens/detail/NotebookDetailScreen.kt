package com.example.chatbot.ui.screens.detail

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.detail_pdf_reader
import com.example.chatbot.resources.detail_ready_to_chat
import com.example.chatbot.resources.ic_file_text
import com.example.chatbot.resources.ic_flask_round
import com.example.chatbot.resources.ic_message_circle
import com.example.chatbot.resources.meta_separator
import com.example.chatbot.resources.pages_count
import com.example.chatbot.resources.tab_chat
import com.example.chatbot.resources.tab_document
import com.example.chatbot.resources.tab_lab
import com.example.chatbot.ui.components.DocumentTabBar
import com.example.chatbot.ui.components.DocumentTabItem
import com.example.chatbot.ui.components.DocumentTopBar
import com.example.chatbot.ui.theme.ChatbotTheme

/** Tabs of the document detail, in display order. */
enum class DetailTab(val label: StringResource, val icon: DrawableResource) {
    CHAT(Res.string.tab_chat, Res.drawable.ic_message_circle),
    DOCUMENT(Res.string.tab_document, Res.drawable.ic_file_text),
    LAB(Res.string.tab_lab, Res.drawable.ic_flask_round),
}

// Sample document length shown in the header (no real documents yet).
private const val SAMPLE_PAGE_COUNT = 24

@Composable
fun NotebookDetailScreen(
    notebookTitle: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(DetailTab.CHAT) }
    val pages = pluralStringResource(Res.plurals.pages_count, SAMPLE_PAGE_COUNT, SAMPLE_PAGE_COUNT)
    val separator = stringResource(Res.string.meta_separator)
    val subtitle = when (selectedTab) {
        DetailTab.DOCUMENT -> stringResource(Res.string.detail_pdf_reader) + separator + pages
        else -> pages + separator + stringResource(Res.string.detail_ready_to_chat)
    }
    val tabs = DetailTab.entries.map { DocumentTabItem(stringResource(it.label), painterResource(it.icon)) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DocumentTopBar(
                title = notebookTitle,
                subtitle = subtitle,
                onBackClick = onBackClick,
                onMoreClick = {},
            )
        },
        bottomBar = {
            DocumentTabBar(
                tabs = tabs,
                selectedIndex = selectedTab.ordinal,
                onTabSelected = { selectedTab = DetailTab.entries[it] },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            when (selectedTab) {
                DetailTab.CHAT -> ChatTab()
                DetailTab.DOCUMENT -> DocumentReaderTab()
                DetailTab.LAB -> LabTab()
            }
        }
    }
}

@Preview
@Composable
private fun NotebookDetailScreenPreview() {
    ChatbotTheme {
        NotebookDetailScreen(notebookTitle = "Annual Report 2024", onBackClick = {})
    }
}
