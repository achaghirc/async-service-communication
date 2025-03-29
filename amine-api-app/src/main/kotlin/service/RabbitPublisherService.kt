package com.tech.app.service

import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.tech.app.model.AuthenticateUserSessionDTO
import io.ktor.server.config.*
import kotlinx.serialization.json.Json

import org.slf4j.LoggerFactory
class RabbitPublisherService(private val config: ApplicationConfig ) {

    private val log = LoggerFactory.getLogger(RabbitPublisherService::class.java)

    private val EXCHANGE_NAME : String by lazy { config.propertyOrNull("rabbitmq.authenticateExchange")?.getString() ?: "default.exchange" }
    private val QUEUE_NAME : String by lazy { config.propertyOrNull("rabbitmq.authenticateQueue")?.getString() ?: "default.queue" }
    private val ROUTING_KEY : String by lazy { config.propertyOrNull("rabbitmq.routingKey")?.toString() ?: "default.key" }

    private fun getConfigValue(envVar: String, configPath: String, default: String): String {
        return System.getenv(envVar) ?: config.propertyOrNull(configPath)?.getString() ?: default
    }
    private val isRabbitEnabled: Boolean = getConfigValue("RABBIT_CONNECTION", "rabbitmq.enabled", "false").toBoolean()

    private fun getRabbitMqUrl(): String {
        val rabbitHost = getConfigValue("RABBITMQ_HOST", "rabbitmq.host", "localhost")
        val rabbitPort = getConfigValue("RABBITMQ_PORT", "rabbitmq.port", "5672")
        val rabbitUser = getConfigValue("RABBITMQ_USER", "rabbitmq.user", "root")
        val rabbitPass = getConfigValue("RABBITMQ_PASS", "rabbitmq.pass", "root")
        return "amqp://$rabbitUser:$rabbitPass@$rabbitHost:$rabbitPort"
    }

    private val connectionFactory: ConnectionFactory by lazy {
        if(isRabbitEnabled) {
            ConnectionFactory().apply {
                setUri(getRabbitMqUrl())
            }
        } else {
            log.info("RabbitMQ is disabled, skipping connection creation")
            ConnectionFactory()
        }
    }
    private val connection: Connection? by lazy {
        if (isRabbitEnabled) {
            connectionFactory.newConnection().also {
                log.info("RabbitMQ connection established")
            }
        } else null
    }
    private val channel: Channel? by lazy {
        connection?.createChannel()?.also {
            it.exchangeDeclare(EXCHANGE_NAME,  BuiltinExchangeType.DIRECT, true)
            it.queueDeclare(QUEUE_NAME, true, false, false, null)
            it.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY)
            log.info("RabbitMQ Channel already ready")
        }
    }

    fun sendMessage(message: AuthenticateUserSessionDTO) {
        if (!isRabbitEnabled) {
            log.warn("RabbitMQ is disabled, message will not be sent.")
            return
        }
        try {
            val jsonMessage = Json.encodeToString(message)
            channel?.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, jsonMessage.toByteArray(Charsets.UTF_8))

            log.info("Message sent to exchange [$EXCHANGE_NAME] with routing key [$ROUTING_KEY]: $message")
        } catch (e : Exception) {
            log.error("Failed to send message to exchange [$EXCHANGE_NAME] with routing key [$ROUTING_KEY]: $message", e)
        }
    }

    fun close() {
        try {
            channel?.close()
            connection?.close()

            log.info("RabbitMQ connection closed")
        } catch (e: Exception) {
            log.error("Failed to close RabbitMQ connection: ${e.message}", e)
        }
    }
}