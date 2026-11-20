package com.kidcurate.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kidcurate.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AppScreen {
    object ChildSelect : AppScreen()           // Choose which child profile
    object ParentZone : AppScreen()            // In-app Parent curation and search
    data class PinEntry(
        val childId: String,
        val childName: String,
        val childAvatar: String,
    ) : AppScreen()
    data class Playlist(
        val childId: String,
        val childName: String,
        val childAvatar: String,
    ) : AppScreen()
    data class Player(
        val childId: String,
        val childName: String,
        val childAvatar: String,
        val startIndex: Int,
    ) : AppScreen()
    data class Completion(
        val childName: String,
        val childAvatar: String,
        val videosWatched: Int,
        val totalMinutes: Int,
    ) : AppScreen()
}

data class KidAppState(
    val screen: AppScreen = AppScreen.ChildSelect,
    val availableProfiles: List<ChildProfileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val playlist: PlaylistData? = null,
    val childName: String = "",
    val childAvatar: String = "compass",
    val childId: String = "",
    val isChildLockActive: Boolean = false,
)

class KidViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(KidAppState())
    val state: StateFlow<KidAppState> = _state
    private val parentPrefs = ParentPreferences.get(application)
    private val gson = Gson()

    private var loadProfilesJob: kotlinx.coroutines.Job? = null

    init {
        ApiClient.onSessionExpired = {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                _state.value = KidAppState(
                    screen = AppScreen.ChildSelect,
                    error = "Your parent session ended. Sign in with Google to continue.",
                )
            }
        }
        loadProfiles()
    }

    fun loadProfiles() {
        loadProfilesJob?.cancel()
        loadProfilesJob = viewModelScope.launch {
            if (!parentPrefs.isParentLoggedIn) {
                ApiClient.authToken = ""
                _state.value = KidAppState(screen = AppScreen.ChildSelect)
                return@launch
            }
            ApiClient.authToken = parentPrefs.parentAuthToken
            if (parentPrefs.serverUrl.isNotBlank()) {
                ApiClient.updateBaseUrl(parentPrefs.serverUrl)
            }
            val currentScreen = _state.value.screen
            val retainedScreen = if (currentScreen is AppScreen.ChildSelect) AppScreen.ChildSelect else currentScreen
            // Restore cached profiles only while an authenticated parent session exists.
            if (parentPrefs.cachedProfilesJson.isNotBlank()) {
                try {
                    val listType = object : TypeToken<List<ChildProfileItem>>() {}.type
                    val cached: List<ChildProfileItem> = gson.fromJson(parentPrefs.cachedProfilesJson, listType)
                    if (cached.isNotEmpty()) {
                        _state.value = _state.value.copy(
                            availableProfiles = cached,
                            isLoading = false,
                            error = null,
                            screen = retainedScreen,
                        )
                    }
                } catch (_: Exception) {}
            } else {
                _state.value = _state.value.copy(isLoading = true, error = null)
            }

            // 2. Fetch live profiles: connect to production cloud server or saved server URL
            var fetchedProfiles: List<ChildProfileItem>? = null
            val primaryUrl = parentPrefs.serverUrl.ifBlank { com.kidcurate.app.BuildConfig.API_BASE_URL }
            val candidateUrls = buildList {
                if (primaryUrl.isNotBlank() && !primaryUrl.contains("127.0.0.1")) {
                    add(primaryUrl)
                }
                add(com.kidcurate.app.BuildConfig.API_BASE_URL)
                if (com.kidcurate.app.BuildConfig.DEBUG) {
                    add("http://192.168.0.107:8000/")
                    add("http://localhost:8000/")
                }
            }.filter { it.isNotBlank() }
             .map { if (it.endsWith("/")) it else "$it/" }
             .distinct()

            var lastNetworkError: Exception? = null
            val (fetched, netErr) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                var found: List<ChildProfileItem>? = null
                var err: Exception? = null
                for (url in candidateUrls) {
                    val isCloud = !CloudConnectionPolicy.isLocalHost(url)
                    val maxAttempts = if (isCloud) 2 else 1
                    var attempt = 0
                    while (attempt < maxAttempts) {
                        attempt++
                        try {
                            val candidateApi = ApiClient.buildProbeApi(url)
                            val result = candidateApi.getPublicProfiles()
                            // If getPublicProfiles succeeds without network exception, connection is confirmed!
                            found = result
                            ApiClient.updateBaseUrl(url)
                            parentPrefs.serverUrl = url
                            err = null
                            break
                        } catch (e: retrofit2.HttpException) {
                            if (e.code() == 401 || e.code() == 403) {
                                // HTTP 401/403 confirms the server IS alive and reachable!
                                // The token is simply invalid, expired, or belongs to another backend.
                                ApiClient.updateBaseUrl(url)
                                parentPrefs.serverUrl = url
                                parentPrefs.clearParentSession()
                                ApiClient.authToken = ""
                                err = null
                                break
                            } else if (e.code() in listOf(408, 429, 500, 502, 503, 504) && attempt < maxAttempts) {
                                err = e
                                kotlinx.coroutines.delay(2000)
                            } else {
                                err = e
                                break
                            }
                        } catch (e: Exception) {
                            err = e
                            if (attempt < maxAttempts) {
                                kotlinx.coroutines.delay(2000)
                            }
                        }
                    }
                    if (found != null || (!parentPrefs.isParentLoggedIn && err == null)) break
                }
                Pair(found, err)
            }
            fetchedProfiles = fetched
            lastNetworkError = netErr

            if (!parentPrefs.isParentLoggedIn) {
                ApiClient.authToken = ""
                _state.value = KidAppState(screen = AppScreen.ChildSelect)
                return@launch
            }

            val activeScreen = _state.value.screen
            val finalScreen = if (activeScreen is AppScreen.ChildSelect) AppScreen.ChildSelect else activeScreen

            if (fetchedProfiles != null) {
                if (fetchedProfiles.isNotEmpty()) {
                    parentPrefs.cachedProfilesJson = gson.toJson(fetchedProfiles)
                    _state.value = _state.value.copy(
                        availableProfiles = fetchedProfiles,
                        isLoading = false,
                        error = null,
                        screen = finalScreen,
                    )
                } else {
                    parentPrefs.cachedProfilesJson = ""
                    _state.value = _state.value.copy(
                        availableProfiles = emptyList(),
                        isLoading = false,
                        error = null,
                        screen = finalScreen,
                    )
                }
            } else {
                // If network failed but we have cached profiles, do NOT show blocking error screen!
                // On launch with 0 profiles, show actionable error if connection failed
                val hasCached = _state.value.availableProfiles.isNotEmpty()
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = if (!hasCached && lastNetworkError != null) "Could not connect to server. Check your connection or tap to retry." else null,
                    screen = finalScreen,
                )
            }
        }
    }

    fun updateServerUrl(newUrl: String) {
        val sanitized = CloudConnectionPolicy.sanitizeBaseUrl(newUrl)
        if (sanitized.isNotBlank()) {
            ApiClient.updateBaseUrl(sanitized)
            parentPrefs.serverUrl = sanitized
            loadProfiles()
        }
    }

    fun selectProfile(profile: ChildProfileItem) {
        if (!parentPrefs.isParentLoggedIn) {
            _state.value = KidAppState(
                screen = AppScreen.ChildSelect,
                error = "Parent Google sign-in is required before opening a child profile.",
            )
            return
        }
        val requiresPin = parentPrefs.requireChildPin && profile.hasPin
        if (requiresPin) {
            _state.value = _state.value.copy(
                childId = profile.id,
                childName = profile.name,
                childAvatar = profile.avatar,
                error = null,
                screen = AppScreen.PinEntry(profile.id, profile.name, profile.avatar),
            )
        } else {
            // Instant Tap-to-Play: friction-free profile launch for kids
            unlockChildDirectly(profile.id, profile.name, profile.avatar)
        }
    }

    fun clearError() {
        if (_state.value.error != null) {
            _state.value = _state.value.copy(error = null)
        }
    }

    fun setChildLock(active: Boolean) {
        _state.value = _state.value.copy(isChildLockActive = active)
    }

    fun toggleChildLock() {
        _state.value = _state.value.copy(isChildLockActive = !_state.value.isChildLockActive)
    }

    fun backToChildSelect() {
        _state.value = _state.value.copy(
            error = null,
            isChildLockActive = false,
            screen = AppScreen.ChildSelect,
        )
    }

    fun ensureParentSession() {
        if (!parentPrefs.isParentLoggedIn) {
            ApiClient.authToken = ""
            _state.value = KidAppState(screen = AppScreen.ChildSelect)
        }
    }

    fun resetAllAccountData() {
        parentPrefs.clearAccountData()
        ApiClient.authToken = ""
        viewModelScope.launch {
            try {
                GoogleCredentialAuth.signOut(getApplication())
            } catch (_: Exception) {}
        }
        _state.value = KidAppState(
            screen = AppScreen.ChildSelect,
            availableProfiles = emptyList(),
            isLoading = false,
            error = null,
            playlist = null,
            childName = "",
            childAvatar = "compass",
            childId = "",
            isChildLockActive = false,
        )
    }

    suspend fun verifyPin(childId: String, pin: String): Boolean {
        if (!parentPrefs.isParentLoggedIn) {
            ApiClient.authToken = ""
            _state.value = KidAppState(
                screen = AppScreen.ChildSelect,
                error = "Parent Google sign-in is required before opening a child profile.",
            )
            return false
        }
        _state.value = _state.value.copy(isLoading = true, error = null)

        // Always verify against the authenticated parent account. A cached child PIN
        // must never outlive a signed-out or deleted parent account.
        return try {
            val response = ApiClient.api.verifyPin(
                ChildVerifyRequest(childId = childId, pin = pin)
            )
            if (response.verified) {
                if (_state.value.screen is AppScreen.PinEntry) {
                    _state.value = _state.value.copy(
                        childId = response.childId,
                        childName = response.name,
                        childAvatar = response.avatar,
                        playlist = null,
                        screen = AppScreen.Playlist(
                            childId = response.childId,
                            childName = response.name,
                            childAvatar = response.avatar,
                        ),
                        isLoading = false,
                        error = null,
                    )
                    loadTodaysPlaylist(response.childId)
                }
                true
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Incorrect PIN. Please try again.",
                )
                false
            }
        } catch (e: retrofit2.HttpException) {
            val detail = try {
                val body = e.response()?.errorBody()?.string() ?: ""
                org.json.JSONObject(body).optString("detail", "")
            } catch (_: Exception) {
                ""
            }
            if (e.code() == 401 && (detail.contains("credentials", ignoreCase = true) || detail.contains("token", ignoreCase = true) || detail.isBlank())) {
                parentPrefs.clearParentSession()
                ApiClient.authToken = ""
                _state.value = KidAppState(
                    screen = AppScreen.ChildSelect,
                    error = "Your parent session ended. Sign in with Google to continue.",
                )
                return false
            }
            val msg = when {
                e.code() == 429 -> if (detail.isNotBlank()) detail else "Too many failed attempts. Account locked temporarily."
                e.code() == 404 -> "Incorrect PIN. Please try again."
                detail.isNotBlank() && !detail.contains("not found", ignoreCase = true) -> detail
                else -> "Incorrect PIN. Please try again."
            }
            _state.value = _state.value.copy(
                isLoading = false,
                error = msg,
            )
            false
        } catch (e: java.io.IOException) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Connection error. Please check your internet connection.",
            )
            false
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Verification error. Please try again.",
            )
            false
        }
    }

    fun unlockChildDirectly(childId: String, name: String, avatar: String) {
        if (!parentPrefs.isParentLoggedIn) {
            _state.value = KidAppState(
                screen = AppScreen.ChildSelect,
                error = "Parent Google sign-in is required before opening a child profile.",
            )
            return
        }
        _state.value = _state.value.copy(
            childId = childId,
            childName = name,
            childAvatar = avatar,
            playlist = null,
            screen = AppScreen.Playlist(
                childId = childId,
                childName = name,
                childAvatar = avatar,
            ),
            isLoading = false,
            error = null,
        )
        loadTodaysPlaylist(childId)
    }

    fun loadTodaysPlaylist(childId: String) {
        viewModelScope.launch {
            if (!parentPrefs.isParentLoggedIn) {
                _state.value = KidAppState(
                    screen = AppScreen.ChildSelect,
                    error = "Parent Google sign-in is required before opening a child profile.",
                )
                return@launch
            }
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = ApiClient.api.getTodaysPlaylist(childId)
                parentPrefs.setCachedPlaylist(childId, gson.toJson(response.playlist))
                _state.value = _state.value.copy(playlist = response.playlist, isLoading = false, error = null)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401) {
                    parentPrefs.clearParentSession()
                    ApiClient.authToken = ""
                    _state.value = KidAppState(
                        screen = AppScreen.ChildSelect,
                        error = "Your parent session ended. Sign in with Google to continue.",
                    )
                } else {
                    _state.value = _state.value.copy(
                        playlist = null,
                        isLoading = false,
                        error = "Could not load playlist. Please try again.",
                    )
                }
            } catch (e: java.io.IOException) {
                val cached = parentPrefs.getCachedPlaylist(childId)
                val cachedPl = if (cached.isNotBlank()) {
                    try { gson.fromJson(cached, PlaylistData::class.java) } catch (_: Exception) { null }
                } else null
                if (cachedPl != null) {
                    _state.value = _state.value.copy(
                        playlist = cachedPl,
                        isLoading = false,
                        error = null,
                    )
                } else {
                    _state.value = _state.value.copy(
                        playlist = null,
                        isLoading = false,
                        error = "Could not load playlist. Please check your connection.",
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    playlist = null,
                    isLoading = false,
                    error = "Could not load playlist. Please check your connection.",
                )
            }
        }
    }

    fun startPlaying(startIndex: Int) {
        val s = _state.value
        val total = s.playlist?.items?.size ?: 0
        val clamped = if (total > 0) startIndex.coerceIn(0, total - 1) else 0
        _state.value = s.copy(
            screen = AppScreen.Player(
                childId = s.childId,
                childName = s.childName,
                childAvatar = s.childAvatar,
                startIndex = clamped,
            )
        )
    }

    fun reportProgress(video: VideoItem, watchedSeconds: Int) {
        viewModelScope.launch {
            try {
                ApiClient.api.updateProgress(
                    ProgressUpdate(
                        childId = _state.value.childId,
                        youtubeId = video.youtubeId,
                        playlistItemId = video.itemId,
                        watchedSeconds = watchedSeconds,
                        completed = false,
                    )
                )
            } catch (ignored: Exception) {}
        }
    }

    fun reportCompleted(video: VideoItem, watchedSeconds: Int) {
        viewModelScope.launch {
            try {
                ApiClient.api.updateProgress(
                    ProgressUpdate(
                        childId = _state.value.childId,
                        youtubeId = video.youtubeId,
                        playlistItemId = video.itemId,
                        watchedSeconds = watchedSeconds,
                        completed = true,
                    )
                )
            } catch (ignored: Exception) {}
        }
    }

    fun showCompletion(videosWatched: Int, totalMinutes: Int) {
        val s = _state.value
        _state.value = s.copy(
            screen = AppScreen.Completion(
                childName = s.childName,
                childAvatar = s.childAvatar,
                videosWatched = videosWatched,
                totalMinutes = totalMinutes,
            )
        )
    }

    fun goBackToPlaylist() {
        val s = _state.value
        _state.value = s.copy(
            screen = AppScreen.Playlist(
                childId = s.childId,
                childName = s.childName,
                childAvatar = s.childAvatar,
            )
        )
    }

    fun openParentZone() {
        _state.value = _state.value.copy(screen = AppScreen.ParentZone)
    }

    fun setDemoWalkthroughData(profile: ChildProfileItem, playlist: PlaylistData) {
        if (!com.kidcurate.app.BuildConfig.DEBUG) return
        _state.value = _state.value.copy(
            availableProfiles = listOf(profile),
            childId = profile.id,
            childName = profile.name,
            childAvatar = profile.avatar,
            playlist = playlist,
            isLoading = false,
            error = null,
        )
    }
}
