package com.kidcurate.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ParentPreferences(context: Context) {
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context.applicationContext,
            "kidcurate_parent_encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e("ParentPreferences", "EncryptedSharedPreferences initialization failed, falling back", e)
        context.getSharedPreferences("kidcurate_parent_prefs", Context.MODE_PRIVATE)
    }

    var subtitlesEnabled: Boolean
        get() = prefs.getBoolean(KEY_SUBTITLES_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SUBTITLES_ENABLED, value).apply()

    var earlyReaderSubtitles: Boolean
        get() = prefs.getBoolean(KEY_EARLY_READER_SUBTITLES, true)
        set(value) = prefs.edit().putBoolean(KEY_EARLY_READER_SUBTITLES, value).apply()

    var videoQuality: String
        get() = prefs.getString(KEY_VIDEO_QUALITY, "auto") ?: "auto"
        set(value) = prefs.edit().putString(KEY_VIDEO_QUALITY, value).apply()

    var requireChildPin: Boolean
        get() = prefs.getBoolean(KEY_REQUIRE_CHILD_PIN, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUIRE_CHILD_PIN, value).apply()

    var parentPin: String
        get() = prefs.getString(KEY_PARENT_PIN, "") ?: ""
        set(value) {
            val toStore = if (value.isBlank() || value.startsWith("pbkdf2$") || value.startsWith("sha256$")) value else hashPinPbkdf2(value)
            prefs.edit().putString(KEY_PARENT_PIN, toStore).apply()
        }

    private fun getOrCreatePinSalt(): ByteArray {
        val storedSaltHex = prefs.getString("parent_pin_salt", "") ?: ""
        if (storedSaltHex.isNotBlank() && storedSaltHex.length == 32) {
            return hexToBytes(storedSaltHex)
        }
        val salt = ByteArray(16)
        java.security.SecureRandom().nextBytes(salt)
        val saltHex = bytesToHex(salt)
        prefs.edit().putString("parent_pin_salt", saltHex).apply()
        return salt
    }

    private fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    fun hashPinPbkdf2(pin: String, salt: ByteArray = getOrCreatePinSalt()): String {
        if (pin.isBlank()) return ""
        val spec = javax.crypto.spec.PBEKeySpec(pin.toCharArray(), salt, 100_000, 256)
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return "pbkdf2$100000$${bytesToHex(salt)}$${bytesToHex(hash)}"
    }

    private fun hashPinLegacySha256(pin: String): String {
        if (pin.isBlank()) return ""
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest("kidcurate_salt_${pin}".toByteArray(Charsets.UTF_8))
        return "sha256$" + bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String): String = hashPinPbkdf2(pin)

    fun getPinLockoutRemainingSeconds(): Int {
        val lockoutUntil = prefs.getLong("parent_pin_lockout_until", 0L)
        val now = System.currentTimeMillis()
        return if (lockoutUntil > now) (((lockoutUntil - now) / 1000) + 1).toInt() else 0
    }

    fun isPinLockedOut(): Boolean = getPinLockoutRemainingSeconds() > 0

    private fun recordFailedPinAttempt() {
        val currentFailed = prefs.getInt("parent_pin_failed_attempts", 0) + 1
        val editor = prefs.edit().putInt("parent_pin_failed_attempts", currentFailed)
        if (currentFailed >= 5) {
            val multiplier = 1 shl (currentFailed - 5).coerceAtMost(5)
            val lockoutMs = 30_000L * multiplier
            editor.putLong("parent_pin_lockout_until", System.currentTimeMillis() + lockoutMs)
        }
        editor.apply()
    }

    private fun recordSuccessfulPinAttempt() {
        prefs.edit()
            .putInt("parent_pin_failed_attempts", 0)
            .putLong("parent_pin_lockout_until", 0L)
            .apply()
    }

    fun verifyParentPin(candidatePin: String): Boolean {
        if (candidatePin.isBlank() || isPinLockedOut()) return false
        val stored = parentPin
        if (stored.isBlank()) return false

        var matches = false
        if (stored.startsWith("pbkdf2$")) {
            val parts = stored.split("$")
            if (parts.size >= 4) {
                val salt = hexToBytes(parts[2])
                val expectedHashHex = parts[3]
                val spec = javax.crypto.spec.PBEKeySpec(candidatePin.toCharArray(), salt, 100_000, 256)
                val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                val hash = factory.generateSecret(spec).encoded
                matches = bytesToHex(hash) == expectedHashHex
            }
        } else if (stored.startsWith("sha256$")) {
            matches = (stored == hashPinLegacySha256(candidatePin))
            if (matches) {
                parentPin = candidatePin
            }
        } else if (stored == candidatePin) {
            matches = true
            parentPin = candidatePin
        }

        if (matches) {
            recordSuccessfulPinAttempt()
            return true
        } else {
            recordFailedPinAttempt()
            return false
        }
    }

    var parentPinOwnerEmail: String
        get() = prefs.getString(KEY_PARENT_PIN_OWNER_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARENT_PIN_OWNER_EMAIL, value.trim().lowercase()).apply()

    val hasParentPin: Boolean
        get() = parentPin.isNotBlank()

    val isParentLoggedIn: Boolean
        get() = parentAuthToken.isNotBlank()

    var hasSeenOnboardingTour: Boolean
        get() = prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, value).apply()

    fun clearParentSession() {
        if (hasParentPin && parentPinOwnerEmail.isBlank() && parentEmail.isNotBlank()) {
            parentPinOwnerEmail = parentEmail
        }
        parentAuthToken = ""
        parentRefreshToken = ""
        parentEmail = ""
        parentName = ""
        cachedProfilesJson = ""
    }

    /** Clears account-owned secrets and cached child data after permanent account deletion. */
    fun clearAccountData() {
        val editor = prefs.edit()
        prefs.all.keys
            .filter {
                it.startsWith(KEY_CHILD_PIN_PREFIX) ||
                    it.startsWith(KEY_CACHED_PLAYLIST_PREFIX) ||
                    it.startsWith("video_pos_") ||
                    it.startsWith("today_watched_seconds_")
            }
            .forEach(editor::remove)
        editor
            .remove(KEY_PARENT_AUTH_TOKEN)
            .remove(KEY_PARENT_REFRESH_TOKEN)
            .remove(KEY_PARENT_EMAIL)
            .remove(KEY_PARENT_NAME)
            .remove(KEY_PARENT_PIN)
            .remove(KEY_PARENT_PIN_OWNER_EMAIL)
            .remove(KEY_CACHED_PROFILES)
            .remove(KEY_RECENT_SEARCHES)
            .remove(KEY_LAST_WATCHED_DATE)
            .remove(KEY_TODAY_WATCHED_SECONDS)
            .remove(KEY_LAST_RECORDED_TIMESTAMP)
            .remove(KEY_DAILY_SCREEN_TIME_MINUTES)
            .remove(KEY_CURFEW_ENABLED)
            .remove(KEY_CURFEW_START_HOUR)
            .remove(KEY_CURFEW_END_HOUR)
            .remove(KEY_REQUIRE_CHILD_PIN)
            .apply()
    }

    var appTheme: String
        get() = prefs.getString(KEY_APP_THEME, "light") ?: "light"
        set(value) = prefs.edit().putString(KEY_APP_THEME, value).apply()

    var serverUrl: String
        get() {
            val url = prefs.getString(KEY_SERVER_URL, "") ?: ""
            if (url.isBlank() || url.contains("127.0.0.1") || (!com.kidcurate.app.BuildConfig.DEBUG && url.contains("localhost"))) {
                val defaultUrl = com.kidcurate.app.BuildConfig.API_BASE_URL
                prefs.edit().putString(KEY_SERVER_URL, defaultUrl).apply()
                return defaultUrl
            }
            return url
        }
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()

    var parentAuthToken: String
        get() = prefs.getString(KEY_PARENT_AUTH_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARENT_AUTH_TOKEN, value).apply()

    var parentRefreshToken: String
        get() = prefs.getString(KEY_PARENT_REFRESH_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARENT_REFRESH_TOKEN, value).apply()

    var parentEmail: String
        get() = prefs.getString(KEY_PARENT_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARENT_EMAIL, value).apply()

    var parentName: String
        get() = prefs.getString(KEY_PARENT_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARENT_NAME, value).apply()

    val googleClientId: String
        get() = com.kidcurate.app.BuildConfig.GOOGLE_CLIENT_ID

    var dailyScreenTimeMinutes: Int
        get() = prefs.getInt(KEY_DAILY_SCREEN_TIME_MINUTES, 45) // 0 means unlimited
        set(value) = prefs.edit().putInt(KEY_DAILY_SCREEN_TIME_MINUTES, value).apply()

    var curfewEnabled: Boolean
        get() = prefs.getBoolean(KEY_CURFEW_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_CURFEW_ENABLED, value).apply()

    var curfewStartHour: Int
        get() = prefs.getInt(KEY_CURFEW_START_HOUR, 20) // 8:00 PM
        set(value) = prefs.edit().putInt(KEY_CURFEW_START_HOUR, value).apply()

    var curfewEndHour: Int
        get() = prefs.getInt(KEY_CURFEW_END_HOUR, 7) // 7:00 AM
        set(value) = prefs.edit().putInt(KEY_CURFEW_END_HOUR, value).apply()

    private fun getTodayDate(): String {
        val cal = java.util.Calendar.getInstance()
        return "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.MONTH) + 1}-${cal.get(java.util.Calendar.DAY_OF_MONTH)}"
    }

    var todayWatchedSeconds: Int
        get() {
            val savedDate = prefs.getString(KEY_LAST_WATCHED_DATE, "")
            val today = getTodayDate()
            val now = System.currentTimeMillis()
            val lastRecorded = prefs.getLong(KEY_LAST_RECORDED_TIMESTAMP, 0L)
            
            // Anti-Tamper: If device clock was rolled backwards by more than 5 minutes, retain previous watch time!
            if (lastRecorded > 0L && now < lastRecorded - (5 * 60 * 1000L)) {
                return prefs.getInt(KEY_TODAY_WATCHED_SECONDS, 0)
            }

            return if (savedDate == today) {
                prefs.getInt(KEY_TODAY_WATCHED_SECONDS, 0)
            } else {
                0
            }
        }
        set(value) {
            val today = getTodayDate()
            val now = System.currentTimeMillis()
            val lastRecorded = prefs.getLong(KEY_LAST_RECORDED_TIMESTAMP, 0L)
            val toRecord = if (now > lastRecorded) now else lastRecorded
            prefs.edit()
                .putString(KEY_LAST_WATCHED_DATE, today)
                .putInt(KEY_TODAY_WATCHED_SECONDS, value)
                .putLong(KEY_LAST_RECORDED_TIMESTAMP, toRecord)
                .apply()
        }

    fun addWatchedSeconds(seconds: Int) {
        todayWatchedSeconds += seconds
    }

    fun resetTodayWatched() {
        todayWatchedSeconds = 0
    }

    var cachedProfilesJson: String
        get() = prefs.getString(KEY_CACHED_PROFILES, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CACHED_PROFILES, value).apply()

    fun getChildLocalPin(childId: String): String {
        return ""
    }

    fun setChildLocalPin(childId: String, pin: String) {
        // Plaintext PIN storage removed for security. Child PINs are verified securely via ApiClient.
    }

    fun getCachedPlaylist(childId: String): String {
        return prefs.getString(KEY_CACHED_PLAYLIST_PREFIX + childId, "") ?: ""
    }

    fun setCachedPlaylist(childId: String, json: String) {
        prefs.edit().putString(KEY_CACHED_PLAYLIST_PREFIX + childId, json).apply()
    }

    fun getVideoProgress(youtubeId: String): Float {
        if (youtubeId.isBlank()) return 0f
        return prefs.getFloat("video_pos_$youtubeId", 0f)
    }

    fun setVideoProgress(youtubeId: String, seconds: Float) {
        if (youtubeId.isBlank()) return
        prefs.edit().putFloat("video_pos_$youtubeId", seconds).apply()
    }

    fun clearVideoProgress(youtubeId: String) {
        if (youtubeId.isBlank()) return
        prefs.edit().remove("video_pos_$youtubeId").apply()
    }

    fun isCurfewActive(): Boolean {
        if (!curfewEnabled) return false
        if (curfewStartHour == curfewEndHour) return true
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return if (curfewStartHour > curfewEndHour) {
            // Over midnight, e.g. 20 (8 PM) to 7 (7 AM)
            currentHour >= curfewStartHour || currentHour < curfewEndHour
        } else {
            currentHour in curfewStartHour until curfewEndHour
        }
    }

    fun isDailyLimitReached(): Boolean {
        if (dailyScreenTimeMinutes <= 0) return false
        return todayWatchedSeconds >= dailyScreenTimeMinutes * 60
    }

    fun getRecentSearches(): List<String> {
        val raw = prefs.getString(KEY_RECENT_SEARCHES, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = org.json.JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val item = arr.optString(i, "")
                if (item.isNotBlank()) list.add(item)
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        val current = getRecentSearches().toMutableList()
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        current.add(0, trimmed)
        val limited = current.take(15)
        val arr = org.json.JSONArray()
        limited.forEach { arr.put(it) }
        prefs.edit().putString(KEY_RECENT_SEARCHES, arr.toString()).apply()
    }

    fun removeRecentSearch(query: String) {
        val current = getRecentSearches().toMutableList()
        current.removeAll { it.equals(query.trim(), ignoreCase = true) }
        val arr = org.json.JSONArray()
        current.forEach { arr.put(it) }
        prefs.edit().putString(KEY_RECENT_SEARCHES, arr.toString()).apply()
    }

    fun clearRecentSearches() {
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply()
    }

    companion object {
        private const val KEY_SUBTITLES_ENABLED = "subtitles_enabled"
        private const val KEY_EARLY_READER_SUBTITLES = "early_reader_subtitles"
        private const val KEY_RECENT_SEARCHES = "recent_searches_json"
        private const val KEY_VIDEO_QUALITY = "video_quality"
        private const val KEY_REQUIRE_CHILD_PIN = "require_child_pin"
        private const val KEY_PARENT_PIN = "parent_pin"
        private const val KEY_PARENT_PIN_OWNER_EMAIL = "parent_pin_owner_email"
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_PARENT_AUTH_TOKEN = "parent_auth_token"
        private const val KEY_PARENT_REFRESH_TOKEN = "parent_refresh_token"
        private const val KEY_PARENT_EMAIL = "parent_email"
        private const val KEY_PARENT_NAME = "parent_name"
        private const val KEY_DAILY_SCREEN_TIME_MINUTES = "daily_screen_time_minutes"
        private const val KEY_CURFEW_ENABLED = "curfew_enabled"
        private const val KEY_CURFEW_START_HOUR = "curfew_start_hour"
        private const val KEY_CURFEW_END_HOUR = "curfew_end_hour"
        private const val KEY_LAST_WATCHED_DATE = "last_watched_date"
        private const val KEY_LAST_RECORDED_TIMESTAMP = "last_recorded_wall_time_ms"
        private const val KEY_TODAY_WATCHED_SECONDS = "today_watched_seconds"
        private const val KEY_CACHED_PROFILES = "cached_profiles_json"
        private const val KEY_CACHED_PLAYLIST_PREFIX = "cached_playlist_json_"
        private const val KEY_CHILD_PIN_PREFIX = "child_local_pin_"
        private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding_tour"

        @Volatile
        private var instance: ParentPreferences? = null

        fun get(context: Context): ParentPreferences {
            return instance ?: synchronized(this) {
                instance ?: ParentPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}

