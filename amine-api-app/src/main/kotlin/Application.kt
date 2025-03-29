package com.tech.app

import com.tech.app.service.RabbitPublisherService
import io.ktor.server.application.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(Koin){
        modules(appModule(environment.config))
    }

    val rabbitService by inject<RabbitPublisherService>()
    configureRouting(rabbitService)
    configureSerialization()

    Runtime.getRuntime().addShutdownHook(Thread {
        rabbitService.close()
    })
}
