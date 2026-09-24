package id.tilik.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarketEvidence(
    @SerialName("foreign_flow")
    val foreignFlow: ForeignFlow,
    val valuation: Valuation
)
