package com.tech.app

import com.tech.app.controller.apiControllerRoutes
import com.tech.app.service.RabbitPublisherService
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(rabbitPublisherService: RabbitPublisherService) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")

        apiControllerRoutes(rabbitPublisherService)
    }
}
