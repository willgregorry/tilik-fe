package id.tilik.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerificationResponse(
    @SerialName("status")
    val status: String = "success",
    @SerialName("ticker")
    val ticker: String? = null,
    @SerialName("company_name")
    val companyName: String? = null,
    @SerialName("verdict")
    val verdict: String,
    @SerialName("confidence_score")
    val confidenceScore: Double,
    @SerialName("user_role")
    val userRole: UserRole? = null,
    @SerialName("verdict_title")
    val verdictTitle: String? = null,
    @SerialName("summary")
    val summary: String? = null,
    @SerialName("points")
    val points: List<FactCheckPoint> = emptyList(),
    @SerialName("cooling_off_prompt")
    val coolingOffPrompt: String,
    @SerialName("details")
    val details: ExpandedDetails,
    @SerialName("is_cached")
    val isCached: Boolean = false,
    @SerialName("history_id")
    val historyId: String? = null
) {
    val verdictLevel: VerdictLevel
        get() = VerdictLevel.fromString(verdict)

    val confidencePercentage: Int
        get() = (confidenceScore * 100).toInt().coerceIn(0, 100)
}
