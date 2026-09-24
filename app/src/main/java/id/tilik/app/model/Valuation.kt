package id.tilik.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Valuation(
    @SerialName("pe_ratio")
    val peRatio: Double? = null,
    @SerialName("pbv_ratio")
    val pbvRatio: Double? = null,
    @SerialName("yoy_profit_growth")
    val yoyProfitGrowth: String? = null
)
