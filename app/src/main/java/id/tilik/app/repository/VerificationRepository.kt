package id.tilik.app.repository

import id.tilik.app.model.VerifyResponse

interface VerificationRepository {
    suspend fun verify(
        imageBytes: ByteArray,
        audioBytes: ByteArray?,
        detectedTicker: String?,
        extractedText: String? = null
    ): Result<VerifyResponse>
}
