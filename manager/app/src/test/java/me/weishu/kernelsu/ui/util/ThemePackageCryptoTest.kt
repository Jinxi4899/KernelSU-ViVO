package me.weishu.kernelsu.ui.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePackageCryptoTest {

    @Test
    fun encryptDecryptRoundTrip() {
        val payload = ByteArray(1024) { index -> (index % 251).toByte() }
        val encrypted = ThemePackageCrypto.encrypt(payload)
        assertTrue(ThemePackageCrypto.hasValidHeader(encrypted))
        assertArrayEquals(payload, ThemePackageCrypto.decrypt(encrypted))
    }

    @Test
    fun streamingRoundTrip() {
        val payload = "theme.json".repeat(4096).toByteArray()
        val sink = ByteArrayOutputStream()
        ThemePackageCrypto.encryptingStream(sink).use { it.write(payload) }
        val encrypted = sink.toByteArray()
        assertTrue(ThemePackageCrypto.hasValidHeader(encrypted))
        val decrypted = ThemePackageCrypto.decryptingStream(
            ByteArrayInputStream(encrypted),
        ).use { it.readBytes() }
        assertArrayEquals(payload, decrypted)
    }

    @Test(expected = IllegalStateException::class)
    fun headerMismatchIsRejected() {
        val encrypted = ThemePackageCrypto.encrypt("data".toByteArray())
        encrypted[0] = 'X'.code.toByte()
        ThemePackageCrypto.decrypt(encrypted)
    }

    @Test(expected = Exception::class)
    fun tamperedPayloadFailsTagVerification() {
        val encrypted = ThemePackageCrypto.encrypt("data".toByteArray())
        encrypted[encrypted.lastIndex] = (encrypted.last() + 1).toByte()
        ThemePackageCrypto.decrypt(encrypted)
    }
}
