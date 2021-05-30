package com.ktorc.server.plugins

import com.ktorc.KtorcConstants.COMMAND
import com.ktorc.KtorcConstants.Headers
import com.ktorc.KtorcConstants.Params
import com.ktorc.KtorcConstants.COMMAND_PREFIX
import com.ktorc.KtorcConstants.STD_RESPONSE_FORMAT
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import io.ktor.routing.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val sharedResourceLock = Mutex()

// A running list of all chat room Ids that currently exist. Global is always present.
private val chatRooms = mutableListOf("Global")

/**
 * Root path
 *
 * A 'global chat' which supports functionality to create private chat sessions/rooms.
 */
fun Route.getGlobalChat() {
    webSocket("/") { // websocketSession
        val userId: String = call.request.headers[Headers.USER_IDENTIFIER] ?:
            throw IllegalAccessError("User identifier absent; Please provide user id.")

        send("Welcome $userId to the global chat!")

        for (frame in incoming) {
            if (frame !is Frame.Text) outgoing.send(
                Frame.Text("Unsupported Frame Type")
            ) else {
                val text = frame.readText()

                if (text.contains(COMMAND_PREFIX)) {
                    val uncheckedCommand = text
                        .substringAfter(COMMAND_PREFIX)
                        .split(" ")

                    when (COMMAND.nullableValueOf(uncheckedCommand[0])) {
                        COMMAND.CREATE_ROOM -> {
                            val roomId = uncheckedCommand[1]
                            sharedResourceLock.withLock {
                                if (!chatRooms.contains(roomId))
                                    chatRooms.add(roomId)
                            }
                            outgoing.send(Frame.Text("Created room: $roomId"))
                        }
                        COMMAND.LIST_ROOMS -> outgoing.send(
                            Frame.Text(chatRooms.toString())
                        )
                        COMMAND.CHANGE_ROOM -> {}
                        COMMAND.DELETE_ROOM -> {}
                        null -> {}
                    }
                }

                outgoing.send(
                    Frame.Text(STD_RESPONSE_FORMAT.format(userId, text))
                )

                if (text.equals("bye", ignoreCase = true)) {
                    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                }
            }
        }
    }
}

/**
 * Chat Room Session Path
 */
fun Route.getChatRoom() {
    webSocket("/room/{${Params.ROOM_IDENTIFIER}}") {
        val room = call.parameters[Params.ROOM_IDENTIFIER] ?:
            throw IllegalStateException("Room entry refused; no room id provided.")
    }
}