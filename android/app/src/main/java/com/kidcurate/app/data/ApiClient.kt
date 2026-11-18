package com.kidcurate.app.data

import android.content.Context
import android.util.Log
import com.kidcurate.app.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface KidCurateApi {

    @GET("api/children/profiles")
    suspend fun getPublicProfiles(): List<ChildProfileItem>

    @POST("api/children/verify-pin")
    suspend fun verifyPin(@Body request: ChildVerifyRequest): ChildVerifyResponse

    @GET("api/playlists/child/{child_id}/today")
    suspend fun getTodaysPlaylist(@Path("child_id") childId: String): TodaysPlaylistResponse

    @POST("api/progress/update")
    suspend fun updateProgress(@Body progress: ProgressUpdate): Map<String, Boolean>

    @GET("api/videos/search")
    suspend fun searchVideos(
        @Query("q") query: String,
        @Query("max_results") maxResults: Int = 30
    ): List<VideoSearchResultItem>

    @GET("api/videos/suggestions")
    suspend fun getSearchSuggestions(@Query("q") query: String): List<String>

    @POST("api/playlists/child/{child_id}/add-video")
    suspend fun addVideoToChildPlaylist(
        @Path("child_id") childId: String,
        @Body request: AddVideoToChildRequest,
    ): Map<String, Any>

    @DELETE("api/playlists/child/{child_id}/items/{item_id}")
    suspend fun removeVideoFromChildPlaylist(
        @Path("child_id") childId: String,
        @Path("item_id") itemId: String,
    ): Map<String, Any>

    @PUT("api/playlists/child/{child_id}/reorder")
    suspend fun reorderChildPlaylist(
        @Path("child_id") childId: String,
        @Body request: ReorderPlaylistRequest,
    ): Map<String, Any>

    @GET("api/children/manage")
    suspend fun getManageChildren(): List<ChildManageItem>

    @POST("api/children/manage")
    suspend fun createChild(@Body request: ChildCreateRequest): ChildManageItem

    @PUT("api/children/manage/{child_id}")
    suspend fun updateChild(
        @Path("child_id") childId: String,
        @Body request: ChildUpdateRequest,
    ): ChildManageItem

    @DELETE("api/children/manage/{child_id}")
    suspend fun deleteChild(@Path("child_id") childId: String): Map<String, Any>

    @DELETE("api/auth/account")
    suspend fun deleteAccount(): Map<String, Any>

    @GET("api/auth/diagnostic/db-summary")
    suspend fun getDbSummary(): DbSummaryResponse

    @GET("api/playlists/activity-packs")
    suspend fun getActivityPacks(): List<ActivityPackItem>

    @GET("api/playlists/curation-history")
    suspend fun getCurationHistory(
        @Query("child_id") childId: String? = null,
        @Query("limit") limit: Int = 50,
    ): CuratedHistoryResponse

    @GET("api/progress/child/{child_id}/history")
    suspend fun getWatchHistory(
        @Path("child_id") childId: String,
        @Query("limit") limit: Int = 50,
    ): WatchHistoryResponse

    @POST("api/playlists/child/{child_id}/apply-pack/{pack_id}")
    suspend fun applyActivityPack(
        @Path("child_id") childId: String,
        @Path("pack_id") packId: String,
    ): Map<String, Any>

    @POST("api/auth/register")
    suspend fun registerParent(@Body request: Map<String, String>): Map<String, Any>

    @POST("api/auth/login")
    suspend fun loginParent(@Body request: Map<String, String>): Map<String, Any>

    @POST("api/auth/verify-parent-password")
    suspend fun verifyParentPassword(@Body request: Map<String, String>): Map<String, Any>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: Map<String, String>): Map<String, Any>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: Map<String, String>): Map<String, Any>

    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: Map<String, String>): Map<String, Any>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: Map<String, String>): Map<String, Any>
}

