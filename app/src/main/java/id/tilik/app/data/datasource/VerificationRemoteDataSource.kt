package id.tilik.app.data.datasource

import id.tilik.app.data.client.NetworkResult
import id.tilik.app.data.model.HealthResponse
import id.tilik.app.data.model.VerificationResponse
import id.tilik.app.data.model.VerifyTweetRequest

interface VerificationRemoteDataSource {
    suspend fun verifyClaim(request: VerifyTweetRequest): NetworkResult<VerificationResponse>
    suspend fun checkHealth(): NetworkResult<HealthResponse>
}
