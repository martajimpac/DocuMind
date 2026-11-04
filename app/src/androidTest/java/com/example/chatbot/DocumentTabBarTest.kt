package com.example.chatbot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.chatbot.ui.screens.detail.DetailTab
import com.example.chatbot.ui.screens.detail.NotebookDetailScreen
import com.example.chatbot.ui.theme.ChatbotTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** CA-05: only the active tab is selected, and the header shows back, title, subtitle and more options. */
@RunWith(AndroidJUnit4::class)
class DocumentTabBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val labels = DetailTab.entries.associateWith { context.getString(it.label) }

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
        composeRule.onNodeWithContentDescription(context.getString(R.string.cd_back)).assertIsDisplayed()
        composeRule.onNodeWithText(TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.detail_ready_to_chat), substring = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.cd_more_options)).assertIsDisplayed()
    }

    private fun assertOnlySelected(selected: DetailTab) {
        DetailTab.entries.forEach { tab ->
            val node = composeRule.onNodeWithText(labels.getValue(tab))
            if (tab == selected) node.assertIsSelected() else node.assertIsNotSelected()
        }
    }

    private companion object {
        const val TITLE = "Annual Report 2024"
    }
}
