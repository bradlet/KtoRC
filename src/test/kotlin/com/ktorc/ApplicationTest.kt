package com.ktorc

import com.ktorc.server.setupWebSockets
import io.ktor.http.cio.websocket.*
import kotlin.test.*
import io.ktor.server.testing.*

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

    @Test
    fun testRoomCreationCommand() {
        val room = "testRoom"
        withTestApplication({ setupWebSockets() }) {

            handleWebSocketConversation("/", {
                addHeader(KtorcConstants.Headers.USER_IDENTIFIER, testUId)
            }) { incoming, outgoing ->
                incoming.receive() // wipe out welcome msg
                outgoing.send(Frame.Text("cm&CREATE_ROOM $room"))
                val response = (incoming.receive() as Frame.Text).readText()
                assert(response.contains(room))
            }
        }
    }

    @Test
    fun testListRoomsCommand() {
        val rooms = listOf("room1", "room2", "room3")
        withTestApplication({ setupWebSockets() }) {

            handleWebSocketConversation("/", {
                addHeader(KtorcConstants.Headers.USER_IDENTIFIER, testUId)
            }) { incoming, outgoing ->
                incoming.receive() // wipe out welcome msg
                for (room in rooms)
                    outgoing.send(Frame.Text("cm&CREATE_ROOM $room"))
                outgoing.send(Frame.Text("cm&LIST_ROOMS"))

                for (frame in incoming) {
                    val response = (frame as Frame.Text).readText()
                    if (response.contains("Available")) {
                        for (room in rooms) {
                            assert(response.contains(room))
                        }
                        return@handleWebSocketConversation // break out of scope to end test
                    }
                }
            }
        }
    }
}