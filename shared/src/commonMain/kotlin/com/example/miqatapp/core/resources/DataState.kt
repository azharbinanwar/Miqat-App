package com.example.miqatapp.core.resources

/**
 * Success / Error / Loading wrapper — the KMP take on the Flutter `DataState`.
 * Sealed so `when` is exhaustive (no double-dispatch callback API).
 */
sealed interface DataState<out T> {
    data class Success<T>(val data: T) : DataState<T>
    data class Error(val message: String, val cause: Throwable? = null) : DataState<Nothing>
    data object Loading : DataState<Nothing>
}

fun <T> DataState<T>.getOrNull(): T? = (this as? DataState.Success)?.data

inline fun <T> DataState<T>.onSuccess(block: (T) -> Unit): DataState<T> {
    if (this is DataState.Success) block(data)
    return this
}

inline fun <T> DataState<T>.onError(block: (String) -> Unit): DataState<T> {
    if (this is DataState.Error) block(message)
    return this
}
