package id.tilik.app.data.client

import id.tilik.app.BuildConfig
import id.tilik.app.data.api.TilikApiService
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
        if (BuildConfig.SECTORS_API_KEY.isNotBlank()) {
            requestBuilder.header("Authorization", BuildConfig.SECTORS_API_KEY)
            requestBuilder.header("X-API-KEY", BuildConfig.SECTORS_API_KEY)
        }
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
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
            "http://10.110.120.199:8000/"
        } else {
            "http://localhost:8000/"
        }
        return createService(fallbackUrl)
    }
}
