package com.ktorc.server

import com.ktorc.KtorcConstants
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.sync.withLock

/**
 * All usable commands that a user can enter, at any point in a Frame, are handled here.
 */

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
            KtorcConstants.COMMAND.LEAVE_ROOM -> leaveRoom(commandArg, thisConnection)
            KtorcConstants.COMMAND.HERE -> checkRoomPopulations(thisConnection)
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
        // Join New Room
        sharedResourceLock.withLock {
            chatRooms[newRoomId]!! += thisConnection
        }
        thisConnection.roomLock.withLock {
            thisConnection.rooms.add(newRoomId)
        }
        broadcastToRooms(KtorcConstants.WELCOME_MSG_FORMAT.format(thisConnection.userId, newRoomId), newRoomId)
    } else {
        send("Cannot Join Room: Room selected does not exist.")
    }
}

suspend fun DefaultWebSocketServerSession.leaveRoom(roomId: String?, thisConnection: Connection) {
    if (roomId != null && chatRooms.keys.contains(roomId)) {
        broadcastToRooms("${thisConnection.userId} is leaving ${thisConnection.rooms[0]}", roomId)
        sharedResourceLock.withLock {
            chatRooms[roomId]!! -= thisConnection
        }
        thisConnection.roomLock.withLock {
            thisConnection.rooms.remove(roomId)
        }
    } else {
        send("Cannot Leave Room: Room does not exist")
    }
}

suspend fun DefaultWebSocketServerSession.checkRoomPopulations(thisConnection: Connection) {
    for (room in thisConnection.rooms) {
        send(
            "Users in $room: ${
                chatRooms[room]?.map { userConnection -> userConnection.userId }
            }"
        )
    }
}
