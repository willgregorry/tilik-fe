package id.tilik.app.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object UserRoleSerializer : KSerializer<UserRole> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UserRole", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UserRole) {
        encoder.encodeString(value.name.lowercase())
    }

    override fun deserialize(decoder: Decoder): UserRole {
        val str = decoder.decodeString()
        return UserRole.fromString(str)
    }
}

@Serializable(with = UserRoleSerializer::class)
enum class UserRole {
    PEMULA,
    EXPERT;

    val displayName: String
        get() = when (this) {
            PEMULA -> "Pemula"
            EXPERT -> "Pakar"
        }

    val subtitle: String
        get() = when (this) {
            PEMULA -> "Bahasa santai & panduan emosi"
            EXPERT -> "Valuasi lengkap & devil's advocate"
        }

    companion object {
        fun fromString(value: String?): UserRole {
            return when (value?.uppercase()?.trim()) {
                "EXPERT", "PAKAR", "ADVANCED" -> EXPERT
                else -> PEMULA
            }
        }
    }
}
