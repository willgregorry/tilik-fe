package id.tilik.app.data.repository

import id.tilik.app.data.model.HealthResponse
import id.tilik.app.data.model.VerificationResponse

interface VerificationRepository {
    suspend fun verify(
        text: String,
        sourcePlatform: String = "threads",
        detectedTicker: String? = null
    ): Result<VerificationResponse>

    suspend fun checkHealth(): Result<HealthResponse>
}
