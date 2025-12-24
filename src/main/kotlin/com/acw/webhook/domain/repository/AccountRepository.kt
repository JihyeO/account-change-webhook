package com.acw.webhook.domain.repository

import com.acw.webhook.model.Account

interface AccountRepository {
    fun findByAccountKey(accountKey: String): Account?
    fun updateEmailForwarding(accountKey: String, value: Boolean): Int
    fun updateAccountStatus(accountKey: String, status: String): Int
}
