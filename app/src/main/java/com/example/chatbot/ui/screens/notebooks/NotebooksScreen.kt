package com.example.chatbot.ui.screens.notebooks

import androidx.annotation.StringRes
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.chatbot.R
import com.example.chatbot.ui.components.BrandLockup
import com.example.chatbot.ui.components.DmAvatar
import com.example.chatbot.ui.components.DmElevation
import com.example.chatbot.ui.components.DmInlineAction
import com.example.chatbot.ui.components.DmPillField
import com.example.chatbot.ui.components.DmPrimaryButton
import com.example.chatbot.ui.components.DmSurfaceCard
import com.example.chatbot.ui.components.DocumentListItem
import com.example.chatbot.ui.components.SectionHeader
import com.example.chatbot.ui.components.SystemBarsProtection
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.theme.Dimens
import com.example.chatbot.ui.theme.DocuMindTheme

/** Sample document shown in the library (no real data yet). */
data class LibraryDocument(
    val id: String,
    @StringRes val title: Int,
    @StringRes val coverTitle: Int,
    val sourceCount: Int,
    @StringRes val size: Int,
    @StringRes val lastActivity: Int,
    @StringRes val status: Int? = null,
)

private val sampleDocuments = listOf(
    LibraryDocument("annual-report", R.string.doc_annual_report, R.string.cover_annual_report, 24, R.string.doc_annual_report_size, R.string.library_opened_just_now, R.string.library_continue),
    LibraryDocument("brand-guidelines", R.string.doc_brand_guidelines, R.string.cover_brand_guidelines, 18, R.string.doc_brand_guidelines_size, R.string.library_added_yesterday),
    LibraryDocument("research-summary", R.string.doc_research_summary, R.string.cover_research_summary, 12, R.string.doc_research_summary_size, R.string.library_added_oct_24),
)

@Composable
fun NotebooksScreen(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {},
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val newNotebookTitle = stringResource(R.string.notebook_default_title)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding),
            ) {
                LibraryHeader(onSignOut = onSignOut)
                Column(
                    modifier = Modifier.padding(start = Dimens.screenPadding, end = Dimens.screenPadding, bottom = Dimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.space16),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.space6)) {
                        Text(
                            text = stringResource(R.string.library_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = stringResource(R.string.library_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DmSurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        elevation = DmElevation.High,
                        verticalArrangement = Arrangement.spacedBy(Dimens.space14),
                    ) {
                        Text(
                            text = stringResource(R.string.library_create_notebook),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        DmPrimaryButton(
                            text = stringResource(R.string.library_start),
                            onClick = { onNavigateToDetail(newNotebookTitle) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    DmPillField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = stringResource(R.string.library_search_placeholder),
                        leadingIcon = painterResource(R.drawable.ic_search),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.space12)) {
                        SectionHeader(title = stringResource(R.string.library_recent), count = sampleDocuments.size) {
                            DmInlineAction(
                                text = stringResource(R.string.library_sort_last_opened),
                                icon = painterResource(R.drawable.ic_arrow_down),
                                onClick = {},
                            )
                        }
                        sampleDocuments.forEach { document ->
                            val title = stringResource(document.title)
                            DocumentListItem(
                                title = title,
                                coverTitle = stringResource(document.coverTitle),
                                metadata = listOf(
                                    pluralStringResource(R.plurals.sources_count, document.sourceCount, document.sourceCount),
                                    stringResource(document.size),
                                    stringResource(document.lastActivity),
                                ).joinToString(stringResource(R.string.meta_separator)),
                                activity = document.status?.let { stringResource(it) },
                                onClick = { onNavigateToDetail(title) },
                            )
                        }
                    }
                    PrivacyNote()
                }
            }
            SystemBarsProtection()
        }
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
                initials = stringResource(R.string.account_initials),
                contentDescription = stringResource(R.string.cd_account),
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
                            text = stringResource(R.string.account_sign_out),
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
            painter = painterResource(R.drawable.ic_lock_keyhole),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.iconXs),
        )
        Text(
            text = stringResource(R.string.library_privacy),
            style = DocuMindTheme.typography.meta,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun NotebooksScreenPreview() {
    ChatbotTheme {
        NotebooksScreen(onNavigateToDetail = {})
    }
}
