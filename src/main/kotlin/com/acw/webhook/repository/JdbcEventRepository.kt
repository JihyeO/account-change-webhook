package com.acw.webhook.repository

import com.acw.webhook.domain.repository.EventRepository

import com.acw.webhook.model.Event
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class JdbcEventRepository(private val jdbc: JdbcTemplate) : EventRepository {

    private val mapper = RowMapper { rs: ResultSet, _ ->
        Event(
            id = rs.getLong("id"),
            eventId = rs.getString("event_id"),
            type = rs.getString("type"),
            accountKey = rs.getString("account_key"),
            provider = rs.getString("provider"),
            payload = rs.getString("payload"),
            status = rs.getString("status") ?: "RECEIVED",
            attempts = rs.getInt("attempts"),
            errorMessage = rs.getString("error_message")
        )
    }

    override fun findByEventId(eventId: String): Event? {
        val sql = "SELECT * FROM events WHERE event_id = ? LIMIT 1"
        return try {
            jdbc.queryForObject(sql, mapper, eventId)
        } catch (e: DataAccessException) {
            null
        }
    }

    override fun insertIfNotExists(ev: Event): Boolean {
        val sql = "INSERT OR IGNORE INTO events (event_id, type, account_key, provider, payload, status, attempts) VALUES (?, ?, ?, ?, ?, ?, ?)"
        val updated = jdbc.update(sql, ev.eventId, ev.type, ev.accountKey, ev.provider, ev.payload, ev.status, ev.attempts)
        return updated > 0
    }

    override fun markProcessing(eventId: String) {
        val sql = "UPDATE events SET status = 'PROCESSING' WHERE event_id = ?"
        jdbc.update(sql, eventId)
    }

    override fun markDone(eventId: String) {
        val sql = "UPDATE events SET status = 'DONE' WHERE event_id = ?"
        jdbc.update(sql, eventId)
    }

    override fun incrementAttempts(eventId: String, errorMessage: String?) {
        val sql = "UPDATE events SET attempts = attempts + 1, error_message = ?, updated_at = CURRENT_TIMESTAMP WHERE event_id = ?"
        jdbc.update(sql, errorMessage, eventId)
    }

    override fun updateStatus(eventId: String, status: String) {
        val sql = "UPDATE events SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE event_id = ?"
        jdbc.update(sql, status, eventId)
    }

    override fun enqueueWork(eventId: String) {
        val sql = "UPDATE events SET status = 'QUEUED' WHERE event_id = ?"
        jdbc.update(sql, eventId)
    }
}
