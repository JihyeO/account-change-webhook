package com.acw.webhook.domain.model

data class DomainEvent(
    val eventId: String,
    val type: String,
    val accountKey: String? = null,
    val provider: String? = null,
    val payload: String? = null
)
