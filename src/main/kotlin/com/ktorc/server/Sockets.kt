package com.ktorc.server

import com.ktorc.KtorcConstants.Paths
import com.ktorc.KtorcConstants.COMMAND
import com.ktorc.KtorcConstants.Headers
import com.ktorc.KtorcConstants.Params
import com.ktorc.KtorcConstants.COMMAND_PREFIX
import com.ktorc.KtorcConstants.DEFAULT_ROOM
import com.ktorc.KtorcConstants.STD_RESPONSE_FORMAT
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.websocket.*
import io.ktor.routing.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal val sharedResourceLock = Mutex()

// A map that tracks all rooms in existence, as well as all connections to each room.
// Global is the default room and is always present.
internal val chatRooms: MutableMap<String, MutableList<Connection>> = mutableMapOf(
    DEFAULT_ROOM to mutableListOf()
)

/**
 * Root path
 *
 * A 'global chat' which supports functionality to create private chat sessions/rooms.
 */
fun Route.getGlobalChat() {
    webSocket(Paths.DEFAULT_URI) { // websocketSession
        val userId: String = call.request.headers[Headers.USER_IDENTIFIER] ?:
            throw IllegalAccessError("User identifier absent; Please provide user id.")
        val userConnection = Connection(this, userId)

        // Create and add user connection to global chat room, then send welcome msg
        sharedResourceLock.withLock {
            chatRooms[DEFAULT_ROOM]!! += userConnection
        }
        broadcastToRoom(
            chatRooms[DEFAULT_ROOM]!!,
            "Welcome $userId to the global chat!"
        )

        for (frame in incoming) {
            if (frame is Frame.Text) {
                val text = frame.readText()

                // Handle optional commands that can appear at any point in a msg
                if (text.contains(COMMAND_PREFIX)) {
                    val command: Pair<String?, String?> = text
                        .substringAfter(COMMAND_PREFIX).split(" ").toPair()
                    handleCommand(command)
                }

                broadcastToRoom(
                    chatRooms[DEFAULT_ROOM]!!,
                    STD_RESPONSE_FORMAT.format(userId, text)
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
    webSocket(Paths.ROOM_URI.format(Params.ROOM_IDENTIFIER)) {
        val room = call.parameters[Params.ROOM_IDENTIFIER] ?:
            throw IllegalStateException("Room entry refused; no room id provided.")
    }
}