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
)

class KidViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(KidAppState())
    val state: StateFlow<KidAppState> = _state
    private val parentPrefs = ParentPreferences.get(application)
    private val gson = Gson()

    init {
        loadProfiles()
    }

    fun loadProfiles() {
        viewModelScope.launch {
            // 1. Immediately restore cached profiles if available for instant offline / startup UI
            if (parentPrefs.cachedProfilesJson.isNotBlank()) {
                try {
                    val listType = object : TypeToken<List<ChildProfileItem>>() {}.type
                    val cached: List<ChildProfileItem> = gson.fromJson(parentPrefs.cachedProfilesJson, listType)
                    if (cached.isNotEmpty()) {
                        _state.value = _state.value.copy(
                            availableProfiles = cached,
                            isLoading = false,
                            error = null,
                            screen = AppScreen.ChildSelect,
                        )
                    }
                } catch (_: Exception) {}
            } else {
                _state.value = _state.value.copy(isLoading = true, error = null)
            }

            // 2. Fetch live profiles with intelligent fallback across configured base URL and localhost
            var fetchedProfiles: List<ChildProfileItem>? = null
            val candidateUrls = listOf(
                ApiClient.getBaseUrl(),
                "http://localhost:8000/",
                "http://10.0.2.2:8000/"
            ).distinct()

            for (url in candidateUrls) {
                try {
                    val candidateApi = ApiClient.buildApi(url)
                    val result = candidateApi.getPublicProfiles()
                    if (result.isNotEmpty() || candidateUrls.indexOf(url) == 0) {
                        fetchedProfiles = result
                        ApiClient.updateBaseUrl(url)
                        parentPrefs.serverUrl = url
                        break
                    }
                } catch (_: Exception) {
                    continue
                }
            }

            if (fetchedProfiles != null) {
                parentPrefs.cachedProfilesJson = gson.toJson(fetchedProfiles)
                _state.value = _state.value.copy(
                    availableProfiles = fetchedProfiles,
                    isLoading = false,
                    error = null,
                    screen = AppScreen.ChildSelect,
                )
            } else {
                // If network failed but we have cached profiles, do NOT show blocking error screen!
                if (_state.value.availableProfiles.isEmpty()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Could not connect to server. Check your Wi-Fi or API URL.",
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = null)
                }
            }
        }
    }

    fun updateServerUrl(newUrl: String) {
        ApiClient.updateBaseUrl(newUrl)
        parentPrefs.serverUrl = newUrl
        loadProfiles()
    }

    fun selectProfile(profile: ChildProfileItem) {
        _state.value = _state.value.copy(
            childId = profile.id,
            childName = profile.name,
            childAvatar = profile.avatar,
            error = null,
            screen = AppScreen.PinEntry(profile.id, profile.name, profile.avatar),
        )
    }

    fun backToChildSelect() {
        _state.value = _state.value.copy(
            error = null,
            screen = AppScreen.ChildSelect,
        )
    }

    fun verifyPin(childId: String, pin: String): Unit {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = ApiClient.api.verifyPin(
                    ChildVerifyRequest(childId = childId, pin = pin)
                )
                if (response.verified) {
                    _state.value = _state.value.copy(
                        childId = response.childId,
                        childName = response.name,
                        childAvatar = response.avatar,
                        screen = AppScreen.Playlist(
                            childId = response.childId,
                            childName = response.name,
                            childAvatar = response.avatar,
                        ),
                        isLoading = false,
                    )
                    loadTodaysPlaylist(response.childId)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Incorrect PIN. Please try again.",
                )
            }
        }
    }

    fun loadTodaysPlaylist(childId: String) {
        viewModelScope.launch {
            // Restore per-child cached playlist first if available
            val cachedJson = parentPrefs.getCachedPlaylist(childId)
            if (cachedJson.isNotBlank()) {
                try {
                    val cached: PlaylistData = gson.fromJson(cachedJson, PlaylistData::class.java)
                    if (cached.items.isNotEmpty()) {
                        _state.value = _state.value.copy(playlist = cached)
                    }
                } catch (_: Exception) {}
            }

            try {
                val response = ApiClient.api.getTodaysPlaylist(childId)
                parentPrefs.setCachedPlaylist(childId, gson.toJson(response.playlist))
                _state.value = _state.value.copy(playlist = response.playlist, error = null)
            } catch (e: Exception) {
                if (_state.value.playlist == null) {
                    _state.value = _state.value.copy(error = "Could not load playlist")
                }
            }
        }
    }

    fun startPlaying(startIndex: Int) {
        val s = _state.value
        _state.value = s.copy(
            screen = AppScreen.Player(
                childId = s.childId,
                childName = s.childName,
                childAvatar = s.childAvatar,
                startIndex = startIndex,
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
}
