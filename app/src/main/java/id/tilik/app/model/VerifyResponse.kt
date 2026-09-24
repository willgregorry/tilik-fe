package id.tilik.app.model

import kotlinx.serialization.Serializable

@Serializable
data class VerifyResponse(
    val status: String,
    val data: VerifyData? = null,
    val message: String? = null
)
