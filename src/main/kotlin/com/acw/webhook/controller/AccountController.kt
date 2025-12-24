package com.acw.webhook.controller

import com.acw.webhook.domain.repository.AccountRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/accounts")
class AccountController(private val accountRepo: AccountRepository) {

    @GetMapping("/{accountKey}")
    fun getAccount(@PathVariable accountKey: String): ResponseEntity<Any> {
        val acc = accountRepo.findByAccountKey(accountKey)
        return if (acc == null) ResponseEntity.notFound().build()
        else ResponseEntity.ok(acc)
    }
}
