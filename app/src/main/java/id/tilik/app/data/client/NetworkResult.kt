package id.tilik.app.data.client

sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>

    val isSuccess: Boolean
        get() = this is Success

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Exception -> throwable
        is Error -> RuntimeException("HTTP $code: $message")
        else -> null
    }
}

inline fun <T> NetworkResult<T>.onSuccess(action: (value: T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

inline fun <T> NetworkResult<T>.onFailure(action: (exception: Throwable) -> Unit): NetworkResult<T> {
    when (this) {
        is NetworkResult.Error -> action(RuntimeException("HTTP $code: $message"))
        is NetworkResult.Exception -> action(throwable)
        is NetworkResult.Success -> Unit
    }
    return this
}
