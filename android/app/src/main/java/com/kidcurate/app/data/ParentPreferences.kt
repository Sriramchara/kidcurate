package com.kidcurate.app.data

import android.content.Context
import android.content.SharedPreferences

class ParentPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kidcurate_parent_prefs", Context.MODE_PRIVATE)

    var subtitlesEnabled: Boolean
        get() = prefs.getBoolean(KEY_SUBTITLES_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SUBTITLES_ENABLED, value).apply()

    var appTheme: String
        get() = prefs.getString(KEY_APP_THEME, "light") ?: "light"
        set(value) = prefs.edit().putString(KEY_APP_THEME, value).apply()

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, "http://localhost:8000") ?: "http://localhost:8000"
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()

    var parentAuthToken: String
        get() = prefs.getString(KEY_PARENT_AUTH_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARENT_AUTH_TOKEN, value).apply()

    var parentEmail: String
        get() = prefs.getString(KEY_PARENT_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARENT_EMAIL, value).apply()

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
        return "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.MONTH)}-${cal.get(java.util.Calendar.DAY_OF_MONTH)}"
    }

    var todayWatchedSeconds: Int
        get() {
            val savedDate = prefs.getString(KEY_LAST_WATCHED_DATE, "")
            val today = getTodayDate()
            return if (savedDate == today) {
                prefs.getInt(KEY_TODAY_WATCHED_SECONDS, 0)
            } else {
                0
            }
        }
        set(value) {
            val today = getTodayDate()
            prefs.edit()
                .putString(KEY_LAST_WATCHED_DATE, today)
                .putInt(KEY_TODAY_WATCHED_SECONDS, value)
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

    fun getCachedPlaylist(childId: String): String {
        return prefs.getString(KEY_CACHED_PLAYLIST_PREFIX + childId, "") ?: ""
    }

    fun setCachedPlaylist(childId: String, json: String) {
        prefs.edit().putString(KEY_CACHED_PLAYLIST_PREFIX + childId, json).apply()
    }

    fun isCurfewActive(): Boolean {
        if (!curfewEnabled) return false
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

    companion object {
        private const val KEY_SUBTITLES_ENABLED = "subtitles_enabled"
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_PARENT_AUTH_TOKEN = "parent_auth_token"
        private const val KEY_PARENT_EMAIL = "parent_email"
        private const val KEY_DAILY_SCREEN_TIME_MINUTES = "daily_screen_time_minutes"
        private const val KEY_CURFEW_ENABLED = "curfew_enabled"
        private const val KEY_CURFEW_START_HOUR = "curfew_start_hour"
        private const val KEY_CURFEW_END_HOUR = "curfew_end_hour"
        private const val KEY_LAST_WATCHED_DATE = "last_watched_date"
        private const val KEY_TODAY_WATCHED_SECONDS = "today_watched_seconds"
        private const val KEY_CACHED_PROFILES = "cached_profiles_json"
        private const val KEY_CACHED_PLAYLIST_PREFIX = "cached_playlist_json_"

        @Volatile
        private var instance: ParentPreferences? = null

        fun get(context: Context): ParentPreferences {
            return instance ?: synchronized(this) {
                instance ?: ParentPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}

