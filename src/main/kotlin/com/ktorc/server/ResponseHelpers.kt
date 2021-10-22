package com.ktorc.server

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersionName
import com.ktorc.KtorcConstants
import io.ktor.http.cio.websocket.*

suspend fun broadcastToRooms(
    msg: String,
    vararg rooms: String,
) {
    rooms.forEach { roomName ->
        chatRooms[roomName]?.forEach {
            it.session.send(Frame.Text("[$roomName] $msg"))
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
