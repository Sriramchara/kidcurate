package com.kidcurate.app.data

import com.kidcurate.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

    @GET("api/ai/quiz/{youtube_id}")
    suspend fun getQuiz(@Path("youtube_id") youtubeId: String): QuizData

    @GET("api/videos/search")
    suspend fun searchVideos(@Query("q") query: String): List<VideoSearchResultItem>

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

    @GET("api/playlists/activity-packs")
    suspend fun getActivityPacks(): List<ActivityPackItem>

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
}

object ApiClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Volatile
    var authToken: String = ""

    private val httpClient = OkHttpClient.Builder()
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
        .addInterceptor(logging)
        .build()

    @Volatile
    private var currentBaseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/') + "/"

    @Volatile
    private var currentApi: KidCurateApi = buildApi(currentBaseUrl)

    fun buildApi(baseUrl: String): KidCurateApi {
        val sanitized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(sanitized)
            .client(httpClient)
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
