package com.unknownrex.sensa.core.domain

/** Marker for every typed failure in the app. */
interface Error

/**
 * Typed result used across layers. Deliberately distinct from [kotlin.Result] so failures carry
 * a domain error instead of a throwable.
 */
sealed interface Result<out D, out E : com.unknownrex.sensa.core.domain.Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : com.unknownrex.sensa.core.domain.Error>(val error: E) : Result<Nothing, E>
}

typealias EmptyResult<E> = Result<Unit, E>

inline fun <T, E : Error, R> Result<T, E>.map(transform: (T) -> R): Result<R, E> = when (this) {
    is Result.Error -> Result.Error(error)
    is Result.Success -> Result.Success(transform(data))
}

inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T, E : Error> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> {
    if (this is Result.Error) action(error)
    return this
}

fun <T, E : Error> Result<T, E>.asEmptyResult(): EmptyResult<E> = map { }

fun <T, E : Error> Result<T, E>.dataOrNull(): T? = (this as? Result.Success)?.data
