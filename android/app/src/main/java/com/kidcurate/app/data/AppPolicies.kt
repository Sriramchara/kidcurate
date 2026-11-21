package com.kidcurate.app.data

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object ParentSessionPolicy {
    fun canReusePin(
        hasPin: Boolean,
        pinOwnerEmail: String,
        currentAccountEmail: String,
        signingInEmail: String,
    ): Boolean {
        if (!hasPin || signingInEmail.isBlank()) return false
        val owner = pinOwnerEmail.ifBlank { currentAccountEmail }
        return owner.isNotBlank() && owner.equals(signingInEmail, ignoreCase = true)
    }
}

enum class ParentLockType(val storageKey: String, val displayName: String) {
    PIN("pin", "4-Digit PIN"),
    PATTERN("pattern", "3x3 Pattern"),
    PASSWORD("password", "Password");

    companion object {
        fun fromStorageKey(key: String?): ParentLockType {
            return entries.firstOrNull { it.storageKey.equals(key, ignoreCase = true) } ?: PIN
        }
    }
}

object ParentLockPolicy {
    const val PIN_LENGTH = 4
    const val MIN_PATTERN_DOTS = 4
    const val MIN_PASSWORD_LENGTH = 6
    const val PBKDF2_ITERATIONS = 100_000

    fun encodePattern(dots: List<Int>): String {
        return dots.filter { it in 0..8 }.distinct().joinToString("-")
    }

    fun decodePattern(encoded: String): List<Int> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split("-").mapNotNull { it.toIntOrNull() }.filter { it in 0..8 }
    }

    fun isValidPin(pin: String): Boolean {
        return pin.length == PIN_LENGTH && pin.all { it.isDigit() }
    }

    fun isValidPattern(dots: List<Int>): Boolean {
        val valid = dots.filter { it in 0..8 }.distinct()
        return valid.size >= MIN_PATTERN_DOTS && valid.size == dots.size
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    fun validateCredential(type: ParentLockType, rawValue: String): String? {
        return when (type) {
            ParentLockType.PIN ->
                if (isValidPin(rawValue)) null else "PIN must be exactly $PIN_LENGTH digits."
            ParentLockType.PATTERN -> {
                val dots = decodePattern(rawValue)
                if (isValidPattern(dots)) null else "Connect at least $MIN_PATTERN_DOTS dots to create a pattern."
            }
            ParentLockType.PASSWORD ->
                if (isValidPassword(rawValue)) null else "Password must be at least $MIN_PASSWORD_LENGTH characters."
        }
    }

    /**
     * Resolves adding a dot to a 3x3 pattern grid (indices 0..8), automatically
     * connecting any skipped collinear midpoint node (e.g., dragging 0 -> 2 includes 1).
     */
    fun resolvePatternStep(currentDots: List<Int>, nextDot: Int): List<Int> {
        if (nextDot !in 0..8 || nextDot in currentDots) return currentDots
        val result = currentDots.toMutableList()
        val lastDot = currentDots.lastOrNull()
        if (lastDot != null) {
            val r1 = lastDot / 3
            val c1 = lastDot % 3
            val r2 = nextDot / 3
            val c2 = nextDot % 3
            if ((r1 + r2) % 2 == 0 && (c1 + c2) % 2 == 0) {
                val midDot = ((r1 + r2) / 2) * 3 + ((c1 + c2) / 2)
                if (midDot != lastDot && midDot != nextDot && midDot !in result) {
                    result.add(midDot)
                }
            }
        }
        result.add(nextDot)
        return result
    }

    fun hashSecretPbkdf2(secret: String, salt: ByteArray): String {
        if (secret.isBlank()) return ""
        val spec = javax.crypto.spec.PBEKeySpec(secret.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return "pbkdf2$$PBKDF2_ITERATIONS$${bytesToHex(salt)}$${bytesToHex(hash)}"
    }

    fun verifySecretPbkdf2(candidate: String, storedHash: String): Boolean {
        if (candidate.isBlank() || !storedHash.startsWith("pbkdf2$")) return false
        val parts = storedHash.split("$")
        if (parts.size < 4) return false
        val iterations = parts[1].toIntOrNull() ?: PBKDF2_ITERATIONS
        val salt = hexToBytes(parts[2])
        if (salt.isEmpty()) return false
        val expectedHashHex = parts[3]
        val spec = javax.crypto.spec.PBEKeySpec(candidate.toCharArray(), salt, iterations, 256)
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        val actualBytes = bytesToHex(hash).toByteArray(Charsets.UTF_8)
        val expectedBytes = expectedHashHex.toByteArray(Charsets.UTF_8)
        return java.security.MessageDigest.isEqual(actualBytes, expectedBytes)
    }

    fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        if (len == 0 || len % 2 != 0) return ByteArray(0)
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            val hi = Character.digit(hex[i], 16)
            val lo = Character.digit(hex[i + 1], 16)
            if (hi == -1 || lo == -1) return ByteArray(0)
            data[i / 2] = ((hi shl 4) + lo).toByte()
            i += 2
        }
        return data
    }
}

