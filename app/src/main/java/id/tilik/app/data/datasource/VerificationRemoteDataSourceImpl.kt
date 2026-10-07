package id.tilik.app.data.datasource

import id.tilik.app.data.api.TilikApiService
import id.tilik.app.data.client.ApiClientProvider
import id.tilik.app.data.client.NetworkResult
import id.tilik.app.data.client.safeApiCall
import id.tilik.app.data.model.HealthResponse
import id.tilik.app.data.model.VerificationResponse
import id.tilik.app.data.model.VerifyTweetRequest
import timber.log.Timber

class VerificationRemoteDataSourceImpl(
    private val apiService: TilikApiService = ApiClientProvider.createService(),
    private val fallbackService: TilikApiService = ApiClientProvider.createFallbackService()
) : VerificationRemoteDataSource {

    override suspend fun verifyClaim(request: VerifyTweetRequest): NetworkResult<VerificationResponse> {
        Timber.tag("TILIK_REMOTE").d("Dispatching verifyClaim to primary backend: text=\"${request.text.take(60)}\", platform=${request.sourcePlatform}")
        val primaryResult = safeApiCall {
            apiService.verifyClaim(request)
        }
        if (primaryResult is NetworkResult.Exception) {
            Timber.tag("TILIK_REMOTE").w("Primary backend failed (${primaryResult.throwable.message}). Retrying with alternate backend URL...")
            return safeApiCall {
                fallbackService.verifyClaim(request)
            }
        }
        return primaryResult
    }

    override suspend fun checkHealth(): NetworkResult<HealthResponse> {
        Timber.tag("TILIK_REMOTE").d("Checking backend health")
        val primary = safeApiCall { apiService.getHealth() }
        if (primary is NetworkResult.Exception) {
            return safeApiCall { fallbackService.getHealth() }
        }
        return primary
    }
}
