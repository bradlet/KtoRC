package com.ktorc

import com.ktorc.KtorcConstants.STD_RESPONSE_FORMAT
import com.ktorc.server.setupWebSockets
import io.ktor.http.cio.websocket.*
import kotlin.test.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay

class ApplicationTest {

    companion object {
        private const val testUId = "test_user"
        private val testMessages = listOf("Who goes there?!", "This is a weird echo")
    }

    @Test
    fun testRootWelcomeMessage() {
        withTestApplication({ setupWebSockets() }) {

            handleWebSocketConversation("/", {
                addHeader(KtorcConstants.Headers.USER_IDENTIFIER, testUId)
            }) { incoming, _ ->
                val welcomeMsg = (incoming.receive() as Frame.Text).readText()
                assert(welcomeMsg.contains(testUId))
            }
        }
    }
}