object ResponsiveLayoutPolicy {
    fun isLandscape(widthDp: Int, heightDp: Int): Boolean = widthDp > heightDp

    fun isTablet(widthDp: Int): Boolean = widthDp >= 600

    fun isTablet(widthDp: Int, heightDp: Int): Boolean =
        kotlin.math.min(widthDp, heightDp) >= 600

    fun isExpanded(widthDp: Int): Boolean = widthDp >= 840

    fun isCompactLandscape(widthDp: Int, heightDp: Int): Boolean =
        widthDp > heightDp && heightDp < 500

    fun isTabletLandscape(widthDp: Int, heightDp: Int): Boolean =
        widthDp > heightDp && kotlin.math.min(widthDp, heightDp) >= 600

    fun shouldUseTwoPanePin(widthDp: Int, heightDp: Int): Boolean =
        widthDp > heightDp

    fun shouldShowTwoPanePlaylist(widthDp: Int, heightDp: Int): Boolean =
        widthDp > heightDp

    fun playlistGridColumns(widthDp: Int, heightDp: Int): Int {
        val isLandscape = widthDp > heightDp
        val minDim = kotlin.math.min(widthDp, heightDp)
        return when {
            minDim >= 600 -> 2
            widthDp >= 720 && !isLandscape -> 2
            else -> 1
        }
    }

    fun pinButtonSizeDp(widthDp: Int, heightDp: Int): Int {
        val isLandscape = widthDp > heightDp
        val isTab = kotlin.math.min(widthDp, heightDp) >= 600
        return when {
            isLandscape && isTab -> 60
            isLandscape -> 46
            heightDp < 680 -> 64
            else -> 74
        }
    }
}

object CloudConnectionPolicy {
    const val LOCAL_PROBE_TIMEOUT_SECONDS = 4
    const val CLOUD_PROBE_TIMEOUT_SECONDS = 25

    private val RFC172_REGEX = Regex("""^172\.(1[6-9]|2[0-9]|3[0-1])\.""")

    fun isLocalHost(url: String): Boolean {
        val sanitized = url.trim().lowercase()
        val host = try {
            val candidate = if (sanitized.contains("://")) sanitized else "http://$sanitized"
            candidate.toHttpUrlOrNull()?.host
        } catch (_: Exception) {
            null
        } ?: sanitized.substringAfter("://").substringBefore("/").substringBefore(":")

        if (host.isNullOrBlank()) return false
        if (host == "localhost" || host == "127.0.0.1" || host == "10.0.2.2") return true
        if (host.startsWith("192.168.")) return true
        if (host.startsWith("10.")) return true
        if (RFC172_REGEX.containsMatchIn(host)) return true
        return false
    }

    fun getRecommendedProbeTimeoutSeconds(url: String): Int {
        return if (isLocalHost(url)) LOCAL_PROBE_TIMEOUT_SECONDS else CLOUD_PROBE_TIMEOUT_SECONDS
    }

    fun sanitizeBaseUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}
