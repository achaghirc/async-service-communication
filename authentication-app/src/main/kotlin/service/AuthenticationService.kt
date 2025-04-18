package com.tech.app.service

import com.tech.app.model.AuthenticateResponseDTO
import com.tech.app.model.AuthenticateUserSessionDTO
import com.tech.app.model.StartSessionDTO
import com.tech.app.model.enums.StatusEnum
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

var sessions = mapOf(
    "123e4567-e89b-12d3-a456-426614174000" to listOf("ValidDriver123456-._~", "ValidDriver654321-._~"),
    "550e8400-e29b-41d4-a716-446655440000" to listOf("ValidDriver-._123456", "ValidDriver-._654321"),
    "550e8400-e29b-41d4-a716-446655440001" to listOf("UnknownDriver-._123456", "UnknownDriver-._654321")
) //stationId -> [driverToken1, driverToken2, ...]

object AuthenticationService {
    private val log: Logger = LoggerFactory.getLogger(AuthenticationService::class.java)

    fun authenticateUserSession(authenticationSessionDTO: AuthenticateUserSessionDTO) {
        val sessionId = authenticationSessionDTO.sessionId
        val startSession = authenticationSessionDTO.startSession
        // Run heavy work with a timeout of 5 seconds for some specific station to simulate the timeout feature
        val status: StatusEnum
        if (startSession.stationId == "550e8400-e29b-41d4-a716-446655440001") {
            log.info("Heavy work is needed for session $sessionId")
             status = runBlocking {
                withTimeoutOrNull(5000) {
                    heavyWork()
                    getStatusResponse(startSession)
                } ?: StatusEnum.UNKNOWN // If timeout occurs, return UNKNOWN
            }
        } else {
            status = getStatusResponse(startSession)
        }
        log.info("Session $sessionId status is $status")
        sendDecisionToCallback(startSession, status)
    }

    private suspend fun heavyWork() {
        log.info("Heavy work started")
        delay(6000)
        log.info("Heavy work finished")
    }

    private fun getStatusResponse(startSession: StartSessionDTO): StatusEnum {
        var status = StatusEnum.NOT_ALLOWED
        val stationId = startSession.stationId
        if (sessions.containsKey(stationId)) {
            if (sessions[stationId]!!.contains(startSession.driverId)) {
                status =  StatusEnum.ALLOWED
            }
        } else {
            status = StatusEnum.INVALID
        }
        return status
    }

    private fun sendDecisionToCallback(startSession: StartSessionDTO, status: StatusEnum) {
        // send decision to callbackUrl
        if (!startSession.isValid()) {
            log.error("Invalid start session")
            return
        }
        try {
            val url = URL(startSession.callbackUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true // to be able to write

            val callBody = AuthenticateResponseDTO(
                stationId = startSession.stationId,
                driverToken = startSession.driverId,
                status = status
            )

            val requestBody = Json.encodeToString(callBody).toByteArray()
            connection.outputStream.use { it.write(requestBody) }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                log.info("Decision sent to callbackUrl ${startSession.callbackUrl}")
            } else {
                log.error("Failed to send decision to callbackUrl ${startSession.callbackUrl}")
            }
        }catch (e: ConnectException) {
            log.error("Connection refused: Unable to reach callbackUrl ${startSession.callbackUrl}",e)
        } catch (e: SocketTimeoutException) {
            log.error("Connection timeout: Unable to reach callbackUrl ${startSession.callbackUrl}",e)
        } catch (e: Exception) {
            log.error("Failed to send decision to callbackUrl ${startSession.callbackUrl}", e)
        }
    }

}