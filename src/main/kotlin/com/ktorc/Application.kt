package com.ktorc

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.ktorc.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureHTTP()
        configureSockets()
    }.start(wait = true)
}
