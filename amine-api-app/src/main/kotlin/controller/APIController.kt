package com.tech.app.controller

import com.tech.app.model.AuthenticateUserSessionDTO
import com.tech.app.model.StartSessionDTO
import com.tech.app.service.RabbitPublisherService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import java.util.UUID

fun Route.apiControllerRoutes() {

    post("/start-session") {
        val request = call.receive<StartSessionDTO>()
        if (!request.isValid()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid parameters values on body request please check."))
            return@post
        }

        val sessionId = UUID.randomUUID().toString()
        val startSessionDTO = request.copy()
        val authenticateUserSession = AuthenticateUserSessionDTO(sessionId, startSessionDTO)

        call.respond(HttpStatusCode.Accepted, mapOf(
            "status" to "accepted",
            "message" to "Request is being processed asynchronously. The result will be sent to the provided callback URL."))

        call.launch {
            //Send the message to the RabbitMQ
            RabbitPublisherService.sendMessage(authenticateUserSession)
        }
    }

    /// ONLY FOR TESTING PURPOSES
    post("/callback-test"){
        val request = call.receive<String>()
        println("Callback test: $request")
        call.respond(HttpStatusCode.OK, mapOf("message" to "Callback test: $request"))
    }
}