package com.tech.app.service

import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.tech.app.model.AuthenticateUserSessionDTO
import kotlinx.serialization.json.Json

import org.slf4j.LoggerFactory
object RabbitPublisherService {

    private val log = LoggerFactory.getLogger(RabbitPublisherService::class.java)
    private const val EXCHANGE_NAME = "chargepoint.authenticate_user.session"
    private const val QUEUE_NAME = "chargepoint.authenticate_user.session#authenticate-app"
    private const val ROUTING_KEY = "chargepoint.authenticate_user.key"
    private const val RABBIT_MQ_URL = "amqp://root:root@localhost:5672"

    private val connectionFactory: ConnectionFactory = ConnectionFactory().apply {
        setUri(RABBIT_MQ_URL)
    }
    private val connection: Connection by lazy {
        connectionFactory.newConnection().also {
            log.info("RabbitMQ connection already created")
        }
    }
    private val channel: Channel by lazy {
        connection.createChannel().also {
            it.exchangeDeclare(EXCHANGE_NAME,  BuiltinExchangeType.DIRECT, true)
            it.queueDeclare(QUEUE_NAME, true, false, false, null)
            it.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY)
            log.info("RabbitMQ Channel already ready")
        }
    }

    fun sendMessage(message: AuthenticateUserSessionDTO) {
        try {
            val jsonMessage = Json.encodeToString(message)
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, jsonMessage.toByteArray(Charsets.UTF_8))

            log.info("Message sent to exchange [$EXCHANGE_NAME] with routing key [$ROUTING_KEY]: $message")
        } catch (e : Exception) {
            log.error("Failed to send message to exchange [$EXCHANGE_NAME] with routing key [$ROUTING_KEY]: $message", e)
        }
    }

    fun close() {
        try {
            channel.close()
            connection.close()

            log.info("RabbitMQ connection closed")
        } catch (e: Exception) {
            log.error("Failed to close RabbitMQ connection: ${e.message}", e)
        }
    }
}