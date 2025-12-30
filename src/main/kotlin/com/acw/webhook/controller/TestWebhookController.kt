package com.acw.webhook.controller

import com.acw.webhook.service.EventService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.parameters.RequestBody as OasRequestBody

@RestController
@RequestMapping("/webhooks")
class TestWebhookController(private val service: EventService) {

    @PostMapping("/account-changes/test")
    @Operation(summary = "Test receive webhook", description = "테스트용 엔드포인트: Swagger UI에서 바디/헤더를 입력해 호출")
    fun receiveWebhookTest(
        @Parameter(name = "X-Signature", description = "HMAC-SHA256 hex", required = false, `in` = ParameterIn.HEADER)
        @RequestHeader(value = "X-Signature", required = false) signature: String?,

        @Parameter(name = "X-Event-Id", description = "Event unique ID", required = false, `in` = ParameterIn.HEADER)
        @RequestHeader(value = "X-Event-Id", required = false) eventId: String?,

        @OasRequestBody(description = "Raw webhook JSON payload")
        @RequestBody body: String
    ): ResponseEntity<String> {
        val result = service.handleWebhook(body.toByteArray(), signature, eventId)
        return ResponseEntity.ok(result)
    }
}
