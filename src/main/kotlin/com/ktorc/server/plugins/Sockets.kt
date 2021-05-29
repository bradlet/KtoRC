package com.ktorc.server.plugins

import com.ktorc.KtorcConstants
import com.ktorc.KtorcConstants.STD_RESPONSE_FORMAT
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
            val userId: String = call.request.headers[KtorcConstants.Headers.USER_IDENTIFIER] ?:
                throw IllegalAccessError("User identifier absent; Please provide user id.")

            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()

                        outgoing.send(
                            Frame.Text(STD_RESPONSE_FORMAT.format(userId, text))
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
