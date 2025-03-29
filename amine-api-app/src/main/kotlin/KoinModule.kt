package com.tech.app

import com.tech.app.service.RabbitPublisherService
import io.ktor.server.config.*
import org.koin.dsl.module

fun appModule(config: ApplicationConfig) = module {
    single<ApplicationConfig> { config }
    single<RabbitPublisherService> {
        RabbitPublisherService(get())
    }
}