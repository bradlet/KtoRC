package com.ktorc.plugins

import io.ktor.features.*
import io.ktor.application.*

fun Application.configureHTTP() {
    install(ConditionalHeaders)
    install(HttpsRedirect) {
        // The port to redirect to. By default 443, the default HTTPS port.
        sslPort = 443
        // 301 Moved Permanently, or 302 Found redirect.
        permanentRedirect = true
    }

}
