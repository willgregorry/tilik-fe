package id.tilik.app.model

enum class VerdictType {
    VALID,
    MISLEADING,
    UNSUBSTANTIATED;

    companion object {
        fun fromString(value: String): VerdictType {
            return when (value.uppercase()) {
                "VALID", "FAKTA" -> VALID
                "MISLEADING", "MENYESATKAN" -> MISLEADING
                else -> UNSUBSTANTIATED
            }
        }
    }
}
