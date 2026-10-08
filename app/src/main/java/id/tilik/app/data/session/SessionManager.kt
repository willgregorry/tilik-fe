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

    private const val PREFS_SESSION = "tilik_session_prefs"
    private const val PREFS_ACCOUNTS = "tilik_onboarded_accounts"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_NAME = "user_name"
    private const val KEY_PICTURE = "user_picture"
    private const val KEY_GOOGLE_ID = "user_google_id"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_ROLE_ONBOARDING_DONE = "role_onboarding_done"
    private const val KEY_ROLE_EXPLICITLY_CHOSEN = "role_explicitly_chosen"

    private var sessionPrefs: SharedPreferences? = null
    private var accountPrefs: SharedPreferences? = null
    var appContext: Context? = null
        private set

    private val _currentUserState = MutableStateFlow<UserPayload?>(null)
    val currentUserState: StateFlow<UserPayload?> = _currentUserState.asStateFlow()

    private val _isLoggedInState = MutableStateFlow(false)
    val isLoggedInState: StateFlow<Boolean> = _isLoggedInState.asStateFlow()

    private val _isRoleOnboardingDoneState = MutableStateFlow(false)
    val isRoleOnboardingDoneState: StateFlow<Boolean> = _isRoleOnboardingDoneState.asStateFlow()

    fun init(context: Context) {
        if (sessionPrefs == null) {
            val appCtx = context.applicationContext
            appContext = appCtx
            sessionPrefs = appCtx.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
            accountPrefs = appCtx.getSharedPreferences(PREFS_ACCOUNTS, Context.MODE_PRIVATE)
            loadSessionFromPrefs()
        }
    }

    fun isAccountOnboarded(googleId: String?, email: String?): Boolean {
        val ap = accountPrefs ?: return false
        val byGid = !googleId.isNullOrBlank() && ap.getBoolean("onboarded_gid_$googleId", false)
        val byEmail = !email.isNullOrBlank() && ap.getBoolean("onboarded_email_${email.lowercase().trim()}", false)
        return byGid || byEmail
    }

    fun markAccountOnboarded(googleId: String?, email: String?, role: UserRole? = null) {
        val ap = accountPrefs ?: return
        ap.edit().apply {
            if (!googleId.isNullOrBlank()) {
                putBoolean("onboarded_gid_$googleId", true)
                if (role != null) putString("role_gid_$googleId", role.name)
            }
            if (!email.isNullOrBlank()) {
                val cleanEmail = email.lowercase().trim()
                putBoolean("onboarded_email_$cleanEmail", true)
                if (role != null) putString("role_email_$cleanEmail", role.name)
            }
            apply()
        }
    }

    private fun loadSessionFromPrefs() {
        val p = sessionPrefs ?: return
        val token = p.getString(KEY_ACCESS_TOKEN, null)
        val email = p.getString(KEY_EMAIL, null)
        val googleId = p.getString(KEY_GOOGLE_ID, null)
        val roleStr = p.getString(KEY_USER_ROLE, null)
        val role = if (!roleStr.isNullOrBlank()) UserRole.fromString(roleStr) else null
        val onboardingDone = p.getBoolean(KEY_ROLE_ONBOARDING_DONE, false) ||
                p.getBoolean(KEY_ROLE_EXPLICITLY_CHOSEN, false) ||
                isAccountOnboarded(googleId, email)

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
        val p = sessionPrefs ?: return
        val alreadyOnboardedLocally = isAccountOnboarded(auth.user.googleId, auth.user.email)
        // If not a brand new user (!auth.isNewUser) OR already onboarded on device OR server already provided a role:
        val isExistingUser = !auth.isNewUser || alreadyOnboardedLocally || (auth.user.userRole != null)
        val resolvedOnboarding = onboardingDone ?: isExistingUser

        if (resolvedOnboarding) {
            markAccountOnboarded(auth.user.googleId, auth.user.email, auth.user.userRole)
        }

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
            putBoolean(KEY_ROLE_EXPLICITLY_CHOSEN, resolvedOnboarding)
            apply()
        }
        _currentUserState.value = auth.user
        _isLoggedInState.value = true
        _isRoleOnboardingDoneState.value = resolvedOnboarding
    }

    fun saveUser(user: UserPayload) {
        val p = sessionPrefs ?: return
        p.edit().apply {
            putString(KEY_EMAIL, user.email)
            putString(KEY_NAME, user.name)
            putString(KEY_PICTURE, user.picture)
            putString(KEY_GOOGLE_ID, user.googleId)
            if (user.userRole != null) {
                putString(KEY_USER_ROLE, user.userRole.name)
            }
            apply()
        }
        if (user.userRole != null) {
            markAccountOnboarded(user.googleId, user.email, user.userRole)
        }
        _currentUserState.value = user
    }

    fun updateRole(role: UserRole) {
        val current = _currentUserState.value
        markAccountOnboarded(current?.googleId, current?.email, role)
        sessionPrefs?.edit()?.apply {
            putString(KEY_USER_ROLE, role.name)
            putBoolean(KEY_ROLE_EXPLICITLY_CHOSEN, true)
            putBoolean(KEY_ROLE_ONBOARDING_DONE, true)
            apply()
        }
        _isRoleOnboardingDoneState.value = true
        if (current != null) {
            _currentUserState.value = current.copy(userRole = role)
        }
    }

    fun setRoleOnboardingDone(done: Boolean) {
        val current = _currentUserState.value
        if (done) {
            markAccountOnboarded(current?.googleId, current?.email, current?.userRole)
        }
        sessionPrefs?.edit()?.apply {
            putBoolean(KEY_ROLE_EXPLICITLY_CHOSEN, done)
            putBoolean(KEY_ROLE_ONBOARDING_DONE, done)
            apply()
        }
        _isRoleOnboardingDoneState.value = done
    }

    fun isRoleOnboardingDone(): Boolean {
        val current = _currentUserState.value
        return isAccountOnboarded(current?.googleId, current?.email) ||
                (sessionPrefs?.getBoolean(KEY_ROLE_ONBOARDING_DONE, false) ?: false)
    }

    fun getAccessToken(): String? {
        return sessionPrefs?.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getUser(): UserPayload? {
        return _currentUserState.value
    }

    fun getUserRole(): UserRole {
        val roleStr = sessionPrefs?.getString(KEY_USER_ROLE, null)
        if (!roleStr.isNullOrBlank()) {
            return UserRole.fromString(roleStr)
        }
        val current = _currentUserState.value
        if (current != null) {
            val ap = accountPrefs
            val savedGidRole = ap?.getString("role_gid_${current.googleId}", null)
            if (!savedGidRole.isNullOrBlank()) return UserRole.fromString(savedGidRole)
            val savedEmailRole = ap?.getString("role_email_${current.email.lowercase().trim()}", null)
            if (!savedEmailRole.isNullOrBlank()) return UserRole.fromString(savedEmailRole)
        }
        return UserRole.PEMULA
    }

    fun clearSession() {
        // Clear active session (tokens, current user), preserving persistent account onboarded status
        sessionPrefs?.edit()?.clear()?.apply()
        _currentUserState.value = null
        _isLoggedInState.value = false
        _isRoleOnboardingDoneState.value = false
    }
}
