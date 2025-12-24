package com.acw.webhook.service

import com.acw.webhook.model.Event
import com.acw.webhook.repository.JdbcEventRepository
import com.acw.webhook.repository.JdbcAccountRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.ArgumentCaptor

class EventProcessorTest {

    @Test
    fun `EMAIL_FORWARDING_CHANGED updates account emailForwarding and marks done`() {
        val eventId = "evt-efc"
        val payload = "{\"emailForwarding\":\"true\"}"
        val ev = Event(eventId = eventId, type = "EMAIL_FORWARDING_CHANGED", accountKey = "acct-1", payload = payload)

        val eventRepo = Mockito.mock(JdbcEventRepository::class.java)
        val accountRepo = Mockito.mock(JdbcAccountRepository::class.java)

        Mockito.`when`(eventRepo.findByEventId(eventId)).thenReturn(ev)

        val processor = EventProcessor(eventRepo, accountRepo)
        processor.process(eventId)

        Mockito.verify(eventRepo).markProcessing(eventId)
        Mockito.verify(accountRepo).updateEmailForwarding("acct-1", true)
        Mockito.verify(eventRepo).markDone(eventId)
    }

    @Test
    fun `ACCOUNT_DELETED sets account status to DELETED and marks done`() {
        val eventId = "evt-del"
        val ev = Event(eventId = eventId, type = "ACCOUNT_DELETED", accountKey = "acct-2")

        val eventRepo = Mockito.mock(JdbcEventRepository::class.java)
        val accountRepo = Mockito.mock(JdbcAccountRepository::class.java)

        Mockito.`when`(eventRepo.findByEventId(eventId)).thenReturn(ev)

        val processor = EventProcessor(eventRepo, accountRepo)
        processor.process(eventId)

        Mockito.verify(eventRepo).markProcessing(eventId)
        Mockito.verify(accountRepo).updateAccountStatus("acct-2", "DELETED")
        Mockito.verify(eventRepo).markDone(eventId)
    }

    @Test
    fun `processing failure increments attempts and marks FAILED`() {
        val eventId = "evt-fail"
        val ev = Event(eventId = eventId, type = "ACCOUNT_DELETED", accountKey = "acct-3")

        val eventRepo = Mockito.mock(JdbcEventRepository::class.java)
        val accountRepo = Mockito.mock(JdbcAccountRepository::class.java)

        Mockito.`when`(eventRepo.findByEventId(eventId)).thenReturn(ev)
        Mockito.doThrow(RuntimeException("DB lock")).`when`(eventRepo).markProcessing(eventId)

        val processor = EventProcessor(eventRepo, accountRepo)
        processor.process(eventId)

        Mockito.verify(eventRepo).updateStatus(eventId, "FAILED")
    }
}
