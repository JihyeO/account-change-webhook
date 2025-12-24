package com.acw.webhook.service

import com.acw.webhook.model.Event
import com.acw.webhook.domain.repository.EventRepository
import com.acw.webhook.domain.repository.AccountRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EventProcessor(
    private val eventRepo: EventRepository,
    private val accountRepo: AccountRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun process(eventId: String) {
        val ev = eventRepo.findByEventId(eventId) ?: run {
            log.warn("Event not found: {}", eventId)
            return
        }

        try {
            eventRepo.markProcessing(eventId)

            when (ev.type) {
                "EMAIL_FORWARDING_CHANGED" -> {
                    val value = parseEmailForwarding(ev.payload)
                    if (ev.accountKey != null && value != null) {
                        accountRepo.updateEmailForwarding(ev.accountKey, value)
                    }
                }
                "ACCOUNT_DELETED" -> {
                    if (ev.accountKey != null) {
                        accountRepo.updateAccountStatus(ev.accountKey, "DELETED")
                    }
                }
                else -> {
                    log.info("Unhandled event type: {}", ev.type)
                }
            }

            eventRepo.markDone(eventId)
        } catch (ex: Exception) {
            val msg = ex.message ?: "error"
            eventRepo.incrementAttempts(eventId, msg)
            eventRepo.updateStatus(eventId, "FAILED")
            log.error("Processing failed for {}: {}", eventId, msg)
        }
    }

    private fun parseEmailForwarding(payload: String?): Boolean? {
        if (payload == null) return null
        val regex = "\"emailForwarding\"\\s*:\\s*\"?(true|false)\"?".toRegex(RegexOption.IGNORE_CASE)
        val m = regex.find(payload) ?: return null
        return m.groupValues[1].lowercase() == "true"
    }
}
