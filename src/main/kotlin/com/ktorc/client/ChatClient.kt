package com.ktorc.client

import com.ktorc.KtorcConstants.Paths
import com.ktorc.KtorcConstants.Headers
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.webSocket
import io.ktor.client.features.websocket.WebSockets
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
fun main() {
    print("Please enter your KtoRC user ID before joining: ")
    val userId = readLine() ?: return

    val client = HttpClient {
        install(WebSockets)
    }

    runBlocking {
        client.webSocket(
            HttpMethod.Get,
            "localhost",
            8080,
            Paths.DEFAULT_URI,
            { headers.append(Headers.USER_IDENTIFIER, userId) }
        ) {
            val printIncomingJob = launch { printIncoming() }
            val sendUserInputJob = launch { sendUserInput() }

            sendUserInputJob.join()
            // Once user is done sending input, they should be done receiving, so:
            printIncomingJob.cancelAndJoin()
        }
    }
    client.close()
    println("KtoRC chat session has ended.")
}

internal suspend fun DefaultClientWebSocketSession.printIncoming() {
    try {
        for (message in incoming)
            println((message as Frame.Text).readText())
    } catch (e: Exception) {
        println("Exception: ${e.message}")
    }
}

internal suspend fun DefaultClientWebSocketSession.sendUserInput() {
    var stop = false
    do {
        try {
            val msg = readLine()

            if (msg != null) {
                send(msg)
                if (msg == "bye")
                    return
            }

        } catch (e: Exception) {
            println(e.message)
            stop = true
        }
    } while (!stop)
}
