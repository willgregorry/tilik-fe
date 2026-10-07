package id.tilik.app.data.repository

import id.tilik.app.data.client.ApiClientProvider
import id.tilik.app.data.model.AuthResponse
import id.tilik.app.data.model.GoogleAuthRequest
import id.tilik.app.data.model.HistoryDetailResponse
import id.tilik.app.data.model.HistoryListResponse
import id.tilik.app.data.model.UserPayload
import id.tilik.app.data.model.UserRole
import id.tilik.app.data.model.UserSettingsUpdate
import id.tilik.app.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

object AuthRepository {

    private val apiService by lazy { ApiClientProvider.createService() }

    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.loginWithGoogle(GoogleAuthRequest(idToken = idToken))
            SessionManager.saveAuthSession(response)
            Timber.i("Google Sign-In successful for: %s", response.user.email)
            Result.success(response)
        } catch (e: Exception) {
            Timber.e(e, "Google Sign-In failed with id_token")
            Result.failure(e)
        }
    }

    suspend fun fetchCurrentUser(): Result<UserPayload> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentUser()
            SessionManager.saveUser(response.user)
            Result.success(response.user)
        } catch (e: Exception) {
            Timber.e(e, "Fetch current user failed")
            Result.failure(e)
        }
    }

    suspend fun updateRole(role: UserRole): Result<UserPayload> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateUserSettings(UserSettingsUpdate(userRole = role))
            val resolvedRole = response.user.userRole ?: role
            SessionManager.updateRole(resolvedRole)
            Result.success(response.user)
        } catch (e: Exception) {
            Timber.e(e, "Update user role failed")
            // Even if offline, update locally in session
            SessionManager.updateRole(role)
            Result.failure(e)
        }
    }

    suspend fun getHistory(limit: Int = 20, offset: Int = 0): Result<HistoryListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHistory(limit = limit, offset = offset)
            Result.success(response)
        } catch (e: Exception) {
            Timber.e(e, "Fetch history failed")
            Result.failure(e)
        }
    }

    suspend fun getHistoryDetail(historyId: String): Result<HistoryDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHistoryDetail(historyId = historyId)
            Result.success(response)
        } catch (e: Exception) {
            Timber.e(e, "Fetch history detail failed for id: %s", historyId)
            Result.failure(e)
        }
    }

    suspend fun clearHistory(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.clearHistory()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Clear history failed")
            Result.failure(e)
        }
    }

    fun logout() {
        SessionManager.clearSession()
    }
}
