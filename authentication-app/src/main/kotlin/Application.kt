package com.tech.app

import com.tech.app.service.RabbitConsumerService
import io.ktor.server.application.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureRouting()
    RabbitConsumerService.startListening()

    Runtime.getRuntime().addShutdownHook(Thread {
        RabbitConsumerService.close()
    })
}
