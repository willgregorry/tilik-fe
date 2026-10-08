package id.tilik.app.data.repository

import id.tilik.app.data.client.NetworkResult
import id.tilik.app.data.datasource.VerificationRemoteDataSource
import id.tilik.app.data.datasource.VerificationRemoteDataSourceImpl
import id.tilik.app.data.model.BrokerDetail
import id.tilik.app.data.model.BrokerFlowDetail
import id.tilik.app.data.model.ExpandedDetails
import id.tilik.app.data.model.FactCheckPoint
import id.tilik.app.data.model.FinancialHealthDetail
import id.tilik.app.data.model.HealthResponse
import id.tilik.app.data.model.ValuationPeerDetail
import id.tilik.app.data.model.VerificationResponse
import id.tilik.app.data.model.VerifyTweetRequest
import id.tilik.app.detection.StockKeywordDetector
import timber.log.Timber

class VerificationRepositoryImpl(
    private val remoteDataSource: VerificationRemoteDataSource = VerificationRemoteDataSourceImpl()
) : VerificationRepository {

    override suspend fun verify(
        text: String,
        sourcePlatform: String,
        detectedTicker: String?
    ): Result<VerificationResponse> {
        Timber.tag("TILIK_REPO").i("Memulai verifikasi klaim bursa. Panjang: ${text.length} chars, platform: $sourcePlatform, ticker: $detectedTicker")

        val request = VerifyTweetRequest(
            text = text.trim(),
            sourcePlatform = sourcePlatform,
            userRole = id.tilik.app.data.session.SessionManager.getUserRole()
        )

        return when (val networkResult = remoteDataSource.verifyClaim(request)) {
            is NetworkResult.Success -> {
                Timber.tag("TILIK_REPO").i("✅ Verifikasi sukses diterima dari backend. Emiten: ${networkResult.data.ticker}, Vonis: ${networkResult.data.verdict}")
                AuthRepository.invalidateHistoryCache()
                Result.success(networkResult.data)
            }
            is NetworkResult.Error -> {
                Timber.tag("TILIK_REPO").w("⚠️ Backend mengembalikan error code ${networkResult.code}: ${networkResult.message}")
                val fallback = createFailedResponse(detectedTicker ?: StockKeywordDetector.extractPotentialTicker(text), "Server error (${networkResult.code})")
                Result.success(fallback)
            }
            is NetworkResult.Exception -> {
                Timber.tag("TILIK_REPO").e(networkResult.throwable, "❌ Gagal menghubungi backend API: ${networkResult.throwable.message}")
                val fallback = createFailedResponse(detectedTicker ?: StockKeywordDetector.extractPotentialTicker(text), networkResult.throwable.message ?: "Koneksi terputus")
                Result.success(fallback)
            }
        }
    }

    override suspend fun checkHealth(): Result<HealthResponse> {
        return when (val result = remoteDataSource.checkHealth()) {
            is NetworkResult.Success -> Result.success(result.data)
            is NetworkResult.Error -> Result.failure(RuntimeException("Health HTTP ${result.code}: ${result.message}"))
            is NetworkResult.Exception -> Result.failure(result.throwable)
        }
    }

    private fun createFailedResponse(
        detectedTicker: String?,
        reason: String
    ): VerificationResponse {
        val ticker = detectedTicker?.takeIf { it.isNotBlank() && it != "IDX" } ?: "-"

        return VerificationResponse(
            status = "failed",
            ticker = ticker,
            companyName = "-",
            verdict = "-",
            confidenceScore = 0.0,
            points = emptyList(),
            coolingOffPrompt = "-",
            details = ExpandedDetails(
                valuation = ValuationPeerDetail(
                    peRatio = null,
                    pbvRatio = null,
                    industryMedianPe = null,
                    industryMedianPbv = null,
                    valuationStatus = "-"
                ),
                brokerFlow = BrokerFlowDetail(
                    foreignNetIdr = 0.0,
                    topBuyers = emptyList(),
                    topSellers = emptyList(),
                    summaryVerdict = "-"
                ),
                financialHealth = FinancialHealthDetail(
                    netProfitGrowthYoy = null,
                    operatingCashFlowIdr = null,
                    isFca = false,
                    specialNotations = emptyList()
                )
            ),
            isCached = false
        )
    }
}
