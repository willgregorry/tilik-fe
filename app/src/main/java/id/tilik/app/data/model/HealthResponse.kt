package id.tilik.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    @SerialName("status")
    val status: String = "healthy",
    @SerialName("app_name")
    val appName: String = "Tilik AI Backend",
    @SerialName("version")
    val version: String = "2.0.0",
    @SerialName("gemini_api")
    val geminiApi: String = "connected",
    @SerialName("sectors_api")
    val sectorsApi: String = "connected",
    @SerialName("slang_rag_records")
    val slangRagRecords: Int = 0
)
