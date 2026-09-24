package id.tilik.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyData(
    val ticker: String,
    @SerialName("company_name")
    val companyName: String,
    val verdict: String,
    @SerialName("confidence_score")
    val confidenceScore: Int = 0,
    @SerialName("influencer_claim")
    val influencerClaim: String,
    @SerialName("market_evidence")
    val marketEvidence: MarketEvidence,
    @SerialName("ai_summary")
    val aiSummary: String,
    val disclaimer: String
)
