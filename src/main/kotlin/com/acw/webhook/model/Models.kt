package com.acw.webhook.model

import java.time.OffsetDateTime

data class Event(
    val id: Long? = null,
    val eventId: String,
    val type: String,
    val accountKey: String? = null,
    val provider: String? = null,
    val payload: String? = null,
    val status: String = "RECEIVED",
    val attempts: Int = 0,
    val errorMessage: String? = null
)

data class Account(
    val id: Long? = null,
    val accountKey: String,
    val parentAccountKey: String? = null,
    val email: String? = null,
    val emailForwarding: Boolean = false,
    val status: String = "ACTIVE"
)
