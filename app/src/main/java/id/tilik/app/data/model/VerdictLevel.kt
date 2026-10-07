package id.tilik.app.data.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class VerdictLevel {
    @SerialName("HOAX_BAHAYA")
    HOAX_BAHAYA,

    @SerialName("WASPADA")
    WASPADA,

    @SerialName("SESUAI_FAKTA")
    SESUAI_FAKTA,

    @SerialName("RED")
    RED,

    @SerialName("YELLOW")
    YELLOW,

    @SerialName("GREEN")
    GREEN,

    @SerialName("UNKNOWN")
    UNKNOWN;

    val label: String
        get() = when (this) {
            HOAX_BAHAYA, RED -> "Klaim Beresiko"
            WASPADA, YELLOW -> "Perlu Waspada"
            SESUAI_FAKTA, GREEN -> "Sesuai Fakta"
            UNKNOWN -> "-"
        }

    val title: String
        get() = when (this) {
            HOAX_BAHAYA, RED -> "Klaim Berpotensi Menyesatkan / Hoax"
            WASPADA, YELLOW -> "Klaim Perlu Diwaspadai"
            SESUAI_FAKTA, GREEN -> "Klaim Terverifikasi Sesuai Data"
            UNKNOWN -> "-"
        }

    val color: Color
        get() = when (this) {
            HOAX_BAHAYA, RED -> Color(0xFFEF4444)
            WASPADA, YELLOW -> Color(0xFFF59E0B)
            SESUAI_FAKTA, GREEN -> Color(0xFF22C55E)
            UNKNOWN -> Color(0xFF888888)
        }

    val containerColor: Color
        get() = when (this) {
            HOAX_BAHAYA, RED -> Color(0xFF241113)
            WASPADA, YELLOW -> Color(0xFF291C08)
            SESUAI_FAKTA, GREEN -> Color(0xFF0F2115)
            UNKNOWN -> Color(0xFF141414)
        }

    val borderColor: Color
        get() = when (this) {
            HOAX_BAHAYA, RED -> Color(0x33EF4444)
            WASPADA, YELLOW -> Color(0x33F59E0B)
            SESUAI_FAKTA, GREEN -> Color(0x3322C55E)
            UNKNOWN -> Color(0x14FFFFFF)
        }

    companion object {
        fun fromString(value: String?): VerdictLevel {
            return when (value?.uppercase()?.trim()) {
                "SESUAI_FAKTA", "GREEN", "HIJAU", "VALID", "FAKTA" -> SESUAI_FAKTA
                "WASPADA", "YELLOW", "KUNING", "NEUTRAL", "NETRAL", "WARNING" -> WASPADA
                "HOAX_BAHAYA", "RED", "MERAH", "MISLEADING", "MENYESATKAN", "HOAX" -> HOAX_BAHAYA
                else -> UNKNOWN
            }
        }
    }
}
