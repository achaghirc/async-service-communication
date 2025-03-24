package com.tech.app

import com.tech.app.service.RabbitConsumerService
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
    RabbitConsumerService.startListening()

    Runtime.getRuntime().addShutdownHook(Thread {
        RabbitConsumerService.close()
    })
}
