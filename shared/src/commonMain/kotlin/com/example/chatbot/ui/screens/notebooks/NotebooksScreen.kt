package com.example.chatbot.ui.screens.notebooks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chatbot.domain.model.Notebook
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.account_initials
import com.example.chatbot.resources.account_sign_out
import com.example.chatbot.resources.action_retry
import com.example.chatbot.resources.cd_account
import com.example.chatbot.resources.ic_arrow_down
import com.example.chatbot.resources.ic_lock_keyhole
import com.example.chatbot.resources.ic_search
import com.example.chatbot.resources.library_added_on
import com.example.chatbot.resources.library_create_notebook
import com.example.chatbot.resources.library_empty_body
import com.example.chatbot.resources.library_empty_title
import com.example.chatbot.resources.library_load_error
import com.example.chatbot.resources.library_privacy
import com.example.chatbot.resources.library_recent
import com.example.chatbot.resources.library_search_placeholder
import com.example.chatbot.resources.library_sort_last_opened
import com.example.chatbot.resources.library_start
import com.example.chatbot.resources.library_subtitle
import com.example.chatbot.resources.library_title
import com.example.chatbot.resources.meta_separator
import com.example.chatbot.resources.sources_count
import com.example.chatbot.ui.components.BrandLockup
import com.example.chatbot.ui.components.CancelUploadDialog
import com.example.chatbot.ui.components.DmErrorText
import com.example.chatbot.ui.components.DmLoadErrorCard
import com.example.chatbot.ui.components.DmAvatar
import com.example.chatbot.ui.components.DmElevation
import com.example.chatbot.ui.components.DmInlineAction
import com.example.chatbot.ui.components.DmPillField
import com.example.chatbot.ui.components.DmPrimaryButton
import com.example.chatbot.ui.components.DmSurfaceCard
import com.example.chatbot.ui.components.DocumentListItem
import com.example.chatbot.ui.components.SectionHeader
import com.example.chatbot.ui.components.SystemBarsProtection
import com.example.chatbot.ui.components.UploadProgressCard
import com.example.chatbot.ui.files.rememberPdfPicker
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.theme.Dimens
import com.example.chatbot.ui.theme.DocuMindTheme
import com.example.chatbot.ui.utils.ObserveAsEvents
import com.example.chatbot.ui.utils.formatDate
import com.example.chatbot.ui.utils.formatFileSize
import com.example.chatbot.ui.utils.message
import com.example.chatbot.ui.utils.uploadStatusText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class) // BackHandler is still experimental in Compose Multiplatform 1.9.
@Composable
fun NotebooksScreen(
    onOpenNotebook: (id: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {},
    viewModel: NotebooksViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pickPdf = rememberPdfPicker(viewModel::onPdfPicked)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is NotebooksEvent.OpenNotebook -> onOpenNotebook(event.id, event.title)
            // This is the root screen: after cancelling, stay here; the next Back leaves the app.
            NotebooksEvent.Leave -> Unit
        }
    }
    // Coming back from a notebook (or from the background) shows its new sources.
    LifecycleStartEffect(viewModel) {
        viewModel.refresh()
        onStopOrDispose {}
    }
    BackHandler(enabled = uiState.interceptBack) { viewModel.onBackPressed() }

    NotebooksContent(
        uiState = uiState,
        onStart = pickPdf,
        onOpenNotebook = onOpenNotebook,
        onRetryLoad = viewModel::onRetryLoad,
        onRetryUpload = viewModel::onRetryUpload,
        onSignOut = onSignOut,
        modifier = modifier,
    )

    if (uiState.showCancelDialog) {
        CancelUploadDialog(
            onConfirm = viewModel::onCancelUploadConfirmed,
            onDismiss = viewModel::onCancelDialogDismissed,
        )
    }
}

