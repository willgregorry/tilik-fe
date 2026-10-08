package id.tilik.app.data.repository

import android.content.Context
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
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

private data class CacheEntry<T>(
    val data: T,
    val timestamp: Long
)

object AuthRepository {

    private val apiService by lazy { ApiClientProvider.createService() }

    private const val PREFS_HISTORY = "tilik_history_cache"
    private const val KEY_PERSISTED_HISTORY = "persisted_history_json"
    private const val KEY_PERSISTED_TS = "persisted_history_ts"

    private val historyJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    // In-memory cache for History List (5 min TTL)
    private var historyListCache: CacheEntry<HistoryListResponse>? = null
    private const val HISTORY_CACHE_TTL = 5 * 60 * 1000L

    // In-memory cache for History Detail by ID (15 min TTL)
    private val historyDetailCache = ConcurrentHashMap<String, CacheEntry<HistoryDetailResponse>>()
    private const val DETAIL_CACHE_TTL = 15 * 60 * 1000L

    // In-memory cache for User (10 min TTL)
    private var currentUserCache: CacheEntry<UserPayload>? = null
    private const val USER_CACHE_TTL = 10 * 60 * 1000L

    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.loginWithGoogle(GoogleAuthRequest(idToken = idToken))
            SessionManager.saveAuthSession(response)
            currentUserCache = CacheEntry(response.user, System.currentTimeMillis())
            invalidateHistoryCache()
            Timber.i("Google Sign-In successful for: %s", response.user.email)
            Result.success(response)
        } catch (e: Exception) {
            val httpEx = e as? HttpException
            val errorBody = try { httpEx?.response()?.errorBody()?.string() } catch (_: Exception) { null }
            val detailMsg = if (httpEx != null) {
                "HTTP ${httpEx.code()} ${httpEx.message()}${if (!errorBody.isNullOrBlank()) " -> $errorBody" else ""}"
            } else {
                "${e.javaClass.simpleName}: ${e.message}"
            }
            Timber.e(e, "Google Sign-In failed with id_token: %s", detailMsg)
            Result.failure(Exception(detailMsg, e))
        }
    }

    suspend fun fetchCurrentUser(forceRefresh: Boolean = false): Result<UserPayload> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cached = currentUserCache
        if (!forceRefresh && cached != null && (now - cached.timestamp < USER_CACHE_TTL)) {
            return@withContext Result.success(cached.data)
        }
        val localUser = SessionManager.getUser()
        if (!forceRefresh && localUser != null && cached == null) {
            currentUserCache = CacheEntry(localUser, now)
            return@withContext Result.success(localUser)
        }

        try {
            val response = apiService.getCurrentUser()
            SessionManager.saveUser(response.user)
            currentUserCache = CacheEntry(response.user, now)
            Result.success(response.user)
        } catch (e: Exception) {
            Timber.e(e, "Fetch current user failed")
            if (localUser != null) {
                Result.success(localUser)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun updateRole(role: UserRole): Result<UserPayload> = withContext(Dispatchers.IO) {
        // Immediately persist locally in session
        SessionManager.updateRole(role)
        try {
            val response = apiService.updateUserSettings(UserSettingsUpdate(userRole = role))
            val resolvedRole = response.user.userRole ?: role
            SessionManager.updateRole(resolvedRole)
            currentUserCache = CacheEntry(response.user, System.currentTimeMillis())
            Timber.i("Role updated on server to: %s", resolvedRole)
            Result.success(response.user)
        } catch (e: Exception) {
            Timber.w(e, "Update user role on backend failed, kept local preference: %s", role)
            val fallbackUser = SessionManager.getUser()?.copy(userRole = role)
                ?: UserPayload(email = "", name = "User", googleId = "", userRole = role)
            currentUserCache = CacheEntry(fallbackUser, System.currentTimeMillis())
            Result.success(fallbackUser)
        }
    }

    private fun persistHistory(history: HistoryListResponse) {
        try {
            val ctx = SessionManager.appContext ?: return
            val jsonStr = historyJson.encodeToString(HistoryListResponse.serializer(), history)
            ctx.getSharedPreferences(PREFS_HISTORY, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PERSISTED_HISTORY, jsonStr)
                .putLong(KEY_PERSISTED_TS, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            Timber.w(e, "Failed to persist history to disk cache")
        }
    }

    private fun loadPersistedHistory(): CacheEntry<HistoryListResponse>? {
        try {
            val ctx = SessionManager.appContext ?: return null
            val prefs = ctx.getSharedPreferences(PREFS_HISTORY, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_PERSISTED_HISTORY, null) ?: return null
            val ts = prefs.getLong(KEY_PERSISTED_TS, 0L)
            val data = historyJson.decodeFromString(HistoryListResponse.serializer(), jsonStr)
            val entry = CacheEntry(data, ts)
            historyListCache = entry
            return entry
        } catch (e: Exception) {
            Timber.w(e, "Failed to load persisted history from disk")
            return null
        }
    }

    private fun clearPersistedHistory() {
        try {
            val ctx = SessionManager.appContext ?: return
            ctx.getSharedPreferences(PREFS_HISTORY, Context.MODE_PRIVATE).edit().clear().apply()
        } catch (e: Exception) {
            Timber.w(e, "Failed to clear persisted history")
        }
    }

    suspend fun getHistory(
        limit: Int = 20,
        offset: Int = 0,
        forceRefresh: Boolean = false
    ): Result<HistoryListResponse> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cached = historyListCache ?: loadPersistedHistory()

        // Return cache if still valid and not explicitly refreshing
        if (!forceRefresh && cached != null && (now - cached.timestamp < HISTORY_CACHE_TTL)) {
            Timber.d("Serving history list from cache (%d items)", cached.data.items.size)
            return@withContext Result.success(cached.data)
        }

        try {
            val response = apiService.getHistory(limit = limit, offset = offset)
            historyListCache = CacheEntry(response, now)
            persistHistory(response)
            Timber.d("Fetched fresh history from network (%d items)", response.items.size)
            Result.success(response)
        } catch (e: Exception) {
            Timber.e(e, "Fetch history failed")
            // Graceful offline / stale fallback (from memory or disk)
            val fallback = cached ?: loadPersistedHistory()
            if (fallback != null) {
                Timber.w("Serving disk/stale cached history as network failed (%d items)", fallback.data.items.size)
                Result.success(fallback.data)
            } else {
                Result.failure(e)
            }
        }
    }

    fun getCachedHistory(): HistoryListResponse? {
        val cached = historyListCache ?: loadPersistedHistory()
        return cached?.data
    }

    suspend fun getHistoryDetail(
        historyId: String,
        forceRefresh: Boolean = false
    ): Result<HistoryDetailResponse> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cached = historyDetailCache[historyId]

        if (!forceRefresh && cached != null && (now - cached.timestamp < DETAIL_CACHE_TTL)) {
            Timber.d("Serving history detail for %s from cache", historyId)
            return@withContext Result.success(cached.data)
        }

        try {
            val response = apiService.getHistoryDetail(historyId = historyId)
            historyDetailCache[historyId] = CacheEntry(response, now)
            Result.success(response)
        } catch (e: Exception) {
            Timber.e(e, "Fetch history detail failed for id: %s", historyId)
            if (cached != null) {
                Timber.w("Serving stale cached history detail for %s", historyId)
                Result.success(cached.data)
            } else {
                Result.failure(e)
            }
        }
    }

    fun getCachedHistoryDetail(historyId: String): HistoryDetailResponse? {
        return historyDetailCache[historyId]?.data
    }

    fun cacheVerificationDetail(verification: id.tilik.app.data.model.VerificationResponse, claimText: String? = null): String {
        val id = verification.historyId?.takeIf { it.isNotBlank() } ?: "temp_${verification.ticker ?: "IDX"}_${System.currentTimeMillis()}"
        val detail = HistoryDetailResponse(
            status = "success",
            id = id,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()),
            tweetText = claimText ?: verification.summary ?: "Klaim Saham ${verification.ticker ?: ""}",
            sourcePlatform = "x",
            verification = verification
        )
        historyDetailCache[id] = CacheEntry(detail, System.currentTimeMillis())
        return id
    }

    fun getCachedHistoryDetailByTicker(ticker: String): HistoryDetailResponse? {
        val cleanTicker = ticker.trim().uppercase()
        return historyDetailCache.values
            .map { it.data }
            .firstOrNull { it.verification.ticker?.uppercase() == cleanTicker }
    }

    fun invalidateHistoryCache() {
        historyListCache = null
        historyDetailCache.clear()
        Timber.d("History cache invalidated")
    }

    suspend fun clearHistory(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.clearHistory()
            clearPersistedHistory()
            invalidateHistoryCache()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Clear history failed")
            clearPersistedHistory()
            invalidateHistoryCache()
            Result.failure(e)
        }
    }

    fun logout() {
        invalidateHistoryCache()
        currentUserCache = null
        SessionManager.clearSession()
    }
}
