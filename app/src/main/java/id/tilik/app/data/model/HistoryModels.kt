package id.tilik.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistoryItemSummary(
    @SerialName("id")
    val id: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("ticker")
    val ticker: String? = null,
    @SerialName("company_name")
    val companyName: String? = null,
    @SerialName("user_role")
    val userRole: UserRole = UserRole.PEMULA,
    @SerialName("verdict")
    val verdict: VerdictLevel = VerdictLevel.UNKNOWN,
    @SerialName("confidence_score")
    val confidenceScore: Double = 0.0,
    @SerialName("tweet_preview")
    val tweetPreview: String = "",
    @SerialName("source_platform")
    val sourcePlatform: String? = "x"
)

@Serializable
data class HistoryListResponse(
    @SerialName("status")
    val status: String = "success",
    @SerialName("total")
    val total: Int = 0,
    @SerialName("items")
    val items: List<HistoryItemSummary> = emptyList()
)

@Serializable
data class HistoryDetailResponse(
    @SerialName("status")
    val status: String = "success",
    @SerialName("id")
    val id: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("tweet_text")
    val tweetText: String,
    @SerialName("source_platform")
    val sourcePlatform: String? = "x",
    @SerialName("verification")
    val verification: VerificationResponse
)