private val Response.responseCount: Int
    get() {
        var count = 1
        var prior = priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

object ApiClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    @Volatile
    var authToken: String = ""

    @Volatile
    private var appContext: Context? = null
    private val refreshLock = Any()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val orig = chain.request()
            val token = authToken
            val newReq = if (token.isNotBlank()) {
                orig.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                orig
            }
            chain.proceed(newReq)
        }
        .authenticator { _, response ->
            if (response.responseCount >= 3) return@authenticator null
            val path = response.request.url.encodedPath
            if (path.contains("api/auth/refresh") || path.contains("api/auth/login") || path.contains("api/auth/google")) {
                return@authenticator null
            }
            val reqToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim() ?: ""

            synchronized(refreshLock) {
                // If another concurrent thread has already refreshed the token, retry with it
                if (authToken.isNotBlank() && authToken != reqToken) {
                    return@authenticator response.request.newBuilder()
                        .header("Authorization", "Bearer $authToken")
                        .build()
                }

                val ctx = appContext ?: return@authenticator null
                val prefs = ParentPreferences.get(ctx)
                val rToken = prefs.parentRefreshToken
                if (rToken.isBlank()) return@authenticator null

                val refreshUrl = currentBaseUrl + "api/auth/refresh"
                val jsonMediaType = "application/json; charset=utf-8".toMediaType()
                val body = "{\"refresh_token\": \"$rToken\"}".toRequestBody(jsonMediaType)
                val refreshRequest = Request.Builder()
                    .url(refreshUrl)
                    .post(body)
                    .build()

                try {
                    val rawClient = OkHttpClient.Builder()
                        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .build()
                    val refreshResp = rawClient.newCall(refreshRequest).execute()
                    if (refreshResp.isSuccessful) {
                        val respBody = refreshResp.body?.string() ?: ""
                        val json = JSONObject(respBody)
                        val newAccess = json.optString("access_token", "")
                        val newRefresh = json.optString("refresh_token", "")
                        if (newAccess.isNotBlank()) {
                            authToken = newAccess
                            prefs.parentAuthToken = newAccess
                            if (newRefresh.isNotBlank()) {
                                prefs.parentRefreshToken = newRefresh
                            }
                            return@authenticator response.request.newBuilder()
                                .header("Authorization", "Bearer $newAccess")
                                .build()
                        }
                    } else if (refreshResp.code == 401 || refreshResp.code == 403) {
                        prefs.clearParentSession()
                        authToken = ""
                    }
                } catch (e: Exception) {
                    Log.e("ApiClient", "Token refresh failed: ${e.message}")
                }
                null
            }
        }
        .addInterceptor(logging)
        .build()

    private val cloudProbeClient = httpClient.newBuilder()
        .connectTimeout(25, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(25, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val localProbeClient = httpClient.newBuilder()
        .connectTimeout(4, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(4, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    @Volatile
    private var currentBaseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/') + "/"

    @Volatile
    private var currentApi: KidCurateApi = buildApi(currentBaseUrl)

    fun buildApi(baseUrl: String): KidCurateApi {
        val sanitized = CloudConnectionPolicy.sanitizeBaseUrl(baseUrl).ifBlank { currentBaseUrl }
        return Retrofit.Builder()
            .baseUrl(sanitized)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KidCurateApi::class.java)
    }

    fun buildProbeApi(baseUrl: String): KidCurateApi {
        val sanitized = CloudConnectionPolicy.sanitizeBaseUrl(baseUrl).ifBlank { currentBaseUrl }
        val isLocal = CloudConnectionPolicy.isLocalHost(sanitized)
        val client = if (isLocal) localProbeClient else cloudProbeClient
        return Retrofit.Builder()
            .baseUrl(sanitized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KidCurateApi::class.java)
    }

    val api: KidCurateApi
        get() = currentApi

    fun getBaseUrl(): String = currentBaseUrl

    fun updateBaseUrl(newUrl: String) {
        val sanitized = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        currentBaseUrl = sanitized
        currentApi = buildApi(sanitized)
    }
}
