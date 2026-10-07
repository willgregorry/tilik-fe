package id.tilik.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValuationPeerDetail(
    @SerialName("pe_ratio")
    val peRatio: Double? = null,
    @SerialName("pbv_ratio")
    val pbvRatio: Double? = null,
    @SerialName("industry_median_pe")
    val industryMedianPe: Double? = null,
    @SerialName("industry_median_pbv")
    val industryMedianPbv: Double? = null,
    @SerialName("valuation_status")
    val valuationStatus: String
)
