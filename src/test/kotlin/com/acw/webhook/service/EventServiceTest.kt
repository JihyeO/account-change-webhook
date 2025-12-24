package com.acw.webhook.service

import com.acw.webhook.model.Event
import com.acw.webhook.repository.JdbcEventRepository
import com.acw.webhook.util.HmacUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.web.server.ResponseStatusException

class EventServiceTest {

    @Test
    fun `signature validation success`() {
        val secret = "test-secret"
        val body = "{\"type\":\"PING\"}".toByteArray()
        val sig = HmacUtil.hmacSha256Hex(secret, body)

        val repo = Mockito.mock(JdbcEventRepository::class.java)
        val expected = Event(eventId = "evt-1", type = "PING", payload = String(body))
        Mockito.doReturn(true).`when`(repo).insertIfNotExists(expected)

        val svc = EventService(repo, secret)
        val res = svc.handleWebhook(body, sig, "evt-1")
        assertEquals("accepted", res)
    }

    @Test
    fun `signature validation failure`() {
        val secret = "test-secret"
        val body = "{\"type\":\"PING\"}".toByteArray()
        val sig = "bad"

        val repo = Mockito.mock(JdbcEventRepository::class.java)
        val svc = EventService(repo, secret)
        assertThrows(ResponseStatusException::class.java) {
            svc.handleWebhook(body, sig, "evt-1")
        }
    }

    @Test
    fun `duplicate event already done returns 이미 처리됨`() {
        val secret = "s"
        val body = "{\"type\":\"X\"}".toByteArray()
        val sig = HmacUtil.hmacSha256Hex(secret, body)

        val repo = Mockito.mock(JdbcEventRepository::class.java)
        val expected = Event(eventId = "evt-1", type = "X", payload = String(body))
        Mockito.doReturn(false).`when`(repo).insertIfNotExists(expected)
        val doneEvent = Event(eventId = "evt-1", type = "X", status = "DONE")
        Mockito.`when`(repo.findByEventId("evt-1")).thenReturn(doneEvent)

        val svc = EventService(repo, secret)
        val res = svc.handleWebhook(body, sig, "evt-1")
        assertEquals("이미 처리됨", res)
    }

    @Test
    fun `duplicate event processing returns 처리 중`() {
        val secret = "s"
        val body = "{\"type\":\"X\"}".toByteArray()
        val sig = HmacUtil.hmacSha256Hex(secret, body)

        val repo = Mockito.mock(JdbcEventRepository::class.java)
        val expected = Event(eventId = "evt-1", type = "X", payload = String(body))
        Mockito.doReturn(false).`when`(repo).insertIfNotExists(expected)
        val processingEvent = Event(eventId = "evt-1", type = "X", status = "PROCESSING")
        Mockito.`when`(repo.findByEventId("evt-1")).thenReturn(processingEvent)

        val svc = EventService(repo, secret)
        val res = svc.handleWebhook(body, sig, "evt-1")
        assertEquals("처리 중", res)
    }
}
