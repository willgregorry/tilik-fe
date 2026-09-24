package id.tilik.app.repository

import id.tilik.app.model.VerifyResponse
import id.tilik.app.network.ApiClientProvider
import id.tilik.app.network.TilikApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber

class VerificationRepositoryImpl(
    private val apiService: TilikApiService = ApiClientProvider.createService()
) : VerificationRepository {

    override suspend fun verify(
        imageBytes: ByteArray,
        audioBytes: ByteArray?,
        detectedTicker: String?,
        extractedText: String?
    ): Result<VerifyResponse> = runCatching {
        val imageRequestBody = imageBytes.toRequestBody("image/webp".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", "frame.webp", imageRequestBody)

        val audioPart = audioBytes?.let {
            val audioRequestBody = it.toRequestBody("audio/wav".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("audio", "audio.wav", audioRequestBody)
        }

        val tickerBody = detectedTicker?.takeIf { it.isNotBlank() }?.let {
            it.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        val textBody = extractedText?.takeIf { it.isNotBlank() }?.let {
            it.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        Timber.d("Dispatching verification request. Ticker: $detectedTicker, HasText: ${extractedText != null}")
        val response = apiService.verifyClaim(imagePart, audioPart, tickerBody, textBody)
        Timber.d("Verification response received: status=${response.status}")
        response
    }.onFailure { throwable ->
        Timber.e(throwable, "Verification request failed")
    }
}