@Composable
private fun NotebooksContent(
    uiState: NotebooksUiState,
    onStart: () -> Unit,
    onOpenNotebook: (id: String, title: String) -> Unit,
    onRetryLoad: () -> Unit,
    onRetryUpload: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .imePadding(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + Dimens.screenPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.space16),
            ) {
                item(key = "header") { LibraryHeader(onSignOut = onSignOut) }
                screenItem("title") {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.space6)) {
                        Text(
                            text = stringResource(Res.string.library_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = stringResource(Res.string.library_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                screenItem("create") {
                    CreateNotebookCard(
                        enabled = uiState.canStartUpload,
                        loading = uiState.upload?.isRunning == true,
                        onStart = onStart,
                    )
                }
                uiState.pickError?.let { error ->
                    screenItem("pick-error") { DmErrorText(error.message()) }
                }
                uiState.upload?.let { upload ->
                    screenItem("upload") {
                        UploadProgressCard(
                            fileName = upload.fileName,
                            statusText = uploadStatusText(upload.progress),
                            progress = upload.progress,
                            errorMessage = upload.error?.message(),
                            retryLabel = stringResource(Res.string.action_retry),
                            onRetry = onRetryUpload,
                        )
                    }
                }
                screenItem("search") {
                    DmPillField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = stringResource(Res.string.library_search_placeholder),
                        leadingIcon = painterResource(Res.drawable.ic_search),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                notebookList(uiState.list, onOpenNotebook, onRetryLoad)
                screenItem("privacy") { PrivacyNote() }
            }
            SystemBarsProtection()
        }
    }
}

/** An item with the horizontal screen margins. */
private fun LazyListScope.screenItem(key: String, content: @Composable () -> Unit) {
    item(key = key) {
        Box(modifier = Modifier.padding(horizontal = Dimens.screenPadding)) { content() }
    }
}

private fun LazyListScope.notebookList(
    list: NotebookListState,
    onOpenNotebook: (id: String, title: String) -> Unit,
    onRetryLoad: () -> Unit,
) {
    val count = (list as? NotebookListState.Content)?.notebooks?.size ?: 0
    screenItem("section") {
        SectionHeader(title = stringResource(Res.string.library_recent), count = count) {
            DmInlineAction(
                text = stringResource(Res.string.library_sort_last_opened),
                icon = painterResource(Res.drawable.ic_arrow_down),
                onClick = {},
            )
        }
    }
    when (list) {
        NotebookListState.Loading -> screenItem("loading") {
            Box(modifier = Modifier.fillMaxWidth().padding(Dimens.space24), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        NotebookListState.Empty -> screenItem("empty") { EmptyLibraryCard() }
        is NotebookListState.Error -> screenItem("error") {
            DmLoadErrorCard(message = stringResource(Res.string.library_load_error), onRetry = onRetryLoad)
        }
        is NotebookListState.Content -> items(list.notebooks, key = { it.id }) { notebook ->
            Box(modifier = Modifier.padding(horizontal = Dimens.screenPadding)) {
                NotebookCard(notebook = notebook, onClick = { onOpenNotebook(notebook.id, notebook.title) })
            }
        }
    }
}

@Composable
private fun NotebookCard(notebook: Notebook, onClick: () -> Unit) {
    DocumentListItem(
        title = notebook.title,
        coverTitle = notebook.title.uppercase(),
        metadata = listOf(
            pluralStringResource(Res.plurals.sources_count, notebook.sourceCount, notebook.sourceCount),
            formatFileSize(notebook.totalSizeBytes),
            stringResource(Res.string.library_added_on, formatDate(notebook.createdAtEpochMillis)),
        ).joinToString(stringResource(Res.string.meta_separator)),
        onClick = onClick,
    )
}

@Composable
private fun CreateNotebookCard(enabled: Boolean, loading: Boolean, onStart: () -> Unit) {
    DmSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = DmElevation.High,
        verticalArrangement = Arrangement.spacedBy(Dimens.space14),
    ) {
        Text(
            text = stringResource(Res.string.library_create_notebook),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        DmPrimaryButton(
            text = stringResource(Res.string.library_start),
            onClick = onStart,
            enabled = enabled,
            loading = loading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EmptyLibraryCard() {
    DmSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.space6),
    ) {
        Text(
            text = stringResource(Res.string.library_empty_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(Res.string.library_empty_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LibraryHeader(onSignOut: () -> Unit, modifier: Modifier = Modifier) {
    var isAccountMenuOpen by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.libraryBarHeight)
            .padding(start = Dimens.screenPadding, end = Dimens.screenPadding - Dimens.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandLockup(modifier = Modifier.weight(1f))
        Box {
            DmAvatar(
                initials = stringResource(Res.string.account_initials),
                contentDescription = stringResource(Res.string.cd_account),
                onClick = { isAccountMenuOpen = true },
            )
            DropdownMenu(
                expanded = isAccountMenuOpen,
                onDismissRequest = { isAccountMenuOpen = false },
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(Res.string.account_sign_out),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = {
                        isAccountMenuOpen = false
                        onSignOut()
                    },
                )
            }
        }
    }
}

@Composable
private fun PrivacyNote(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.space6, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_lock_keyhole),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.iconXs),
        )
        Text(
            text = stringResource(Res.string.library_privacy),
            style = DocuMindTheme.typography.meta,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun NotebooksContentPreview() {
    ChatbotTheme {
        NotebooksContent(
            uiState = NotebooksUiState(
                list = NotebookListState.Content(
                    listOf(
                        Notebook("1", "Annual Report 2024", 1_793_750_400_000, 3, 2_516_582),
                        Notebook("2", "Brand Guidelines", 1_793_664_000_000, 1, 962_560),
                    ),
                ),
                upload = UploadUiState(fileName = "Research summary.pdf", progress = 0.4f),
            ),
            onStart = {},
            onOpenNotebook = { _, _ -> },
            onRetryLoad = {},
            onRetryUpload = {},
            onSignOut = {},
        )
    }
}

@Preview
@Composable
private fun NotebooksEmptyPreview() {
    ChatbotTheme {
        NotebooksContent(NotebooksUiState(list = NotebookListState.Empty), {}, { _, _ -> }, {}, {}, {})
    }
}
