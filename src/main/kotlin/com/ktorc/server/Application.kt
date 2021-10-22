package com.ktorc.server

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersionName
import com.ktorc.KtorcConstants
import com.ktorc.KtorcConstants.JDBC_URL_TEMPLATE
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.WebSockets
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import java.time.Duration

fun main() {

    /** Get secrets for database connection setup */
    val secretManager: SecretManagerServiceClient = SecretManagerServiceClient.create()
    // Returns a string representation of a secret value from GCP Secret Manager
    fun SecretManagerServiceClient.getSecretValue(secretName: String): String {
        return accessSecretVersion(
            SecretVersionName.of(
                KtorcConstants.PROJECT, secretName, KtorcConstants.SECRET_VERSION
            )
        ).payload.data.toStringUtf8()
    }
    val dbName = secretManager.getSecretValue("DB_NAME")
    val dbPassword = secretManager.getSecretValue("DB_PASS")
    val dbUser = secretManager.getSecretValue("DB_USER")
    val connectionName = secretManager.getSecretValue("CLOUD_SQL_CONNECTION_NAME")

    /** Setup database connection and manager */
    val jdbcUrl = JDBC_URL_TEMPLATE.format(dbName, connectionName, dbUser, dbPassword)
    val database = Database.connect(jdbcUrl, driver = "org.h2.Driver")
    val dbManager = DatabaseManager(database)

    embeddedServer(Netty, port = 8080, host = "localhost") {
        setupWebSockets(dbManager)
    }.start(wait = true)
}

fun Application.setupWebSockets(
    databaseManager: DatabaseManager
) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        getChat(databaseManager)
    }
}
