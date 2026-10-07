package id.tilik.app.data.session

import android.content.Context
import android.content.SharedPreferences
import id.tilik.app.data.model.AuthResponse
import id.tilik.app.data.model.UserPayload
import id.tilik.app.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private const val PREFS_NAME = "tilik_session_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_NAME = "user_name"
    private const val KEY_PICTURE = "user_picture"
    private const val KEY_GOOGLE_ID = "user_google_id"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_ROLE_ONBOARDING_DONE = "role_onboarding_done"

    private var prefs: SharedPreferences? = null

    private val _currentUserState = MutableStateFlow<UserPayload?>(null)
    val currentUserState: StateFlow<UserPayload?> = _currentUserState.asStateFlow()

    private val _isLoggedInState = MutableStateFlow(false)
    val isLoggedInState: StateFlow<Boolean> = _isLoggedInState.asStateFlow()

    private val _isRoleOnboardingDoneState = MutableStateFlow(false)
    val isRoleOnboardingDoneState: StateFlow<Boolean> = _isRoleOnboardingDoneState.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadSessionFromPrefs()
        }
    }

    private fun loadSessionFromPrefs() {
        val p = prefs ?: return
        val token = p.getString(KEY_ACCESS_TOKEN, null)
        val email = p.getString(KEY_EMAIL, null)
        val googleId = p.getString(KEY_GOOGLE_ID, null)
        val roleStr = p.getString(KEY_USER_ROLE, null)
        val role = if (!roleStr.isNullOrBlank()) UserRole.fromString(roleStr) else null
        val onboardingDone = p.getBoolean(KEY_ROLE_ONBOARDING_DONE, false) || (role != null)

        if (!token.isNullOrBlank() && !email.isNullOrBlank()) {
            val user = UserPayload(
                email = email,
                name = p.getString(KEY_NAME, null),
                picture = p.getString(KEY_PICTURE, null),
                googleId = googleId ?: "",
                userRole = role
            )
            _currentUserState.value = user
            _isLoggedInState.value = true
            _isRoleOnboardingDoneState.value = onboardingDone
        } else {
            _currentUserState.value = null
            _isLoggedInState.value = false
            _isRoleOnboardingDoneState.value = false
        }
    }

    fun saveAuthSession(auth: AuthResponse, onboardingDone: Boolean? = null) {
        val p = prefs ?: return
        val hasRole = auth.user.userRole != null
        val resolvedOnboarding = onboardingDone ?: (hasRole || p.getBoolean(KEY_ROLE_ONBOARDING_DONE, false))
        p.edit().apply {
            putString(KEY_ACCESS_TOKEN, auth.accessToken)
            putString(KEY_EMAIL, auth.user.email)
            putString(KEY_NAME, auth.user.name)
            putString(KEY_PICTURE, auth.user.picture)
            putString(KEY_GOOGLE_ID, auth.user.googleId)
            if (auth.user.userRole != null) {
                putString(KEY_USER_ROLE, auth.user.userRole.name)
            }
            putBoolean(KEY_ROLE_ONBOARDING_DONE, resolvedOnboarding)
            apply()
        }
        _currentUserState.value = auth.user
        _isLoggedInState.value = true
        _isRoleOnboardingDoneState.value = resolvedOnboarding
    }

    fun saveUser(user: UserPayload) {
        val p = prefs ?: return
        p.edit().apply {
            putString(KEY_EMAIL, user.email)
            putString(KEY_NAME, user.name)
            putString(KEY_PICTURE, user.picture)
            putString(KEY_GOOGLE_ID, user.googleId)
            if (user.userRole != null) {
                putString(KEY_USER_ROLE, user.userRole.name)
                putBoolean(KEY_ROLE_ONBOARDING_DONE, true)
            }
            apply()
        }
        _currentUserState.value = user
        if (user.userRole != null) {
            _isRoleOnboardingDoneState.value = true
        }
    }

    fun updateRole(role: UserRole) {
        val p = prefs ?: return
        p.edit().apply {
            putString(KEY_USER_ROLE, role.name)
            putBoolean(KEY_ROLE_ONBOARDING_DONE, true)
            apply()
        }
        _isRoleOnboardingDoneState.value = true
        val current = _currentUserState.value
        if (current != null) {
            _currentUserState.value = current.copy(userRole = role)
        }
    }

    fun setRoleOnboardingDone(done: Boolean) {
        prefs?.edit()?.putBoolean(KEY_ROLE_ONBOARDING_DONE, done)?.apply()
        _isRoleOnboardingDoneState.value = done
    }

    fun isRoleOnboardingDone(): Boolean {
        return prefs?.getBoolean(KEY_ROLE_ONBOARDING_DONE, false) ?: false
    }

    fun getAccessToken(): String? {
        return prefs?.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getUser(): UserPayload? {
        return _currentUserState.value
    }

    fun getUserRole(): UserRole {
        val roleStr = prefs?.getString(KEY_USER_ROLE, null)
        return if (!roleStr.isNullOrBlank()) UserRole.fromString(roleStr) else UserRole.PEMULA
    }

    fun clearSession() {
        prefs?.edit()?.clear()?.apply()
        _currentUserState.value = null
        _isLoggedInState.value = false
        _isRoleOnboardingDoneState.value = false
    }
}
