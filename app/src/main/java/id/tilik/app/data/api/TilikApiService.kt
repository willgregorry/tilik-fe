package id.tilik.app.data.api

import id.tilik.app.data.model.HealthResponse
import id.tilik.app.data.model.VerificationResponse
import id.tilik.app.data.model.VerifyTweetRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TilikApiService {

    @POST("api/v1/verify")
    suspend fun verifyClaim(
        @Body request: VerifyTweetRequest
    ): VerificationResponse

    @GET("api/v1/health")
    suspend fun getHealth(): HealthResponse
}
