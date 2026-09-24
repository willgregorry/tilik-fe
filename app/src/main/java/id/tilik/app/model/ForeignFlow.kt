package id.tilik.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForeignFlow(
    val status: String,
    @SerialName("value_formatted")
    val valueFormatted: String,
    val period: String
)
