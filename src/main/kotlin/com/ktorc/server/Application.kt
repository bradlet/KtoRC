package com.ktorc.server

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.WebSockets
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.time.Duration

fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost") {
        setupWebSockets()
    }.start(wait = true)
}

fun Application.setupWebSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        getChat()
    }
}
