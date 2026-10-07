package id.tilik.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpandedDetails(
    @SerialName("valuation")
    val valuation: ValuationPeerDetail,
    @SerialName("broker_flow")
    val brokerFlow: BrokerFlowDetail,
    @SerialName("financial_health")
    val financialHealth: FinancialHealthDetail
)
