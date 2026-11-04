package com.example.chatbot.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/** Base for view model tests: replaces Dispatchers.Main so viewModelScope runs on the test scheduler. */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class MainDispatcherTest(val dispatcher: TestDispatcher = StandardTestDispatcher()) {
    @BeforeTest
    fun setMainDispatcher() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun resetMainDispatcher() = Dispatchers.resetMain()
}
