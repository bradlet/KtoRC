package com.ktorc.server

import com.ktorc.KtorcConstants.Paths
import com.ktorc.KtorcConstants.Headers
import com.ktorc.KtorcConstants.COMMAND_PREFIX
import com.ktorc.KtorcConstants.DEFAULT_ROOM
import com.ktorc.KtorcConstants.STD_RESPONSE_FORMAT
import com.ktorc.KtorcConstants.WELCOME_MSG_FORMAT
import io.ktor.http.cio.websocket.*
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
fun Route.getChat() {
    webSocket(Paths.DEFAULT_URI) { // websocketSession
        val userId: String = call.request.headers[Headers.USER_IDENTIFIER] ?:
            throw IllegalAccessError("User identifier absent; Please provide user id.")
        val userConnection = Connection(this, userId, mutableListOf(DEFAULT_ROOM))

        // Create and add user connection to this chat room, then send welcome msg
        sharedResourceLock.withLock {
            chatRooms[DEFAULT_ROOM]!! += userConnection
        }
        broadcastToRoom(
            chatRooms[DEFAULT_ROOM]!!,
            WELCOME_MSG_FORMAT.format(userId, DEFAULT_ROOM)
        )

        // Main Chat Response Loop
        for (frame in incoming) {
            if (frame is Frame.Text) {
                val text = frame.readText()

                // Handle optional commands that can appear at any point in a msg
                if (text.contains(COMMAND_PREFIX)) {
                    val command: Pair<String?, String?> = text
                        .substringAfter(COMMAND_PREFIX).split(" ").toPair()
                    handleCommand(command, userConnection)
                }

                // Send this message from incoming to the room. For now assume user
                // can only be in one room:
                broadcastToRoom(
                    chatRooms[userConnection.rooms[0]]!!,
                    STD_RESPONSE_FORMAT.format(userId, text)
                )

                // Handle user exit code
                if (text.equals("bye", ignoreCase = true)) {
                    sharedResourceLock.withLock {
                        userConnection.rooms.forEach { chatRooms[it]!! -= userConnection }
                    }
                    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                }
            }
        }

    }
}
