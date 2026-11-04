package com.example.chatbot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chatbot.ui.screens.detail.DetailTab
import com.example.chatbot.ui.screens.detail.NotebookDetailScreen
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.cd_back
import com.example.chatbot.resources.cd_more_options
import com.example.chatbot.resources.detail_ready_to_chat
import com.example.chatbot.ui.theme.ChatbotTheme
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** CA-05: only the active tab is selected, and the header shows back, title, subtitle and more options. */
@RunWith(AndroidJUnit4::class)
class DocumentTabBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val labels = DetailTab.entries.associateWith { text(it.label) }

    @Before
    fun setUp() {
        composeRule.setContent {
            ChatbotTheme {
                NotebookDetailScreen(notebookTitle = TITLE, onBackClick = {})
            }
        }
    }

    @Test
    fun opensOnChatTab() {
        assertOnlySelected(DetailTab.CHAT)
    }

    @Test
    fun selectingEachTabSelectsOnlyThatTab() {
        DetailTab.entries.forEach { tab ->
            composeRule.onNodeWithText(labels.getValue(tab)).performClick()
            assertOnlySelected(tab)
        }
    }

    @Test
    fun headerShowsBackTitleSubtitleAndMoreOptions() {
        composeRule.onNodeWithContentDescription(text(Res.string.cd_back)).assertIsDisplayed()
        composeRule.onNodeWithText(TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(text(Res.string.detail_ready_to_chat), substring = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(text(Res.string.cd_more_options)).assertIsDisplayed()
    }

    private fun assertOnlySelected(selected: DetailTab) {
        DetailTab.entries.forEach { tab ->
            val node = composeRule.onNodeWithText(labels.getValue(tab))
            if (tab == selected) node.assertIsSelected() else node.assertIsNotSelected()
        }
    }

    // Shared strings in the device language, read the same way the screen reads them.
    private fun text(resource: StringResource): String = runBlocking { getString(resource) }

    private companion object {
        const val TITLE = "Annual Report 2024"
    }
}
