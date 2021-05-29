package com.ktorc

import com.ktorc.KtorcConstants.STD_RESPONSE_FORMAT
import com.ktorc.server.plugins.configureSockets
import io.ktor.http.cio.websocket.*
import kotlin.test.*
import io.ktor.server.testing.*

class ApplicationTest {

    @Test
    fun testConventionalConversation() {
        val testUId = "test_user"
        val testMessages = listOf("Who goes there?!", "This is a weird echo")

        withTestApplication({ configureSockets() }) {

            handleWebSocketConversation("/", {
                addHeader(KtorcConstants.Headers.USER_IDENTIFIER, testUId)
            }) { incoming, outgoing ->
                for (msg in testMessages) {
                    outgoing.send(Frame.Text(msg))

                    val responseMsg: String = (incoming.receive() as Frame.Text).readText()
                    val expected = STD_RESPONSE_FORMAT.format(testUId, msg)
                    assertEquals(responseMsg, expected)
                }
            }
        }
    }
}