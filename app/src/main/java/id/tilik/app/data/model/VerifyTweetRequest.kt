package id.tilik.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyTweetRequest(
    @SerialName("text")
    val text: String,
    @SerialName("source_platform")
    val sourcePlatform: String? = "threads"
)
