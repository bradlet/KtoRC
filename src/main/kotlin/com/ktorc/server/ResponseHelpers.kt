package com.ktorc.server

import com.ktorc.KtorcConstants
import com.ktorc.KtorcConstants.WELCOME_MSG_FORMAT
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.sync.withLock

suspend fun broadcastToRoom(
    room: List<Connection>,
    msg: String
) = room.forEach { it.session.send(Frame.Text(msg)) }

// Chop off first two elements of list to make a pair, nicer to work with.
fun List<String>.toPair(): Pair<String?, String?> {
    if (size < 1)
        return null to null
    if (size == 1)
        return this[0] to null
    return this[0] to this[1]
}

suspend fun DefaultWebSocketServerSession.handleCommand(
    command: Pair<String?, String?>,
    thisConnection: Connection,
) {
    val (uncheckedCommand, commandArg) = command
    uncheckedCommand?.let{
        when (KtorcConstants.COMMAND.nullableValueOf(it)) {
            KtorcConstants.COMMAND.CREATE_ROOM -> if (commandArg != null)
                createRoom(commandArg)
            KtorcConstants.COMMAND.LIST_ROOMS -> send(
                "Available rooms: ${chatRooms.keys}"
            )
            KtorcConstants.COMMAND.JOIN_ROOM -> joinRoom(commandArg, thisConnection)
            //KtorcConstants.COMMAND.DELETE_ROOM -> {} // Not required so can wait
            null -> {}
        }
    }
}

suspend fun DefaultWebSocketServerSession.createRoom(newRoomId: String) {
    sharedResourceLock.withLock {
        if (!chatRooms.containsKey(newRoomId))
            chatRooms[newRoomId] = mutableListOf()
    }
    send("Created room: $newRoomId")
}

suspend fun DefaultWebSocketServerSession.joinRoom(
    newRoomId: String?,
    thisConnection: Connection
) {
    if (newRoomId != null && chatRooms.keys.contains(newRoomId)) {
        // Leave Current Room
        broadcastToRoom(
            chatRooms[thisConnection.rooms[0]]!!,
            "${thisConnection.userId} is leaving ${thisConnection.rooms[0]}"
        )
        sharedResourceLock.withLock {
            chatRooms[thisConnection.rooms[0]]!! -= thisConnection
        }
        thisConnection.roomLock.withLock {
            thisConnection.rooms.removeAt(0)
        }

        // Join New Room
        sharedResourceLock.withLock {
            chatRooms[newRoomId]!! += thisConnection
        }
        thisConnection.roomLock.withLock {
            thisConnection.rooms.add(newRoomId)
        }
        broadcastToRoom(
            chatRooms[newRoomId]!!,
            WELCOME_MSG_FORMAT.format(thisConnection.userId, newRoomId)
        )

    } else {
        send("Cannot Join Room: Room selected does not exist.")
    }
}
