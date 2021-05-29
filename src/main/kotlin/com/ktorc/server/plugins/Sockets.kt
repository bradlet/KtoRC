package com.ktorc.server.plugins

import com.ktorc.KtorcConstants
import com.ktorc.KtorcConstants.STD_RESPONSE_FORMAT
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import io.ktor.routing.*

/**
 * Root path
 *
 * A 'global chat' which supports functionality to create private chat sessions/rooms.
 */
fun Route.getGlobalChat() {
    webSocket("/") { // websocketSession
        val userId: String = call.request.headers[KtorcConstants.Headers.USER_IDENTIFIER] ?:
            throw IllegalAccessError("User identifier absent; Please provide user id.")

        send("Welcome $userId to the global chat!")

        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()

                    outgoing.send(
                        Frame.Text(STD_RESPONSE_FORMAT.format(userId, text))
                    )

                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
                else -> outgoing.send(Frame.Text("Unsupported Frame Type"))
            }
        }
    }
}

/**
 * Chat Room Session Path
 */
fun Route.getChatRoom() {
    webSocket("/room/{${KtorcConstants.Params.ROOM_IDENTIFIER}}") {
        val room = call.parameters[KtorcConstants.Params.ROOM_IDENTIFIER] ?:
            throw IllegalStateException("Room entry refused; no room id provided.")
    }
}