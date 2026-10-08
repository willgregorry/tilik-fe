package id.tilik.app.data.client

import android.content.Context
import id.tilik.app.BuildConfig
import id.tilik.app.data.api.TilikApiService
import id.tilik.app.data.session.SessionManager
import kotlinx.serialization.json.Json
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object ApiClientProvider {

    private val defaultBaseUrl = BuildConfig.BASE_URL

    @Volatile
    private var activeHost: String? = null

    private fun getPersistedHost(): String? {
        val ctx = SessionManager.appContext ?: return activeHost
        val prefs = ctx.getSharedPreferences("tilik_network_prefs", Context.MODE_PRIVATE)
        return prefs.getString("active_host", null)
    }

    private fun persistHost(host: String) {
        activeHost = host
        val ctx = SessionManager.appContext ?: return
        val prefs = ctx.getSharedPreferences("tilik_network_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("active_host", host).apply()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        val token = SessionManager.getAccessToken()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        } else if (BuildConfig.SECTORS_API_KEY.isNotBlank()) {
            requestBuilder.header("Authorization", BuildConfig.SECTORS_API_KEY)
        }

        if (BuildConfig.SECTORS_API_KEY.isNotBlank()) {
            requestBuilder.header("X-API-KEY", BuildConfig.SECTORS_API_KEY)
        }
        chain.proceed(requestBuilder.build())
    }

    private val localFallbackInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val originalHost = originalUrl.host

        val isLocalAddress = originalHost.equals("localhost", ignoreCase = true) ||
                originalHost == "127.0.0.1" ||
                originalHost == "10.0.2.2" ||
                originalHost == "192.168.100.29"

        if (!isLocalAddress) {
            return@Interceptor chain.proceed(originalRequest)
        }

        val rememberedHost = activeHost ?: getPersistedHost()
        val targetHost = rememberedHost ?: originalHost

        val targetRequest = if (targetHost != originalHost) {
            val newUrl = originalUrl.newBuilder().host(targetHost).build()
            originalRequest.newBuilder().url(newUrl).build()
        } else {
            originalRequest
        }

        // Fast-connect timeout for initial local probing (2000ms max)
        val initialChain = if (chain.connectTimeoutMillis() > 2000) {
            chain.withConnectTimeout(2000, TimeUnit.MILLISECONDS)
        } else {
            chain
        }

        try {
            val response = initialChain.proceed(targetRequest)
            if (activeHost != targetHost) {
                persistHost(targetHost)
            }
            response
        } catch (e: java.io.IOException) {
            val fallbackHost = when (targetHost) {
                "localhost", "127.0.0.1", "10.0.2.2" -> "192.168.100.29"
                "192.168.100.29" -> "127.0.0.1"
                else -> null
            }

            if (fallbackHost != null) {
                timber.log.Timber.w("Local request to %s failed (%s). Retrying with fallback %s...", targetHost, e.message, fallbackHost)
                val fallbackUrl = originalUrl.newBuilder().host(fallbackHost).build()
                val fallbackRequest = originalRequest.newBuilder().url(fallbackUrl).build()
                val fallbackResponse = chain.withConnectTimeout(3500, TimeUnit.MILLISECONDS).proceed(fallbackRequest)
                persistHost(fallbackHost)
                timber.log.Timber.i("Fallback to %s succeeded! Stored as active host.", fallbackHost)
                fallbackResponse
            } else {
                throw e
            }
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .dns(object : okhttp3.Dns {
            override fun lookup(hostname: String): List<java.net.InetAddress> {
                if (hostname.equals("localhost", ignoreCase = true)) {
                    return listOf(
                        java.net.InetAddress.getByAddress("localhost", byteArrayOf(127, 0, 0, 1))
                    )
                }
                return okhttp3.Dns.SYSTEM.lookup(hostname)
            }
        })
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(localFallbackInterceptor)
        .addInterceptor(loggingInterceptor)
        .retryOnConnectionFailure(true)
        .build()

    fun createService(baseUrl: String = defaultBaseUrl): TilikApiService {
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(TilikApiService::class.java)
    }

    fun createFallbackService(): TilikApiService {
        val fallbackUrl = if (defaultBaseUrl.contains("localhost")) {
            "http://192.168.100.29:8000/"
        } else {
            "http://localhost:8000/"
        }
        return createService(fallbackUrl)
    }
}
