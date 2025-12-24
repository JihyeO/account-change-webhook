package com.acw.webhook.service

import com.acw.webhook.model.Event
import com.acw.webhook.domain.repository.EventRepository
import com.acw.webhook.util.HmacUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class EventService(
    private val repo: EventRepository,
        @Value("\${WEBHOOK_SECRET}")
    private val webhookSecret: String
) {

    fun handleWebhook(rawBody: ByteArray, signatureHeader: String?, eventIdHeader: String?): String {
        if (signatureHeader.isNullOrBlank() || eventIdHeader.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required headers")
        }

        val computed = HmacUtil.hmacSha256Hex(webhookSecret, rawBody)
        if (!HmacUtil.secureEquals(computed, signatureHeader)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid signature")
        }

        // Simplified payload parsing: in real app parse JSON for type/account/provider
        // For PoC, expect event type and accountKey in JSON fields
        val body = String(rawBody)
        val type = extractJsonField(body, "type") ?: "UNKNOWN"
        val accountKey = extractJsonField(body, "accountKey")
        val provider = extractJsonField(body, "provider")

        val event = Event(eventId = eventIdHeader, type = type, accountKey = accountKey, provider = provider, payload = body)

        val inserted = repo.insertIfNotExists(event)
        if (!inserted) {
            val existing = repo.findByEventId(event.eventId)
            return when (existing?.status) {
                "DONE" -> "이미 처리됨"
                "PROCESSING" -> "처리 중"
                else -> "accepted"
            }
        }

        // enqueue for processing
        repo.enqueueWork(event.eventId)
        return "accepted"
    }

    private fun extractJsonField(json: String, field: String): String? {
        // VERY simple extraction for PoC (not a JSON parser). Replace with Jackson in production.
        val regex = "\"$field\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        val match = regex.find(json) ?: return null
        return match.groupValues[1]
    }
}
