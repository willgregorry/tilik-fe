package id.tilik.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FinancialHealthDetail(
    @SerialName("net_profit_growth_yoy")
    val netProfitGrowthYoy: Double? = null,
    @SerialName("operating_cash_flow_idr")
    val operatingCashFlowIdr: Double? = null,
    @SerialName("is_fca")
    val isFca: Boolean = false,
    @SerialName("special_notations")
    val specialNotations: List<String> = emptyList()
)
