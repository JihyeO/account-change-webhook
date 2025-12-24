package com.acw.webhook.domain.repository

import com.acw.webhook.model.Event

interface EventRepository {
    fun findByEventId(eventId: String): Event?
    fun insertIfNotExists(ev: Event): Boolean
    fun markProcessing(eventId: String)
    fun markDone(eventId: String)
    fun incrementAttempts(eventId: String, errorMessage: String?)
    fun updateStatus(eventId: String, status: String)
    fun enqueueWork(eventId: String)
}
