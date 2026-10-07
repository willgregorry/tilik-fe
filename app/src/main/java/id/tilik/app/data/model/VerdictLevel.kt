package id.tilik.app.data.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
enum class VerdictLevel {
    RED,
    YELLOW,
    GREEN;

    val label: String
        get() = when (this) {
            RED -> "Klaim Beresiko"
            YELLOW -> "Perlu Waspada"
            GREEN -> "Sesuai Fakta"
        }

    val title: String
        get() = when (this) {
            RED -> "Klaim Berpotensi Menyesatkan"
            YELLOW -> "Klaim Perlu Diwaspadai"
            GREEN -> "Klaim Terverifikasi Sesuai Data"
        }

    val color: Color
        get() = when (this) {
            RED -> Color(0xFFDC2626)
            YELLOW -> Color(0xFFD97706)
            GREEN -> Color(0xFF16A34A)
        }

    val containerColor: Color
        get() = when (this) {
            RED -> Color(0xFFFEF2F2)
            YELLOW -> Color(0xFFFFFBEB)
            GREEN -> Color(0xFFF0FDF4)
        }

    val borderColor: Color
        get() = when (this) {
            RED -> Color(0xFFFECACA)
            YELLOW -> Color(0xFFFDE68A)
            GREEN -> Color(0xFFBBF7D0)
        }

    companion object {
        fun fromString(value: String?): VerdictLevel {
            return when (value?.uppercase()?.trim()) {
                "GREEN", "HIJAU", "VALID", "FAKTA" -> GREEN
                "YELLOW", "KUNING", "NEUTRAL", "NETRAL", "WARNING" -> YELLOW
                "RED", "MERAH", "MISLEADING", "MENYESATKAN", "HOAX" -> RED
                else -> YELLOW
            }
        }
    }
}
