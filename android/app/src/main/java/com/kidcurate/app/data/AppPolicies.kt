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
