package com.ktorc.server

import com.ktorc.KtorcConstants
import com.ktorc.KtorcConstants.Paths
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.websocket.*
import kotlinx.coroutines.sync.withLock

suspend fun DefaultWebSocketServerSession.createRoom(newRoomId: String) {
    sharedResourceLock.withLock {
        if (!chatRooms.containsKey(newRoomId))
            chatRooms[newRoomId] = mutableListOf()
    }
    send(Frame.Text("Created room: $newRoomId"))
}

suspend fun broadcastToRoom(
    room: List<Connection>,
    msg: String
) = room.forEach { it.session.send(Frame.Text(msg)) }

suspend fun DefaultWebSocketServerSession.handleCommand(command: Pair<String?, String?>) {
    val (uncheckedCommand, commandArg) = command
    uncheckedCommand?.let{
        when (KtorcConstants.COMMAND.nullableValueOf(it)) {
            KtorcConstants.COMMAND.CREATE_ROOM -> if (commandArg != null)
                createRoom(commandArg)
            KtorcConstants.COMMAND.LIST_ROOMS -> send(
                Frame.Text("Available rooms: ${chatRooms.keys}")
            )
            KtorcConstants.COMMAND.JOIN_ROOM -> if (commandArg != null)
                call.respondRedirect(Paths.ROOM_URI.format(commandArg))
            KtorcConstants.COMMAND.DELETE_ROOM -> {} // Not required so can wait
            null -> {}
        }
    }
}

// Chop off first two elements of list to make a pair, nicer to work with.
fun List<String>.toPair(): Pair<String?, String?> {
    if (size < 1)
        return null to null
    if (size == 1)
        return this[0] to null
    return this[0] to this[1]
}