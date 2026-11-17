package com.kidcurate.app.data

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

// ─── Auth ─────────────────────────────────────────────────────────────────

data class ChildProfileItem(
    val id: String,
    val name: String,
    val avatar: String,
)

data class ChildVerifyRequest(
    @SerializedName("child_id") val childId: String,
    val pin: String,
)

data class ChildVerifyResponse(
    val verified: Boolean,
    @SerializedName("child_id") val childId: String,
    val name: String,
    val avatar: String,
)

// ─── Playlist ─────────────────────────────────────────────────────────────

@Immutable
data class VideoItem(
    @SerializedName("item_id") val itemId: String,
    val position: Int,
    @SerializedName("youtube_id") val youtubeId: String,
    val title: String,
    @SerializedName("channel_name") val channelName: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("duration_seconds") val durationSeconds: Int?,
    @SerializedName("overall_score") val overallScore: Int?,
    @SerializedName("subject_tags") val subjectTags: List<String>?,
)

@Immutable
data class PlaylistData(
    val id: String,
    @SerializedName("child_id") val childId: String,
    val title: String,
    val description: String?,
    @SerializedName("scheduled_date") val scheduledDate: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("total_duration_seconds") val totalDurationSeconds: Int,
    @SerializedName("video_count") val videoCount: Int,
    val items: List<VideoItem>,
)

data class TodaysPlaylistResponse(
    val playlist: PlaylistData?,
    val message: String?,
)

// ─── Progress ─────────────────────────────────────────────────────────────

data class ProgressUpdate(
    @SerializedName("child_id") val childId: String,
    @SerializedName("youtube_id") val youtubeId: String,
    @SerializedName("playlist_item_id") val playlistItemId: String?,
    @SerializedName("watched_seconds") val watchedSeconds: Int,
    val completed: Boolean,
)

// ─── Quiz ─────────────────────────────────────────────────────────────────

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    @SerializedName("correct_answer") val correctAnswer: Int,
    val explanation: String
)

data class QuizData(
    val questions: List<QuizQuestion>
)

// ─── Parent Zone & Search ───────────────────────────────────────────────────

data class VideoSearchResultItem(
    @SerializedName("youtube_id") val youtubeId: String,
    val title: String,
    @SerializedName("channel_name") val channelName: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("duration_seconds") val durationSeconds: Int?,
    val description: String?,
    @SerializedName("overall_score") val overallScore: Int?,
    @SerializedName("educational_score") val educationalScore: Int?,
    @SerializedName("safety_score") val safetyScore: Int?,
    @SerializedName("is_safe") val isSafe: Boolean?,
)

data class AddVideoToChildRequest(
    @SerializedName("youtube_id") val youtubeId: String,
    val title: String,
    @SerializedName("channel_name") val channelName: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("duration_seconds") val durationSeconds: Int,
)

data class ChildManageItem(
    val id: String,
    val name: String,
    val age: Int,
    val avatar: String,
    val pin: String? = "••••",
    @SerializedName("has_pin") val hasPin: Boolean? = true,
    @SerializedName("parent_id") val parentId: String? = null,
)

data class ChildCreateRequest(
    val name: String,
    val age: Int,
    val avatar: String,
    val pin: String,
)

data class ChildUpdateRequest(
    val name: String?,
    val age: Int?,
    val avatar: String?,
    val pin: String?,
)

data class ReorderPlaylistRequest(
    @SerializedName("ordered_item_ids") val orderedItemIds: List<String>,
)

// ─── Activity Packs for Parents ─────────────────────────────────────────────

@Immutable
data class ActivityPackVideo(
    @SerializedName("youtube_id") val youtubeId: String,
    val title: String,
    @SerializedName("channel_name") val channelName: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("duration_seconds") val durationSeconds: Int,
)

@Immutable
data class ActivityPackItem(
    val id: String,
    val title: String,
    @SerializedName("badge_icon") val badgeIcon: String,
    val tagline: String,
    val category: String,
    @SerializedName("approx_minutes") val approxMinutes: Int,
    val videos: List<ActivityPackVideo>,
)

@Immutable
data class DbSummaryResponse(
    @SerializedName("parent_email") val parentEmail: String = "",
    @SerializedName("database_engine") val databaseEngine: String = "",
    @SerializedName("children_count") val childrenCount: Int = 0,
    @SerializedName("playlists_count") val playlistsCount: Int = 0,
    @SerializedName("total_videos_count") val totalVideosCount: Int = 0,
    @SerializedName("watch_history_count") val watchHistoryCount: Int = 0,
    @SerializedName("account_created_at") val accountCreatedAt: String = "",
)

// ─── Curation & Watch History ───────────────────────────────────────────────

@Immutable
data class CuratedHistoryItem(
    val id: String = "",
    @SerializedName("child_id") val childId: String = "",
    @SerializedName("child_name") val childName: String = "",
    val action: String = "added", // "added", "removed", "pack_applied"
    val title: String = "",
    @SerializedName("youtube_id") val youtubeId: String = "",
    @SerializedName("channel_name") val channelName: String = "",
    @SerializedName("thumbnail_url") val thumbnailUrl: String = "",
    @SerializedName("duration_seconds") val durationSeconds: Int = 0,
    @SerializedName("created_at") val createdAt: String = "",
)

@Immutable
data class CuratedHistoryResponse(
    val history: List<CuratedHistoryItem> = emptyList(),
)

@Immutable
data class WatchHistoryItem(
    @SerializedName("youtube_id") val youtubeId: String = "",
    val title: String = "",
    @SerializedName("thumbnail_url") val thumbnailUrl: String = "",
    @SerializedName("watched_seconds") val watchedSeconds: Int = 0,
    @SerializedName("duration_seconds") val durationSeconds: Int? = null,
    val completed: Boolean = false,
    @SerializedName("watched_at") val watchedAt: String? = null,
)

@Immutable
data class WatchHistoryResponse(
    val history: List<WatchHistoryItem> = emptyList(),
)

