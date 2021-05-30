package com.ktorc.server

import io.ktor.http.cio.websocket.*

@Repeatable
@MustBeDocumented
annotation class UserRequest()

data class Connection(val session: DefaultWebSocketSession, val userId: String)
