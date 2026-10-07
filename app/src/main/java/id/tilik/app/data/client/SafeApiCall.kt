package id.tilik.app.data.client

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    apiCall: suspend () -> T
): NetworkResult<T> = withContext(dispatcher) {
    try {
        val result = apiCall()
        NetworkResult.Success(result)
    } catch (e: HttpException) {
        val code = e.code()
        val errorBody = e.response()?.errorBody()?.string() ?: e.message()
        Timber.tag("TILIK_NETWORK").e(e, "SafeApiCall HTTP $code Error: $errorBody")
        NetworkResult.Error(code = code, message = errorBody)
    } catch (e: SocketTimeoutException) {
        Timber.tag("TILIK_NETWORK").e(e, "SafeApiCall Timeout: ${e.message}")
        NetworkResult.Exception(RuntimeException("Koneksi ke server timeout. Silakan periksa jaringan Anda.", e))
    } catch (e: IOException) {
        Timber.tag("TILIK_NETWORK").e(e, "SafeApiCall Network I/O Error: ${e.message}")
        NetworkResult.Exception(RuntimeException("Tidak dapat terhubung ke server backend Tilik. Pastikan server aktif.", e))
    } catch (e: Throwable) {
        Timber.tag("TILIK_NETWORK").e(e, "SafeApiCall Unexpected Error: ${e.message}")
        NetworkResult.Exception(e)
    }
}
