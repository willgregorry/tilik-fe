package id.tilik.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(
    @SerialName("id_token")
    val idToken: String
)

@Serializable
data class UserPayload(
    @SerialName("email")
    val email: String,
    @SerialName("name")
    val name: String? = null,
    @SerialName("picture")
    val picture: String? = null,
    @SerialName("google_id")
    val googleId: String,
    @SerialName("user_role")
    val userRole: UserRole? = null
)

@Serializable
data class AuthResponse(
    @SerialName("status")
    val status: String = "success",
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String = "bearer",
    @SerialName("expires_in")
    val expiresIn: Int = 86400,
    @SerialName("user")
    val user: UserPayload,
    @SerialName("is_new_user")
    val isNewUser: Boolean = false
)

@Serializable
data class UserProfileResponse(
    @SerialName("status")
    val status: String = "success",
    @SerialName("user")
    val user: UserPayload
)

@Serializable
data class UserSettingsUpdate(
    @SerialName("user_role")
    val userRole: UserRole
)

@Serializable
data class ErrorResponse(
    @SerialName("detail")
    val detail: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("error_type")
    val errorType: String? = null
)
