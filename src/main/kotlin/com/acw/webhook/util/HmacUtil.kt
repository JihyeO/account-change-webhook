package com.acw.webhook.util

import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

object HmacUtil {
    private const val HMAC_SHA256 = "HmacSHA256"

    fun hmacSha256Hex(secret: String, data: ByteArray): String {
        val mac = Mac.getInstance(HMAC_SHA256)
        val keySpec = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), HMAC_SHA256)
        mac.init(keySpec)
        val raw = mac.doFinal(data)
        return raw.joinToString("") { String.format("%02x", it and 0xff.toByte()) }
    }

    fun secureEquals(a: String, b: String): Boolean {
        val aa = a.toByteArray(StandardCharsets.UTF_8)
        val bb = b.toByteArray(StandardCharsets.UTF_8)
        return java.security.MessageDigest.isEqual(aa, bb)
    }
}
