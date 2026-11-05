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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chatbot.ui.components.CancelUploadDialog
import com.example.chatbot.ui.files.rememberPdfPicker
import com.example.chatbot.ui.utils.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel
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

@OptIn(ExperimentalComposeUiApi::class) // BackHandler is still experimental in Compose Multiplatform 1.9.
@Composable
fun NotebookDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotebookDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pickPdf = rememberPdfPicker(viewModel::onPdfPicked)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            NotebookDetailEvent.Leave -> onBackClick()
        }
    }
    // CA-12: Back (system or top bar) during an upload asks before cancelling it.
    BackHandler(enabled = uiState.interceptBack) { viewModel.onBackPressed() }

    NotebookDetailContent(
        notebookTitle = uiState.title,
        onBackClick = { if (uiState.interceptBack) viewModel.onBackPressed() else onBackClick() },
        modifier = modifier,
        documentSources = {
            SourcesSection(
                uiState = uiState,
                onAddSource = pickPdf,
                onRetryLoad = viewModel::onRetryLoad,
                onRetryUpload = viewModel::onRetryUpload,
            )
        },
    )

    if (uiState.showCancelDialog) {
        CancelUploadDialog(
            onConfirm = viewModel::onCancelUploadConfirmed,
            onDismiss = viewModel::onCancelDialogDismissed,
        )
    }
}

/** Stateless detail layout (header, tabs, tab content). Used by the screen, previews and UI tests. */
@Composable
fun NotebookDetailContent(
    notebookTitle: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    documentSources: @Composable () -> Unit = {},
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
                DetailTab.DOCUMENT -> DocumentReaderTab(sources = documentSources)
                DetailTab.LAB -> LabTab()
            }
        }
    }
}

@Preview
@Composable
private fun NotebookDetailContentPreview() {
    ChatbotTheme {
        NotebookDetailContent(notebookTitle = "Annual Report 2024", onBackClick = {})
    }
}
