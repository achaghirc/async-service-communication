package com.tech.app

import io.github.damir.denis.tudor.ktor.server.rabbitmq.RabbitMQ
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.*
import io.ktor.server.application.*

fun Application.configureRabbitMQ() {

    install(RabbitMQ) {
        uri = "amqp://root:root@localhost:5672"
        defaultConnectionName = "default"
        connectionAttempts = 10
        attemptDelay = 10
        dispatcherThreadPollSize = 4
        tlsEnabled = false

    }

    rabbitmq {
        queueBind {
            queue = "chargepoint.authenticate_user.session#authenticate-app"
            exchange = "chargepoint.authenticate_user.session"
            routingKey = "chargepoint.authenticate_user.key"
            queueDeclare {
                queue = "chargepoint.authenticate_user.session#authenticate-app"
                durable = true
            }
            exchangeDeclare {
                exchange = "chargepoint.authenticate_user.session"
                type = "direct"
            }
        }
    }
}