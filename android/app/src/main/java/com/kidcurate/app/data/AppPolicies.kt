package com.kidcurate.app.data

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
    fun isLocalHost(url: String): Boolean {
        val sanitized = url.lowercase()
        return sanitized.contains("localhost") || sanitized.contains("10.0.2.2") || sanitized.contains("127.0.0.1")
    }

    fun getRecommendedProbeTimeoutSeconds(url: String): Int {
        return if (isLocalHost(url)) 4 else 25
    }

    fun sanitizeBaseUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}
