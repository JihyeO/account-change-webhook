package com.acw.webhook.controller

import com.acw.webhook.service.EventService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/webhooks")
class WebhookController(private val service: EventService) {

    @PostMapping("/account-changes")
    fun receiveWebhook(request: HttpServletRequest): ResponseEntity<String> {
        val signature = request.getHeader("X-Signature")
        val eventId = request.getHeader("X-Event-Id")
        val body = request.inputStream.readAllBytes()
        val result = service.handleWebhook(body, signature, eventId)
        return ResponseEntity.ok(result)
    }
}
