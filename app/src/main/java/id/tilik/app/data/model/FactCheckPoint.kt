package id.tilik.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FactCheckPoint(
    @SerialName("title")
    val title: String,
    @SerialName("fact")
    val fact: String,
    @SerialName("is_favorable")
    val isFavorable: Boolean
)
