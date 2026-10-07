package id.tilik.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BrokerDetail(
    @SerialName("broker_code")
    val brokerCode: String,
    @SerialName("broker_type")
    val brokerType: String,
    @SerialName("net_value_idr")
    val netValueIdr: Double,
    @SerialName("action")
    val action: String
)
