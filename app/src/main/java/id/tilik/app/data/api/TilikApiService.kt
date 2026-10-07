package id.tilik.app.data.api

import id.tilik.app.data.model.AuthResponse
import id.tilik.app.data.model.GoogleAuthRequest
import id.tilik.app.data.model.HealthResponse
import id.tilik.app.data.model.HistoryDetailResponse
import id.tilik.app.data.model.HistoryListResponse
import id.tilik.app.data.model.UserProfileResponse
import id.tilik.app.data.model.UserSettingsUpdate
import id.tilik.app.data.model.VerificationResponse
import id.tilik.app.data.model.VerifyTweetRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TilikApiService {

    // --- Authentication ---
    @POST("api/v1/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleAuthRequest
    ): AuthResponse

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): UserProfileResponse

    // --- User Settings ---
    @PUT("api/v1/user/settings")
    suspend fun updateUserSettings(
        @Body request: UserSettingsUpdate
    ): UserProfileResponse

    // --- Verification ---
    @POST("api/v1/verify")
    suspend fun verifyClaim(
        @Body request: VerifyTweetRequest
    ): VerificationResponse

    // --- History ---
    @GET("api/v1/history")
    suspend fun getHistory(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): HistoryListResponse

    @GET("api/v1/history/{history_id}")
    suspend fun getHistoryDetail(
        @Path("history_id") historyId: String
    ): HistoryDetailResponse

    @DELETE("api/v1/history")
    suspend fun clearHistory(): Unit

    @DELETE("api/v1/history/{history_id}")
    suspend fun deleteHistoryItem(
        @Path("history_id") historyId: String
    ): Unit

    // --- Health ---
    @GET("api/v1/health")
    suspend fun getHealth(): HealthResponse
}
