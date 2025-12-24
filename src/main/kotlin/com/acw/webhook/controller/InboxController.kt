package com.acw.webhook.controller

import com.acw.webhook.domain.repository.EventRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/inbox")
class InboxController(private val eventRepo: EventRepository) {

    @GetMapping("/events/{eventId}")
    fun getEvent(@PathVariable eventId: String): ResponseEntity<Any> {
        val ev = eventRepo.findByEventId(eventId)
        return if (ev == null) ResponseEntity.notFound().build()
        else ResponseEntity.ok(ev)
    }
}
