package com.ktorc.server

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.sync.Mutex

@Repeatable
@MustBeDocumented
annotation class UserRequest()

data class Connection(
    val session: DefaultWebSocketSession,
    val userId: String,
    val rooms: MutableList<String>,
    val roomLock: Mutex = Mutex()
)
