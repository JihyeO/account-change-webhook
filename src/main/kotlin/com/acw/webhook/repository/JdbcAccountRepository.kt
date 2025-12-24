package com.acw.webhook.repository

import com.acw.webhook.domain.repository.AccountRepository

import com.acw.webhook.model.Account
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class JdbcAccountRepository(private val jdbc: JdbcTemplate) : AccountRepository {

    private val mapper = RowMapper { rs: ResultSet, _ ->
        Account(
            id = rs.getLong("id"),
            accountKey = rs.getString("account_key"),
            parentAccountKey = rs.getString("parent_account_key"),
            email = rs.getString("email"),
            emailForwarding = rs.getInt("email_forwarding") != 0,
            status = rs.getString("status")
        )
    }

    override fun findByAccountKey(accountKey: String): Account? {
        val sql = "SELECT * FROM accounts WHERE account_key = ? LIMIT 1"
        return try {
            jdbc.queryForObject(sql, mapper, accountKey)
        } catch (e: DataAccessException) {
            null
        }
    }

    override fun updateEmailForwarding(accountKey: String, value: Boolean): Int {
        val sql = "UPDATE accounts SET email_forwarding = ?, updated_at = datetime('now') WHERE account_key = ?"
        return jdbc.update(sql, if (value) 1 else 0, accountKey)
    }

    override fun updateAccountStatus(accountKey: String, status: String): Int {
        val sql = "UPDATE accounts SET status = ?, updated_at = datetime('now') WHERE account_key = ?"
        return jdbc.update(sql, status, accountKey)
    }
}
