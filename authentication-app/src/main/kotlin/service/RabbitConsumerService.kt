package com.tech.app.service

import com.rabbitmq.client.*
import com.tech.app.model.AuthenticateUserSessionDTO
import io.ktor.server.config.*
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class RabbitConsumerService(private val config: ApplicationConfig) {

    private val log: Logger = LoggerFactory.getLogger(RabbitConsumerService::class.java)

    private val EXCHANGE_NAME: String by lazy { config.propertyOrNull("rabbitmq.authenticateExchange")?.getString() ?: "default.queue" }
    private val QUEUE_NAME: String by lazy { config.propertyOrNull("rabbitmq.authenticateQueue")?.getString() ?: "default.queue" }
    private val ROUTING_KEY: String by lazy { config.propertyOrNull("rabbitmq.routingKey")?.getString() ?: "default.key" }

    private fun getConfigValue(envVar: String, configPath: String, default: String): String {
        return System.getenv(envVar) ?: config.propertyOrNull(configPath)?.getString() ?: default
    }
    private val isRabbitEnabled: Boolean = getConfigValue("RABBIT_CONNECTION", "rabbitmq.enabled", "false").toBoolean()

    private fun getRabbitMqUrl(): String {
        val rabbitHost = getConfigValue("RABBITMQ_HOST", "rabbitmq.host", "localhost")
        val rabbitPort = getConfigValue("RABBITMQ_PORT", "rabbitmq.port", "5672")
        val rabbitUser = getConfigValue("RABBITMQ_USER", "rabbitmq.user", "guest")
        val rabbitPass = getConfigValue("RABBITMQ_PASS", "rabbitmq.pass", "guest")
        return "amqp://$rabbitUser:$rabbitPass@$rabbitHost:$rabbitPort"
    }

    private val connectionFactory: ConnectionFactory by lazy {
        if (!isRabbitEnabled) {
            log.info("RabbitMQ is disabled, skipping connection creation")
            ConnectionFactory()
        } else {
            ConnectionFactory().apply {
                setUri(getRabbitMqUrl())
            }
        }
    }

    private val connection: Connection? by lazy {
        if (isRabbitEnabled) {
            connectionFactory.newConnection().also {
                log.info("RabbitMQ connection already created")
            }
        } else null
    }
    private val channel: Channel? by lazy {
        connection?.createChannel()?.also {
            log.info("RabbitMQ Channel already ready")
        }
    }

    fun startListening() {
        try {
            channel.also {
                it?.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true)
                it?.queueDeclare(QUEUE_NAME, true, false, false, null)
                it?.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY)
                log.info("Rabbit Channel is ready")
            }
            val consumer = object : DefaultConsumer(channel) {
                override fun handleDelivery(
                    consumerTag: String?,
                    envelope: Envelope?,
                    properties: AMQP.BasicProperties?,
                    body: ByteArray?
                ) {
                    body?.let {
                        val message: AuthenticateUserSessionDTO = Json.decodeFromString<AuthenticateUserSessionDTO>(String(it, Charsets.UTF_8))
                        log.info("Message received from queue [$QUEUE_NAME] with routing key [$ROUTING_KEY]: $message")

                        AuthenticationService.authenticateUserSession(message)
                    }
                }
            }
            channel?.basicConsume(QUEUE_NAME, true, consumer)
            log.info("Listening to RabbitMQ messages on queue [$QUEUE_NAME]")
        } catch (e: Exception){
            log.error("Error while listening to RabbitMQ: ${e.message}", e)
        }
    }


    fun close() {
        try {
            channel?.close()
            connection?.close()
            log.info("RabbitMQ connection closed")
        } catch (e: Exception) {
            log.error("Error while closing RabbitMQ connection: ${e.message}", e)
        }
    }

}