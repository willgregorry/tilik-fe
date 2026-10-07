package id.tilik.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BrokerFlowDetail(
    @SerialName("foreign_net_idr")
    val foreignNetIdr: Double,
    @SerialName("top_buyers")
    val topBuyers: List<BrokerDetail> = emptyList(),
    @SerialName("top_sellers")
    val topSellers: List<BrokerDetail> = emptyList(),
    @SerialName("summary_verdict")
    val summaryVerdict: String
)
