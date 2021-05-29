package com.ktorc.server.plugins

import com.ktorc.KtorcConstants
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.application.*
import io.ktor.routing.*

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/") { // websocketSession
            val userId = call.request.headers[KtorcConstants.Headers.USER_IDENTIFIER]

            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        outgoing.send(
                            if (userId.isNullOrBlank())
                                Frame.Text("$userId SAID: $text")
                            else
                                Frame.Text("Unknown UID, please identify yourself.")
                        )
                        if (text.equals("bye", ignoreCase = true)) {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        }
                    }
                    else -> outgoing.send(Frame.Text("Unsupported Frame Type"))
                }
            }
        }
    }
}
