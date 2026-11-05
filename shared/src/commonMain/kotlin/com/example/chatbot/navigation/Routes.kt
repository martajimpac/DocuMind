package com.example.chatbot.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Argument names are also the SavedStateHandle keys the view models read.

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data class ForgotPasswordRoute(val email: String = "")

@Serializable
data class ResetPasswordRoute(
    val code: String? = null,
    @SerialName("error_code") val errorCode: String? = null,
)

@Serializable
data object NotebooksRoute

/** The title is shown at once, before the notebook is loaded by [notebookId]. */
@Serializable
data class NotebookDetailRoute(val notebookId: String, val title: String)
