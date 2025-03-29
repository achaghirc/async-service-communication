package com.tech.app

import com.tech.app.service.RabbitConsumerService
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val environment = environment.config
    install(Koin){
        modules(appModule(environment))
    }
    val rabbitConsumerService = RabbitConsumerService(environment)
    launch { rabbitConsumerService.startListening() }

    configureSerialization()
    configureRouting()

    if (environment.propertyOrNull("rabbitmq.enabled")?.getString()?.toBoolean() == true) {
        Runtime.getRuntime().addShutdownHook(Thread {
            rabbitConsumerService.close()
        })
    }
}
