package com.ktorc.server

import io.ktor.application.*
import io.ktor.response.*

suspend fun ApplicationCall.createChatRoom(id: String) {
    respondRedirect("/room/$id")
}