package com.tech.app.service

import com.rabbitmq.client.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object RabbitConsumerService { // Singleton object

    private val log: Logger = LoggerFactory.getLogger(RabbitConsumerService::class.java)

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
            log.info("RabbitMQ Channel already ready")
        }
    }

    fun startListening() {
        try {
            val consumer = object : DefaultConsumer(channel) {
                override fun handleDelivery(
                    consumerTag: String?,
                    envelope: Envelope?,
                    properties: AMQP.BasicProperties?,
                    body: ByteArray?
                ) {
                    body?.let {
                        val message = String(it, Charsets.UTF_8)
                        log.info("Message received from exchange [$EXCHANGE_NAME] with routing key [$ROUTING_KEY]: $message")
                    }
                }
            }
            channel.basicConsume(QUEUE_NAME, true, consumer)
            log.info("Listening to RabbitMQ messages on queue [$QUEUE_NAME]")
        } catch (e: Exception){
            log.error("Error while listening to RabbitMQ: ${e.message}", e)
        }
    }


    fun close() {
        try {
            channel.close()
            connection.close()
            log.info("RabbitMQ connection closed")
        } catch (e: Exception) {
            log.error("Error while closing RabbitMQ connection: ${e.message}", e)
        }
    }

}