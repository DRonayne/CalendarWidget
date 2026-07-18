package com.darach.calendarwidget.core.data.repository

import com.darach.calendarwidget.core.model.DomainError
import com.darach.calendarwidget.core.model.DomainException
import kotlinx.coroutines.CancellationException

/**
 * The single wrapper for ContentProvider access (the project's safeApiCall
 * analogue): maps platform failures to sealed [DomainError]s at the edge.
 */
@Suppress("TooGenericExceptionCaught")
suspend inline fun <T> safeProviderCall(crossinline block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (security: SecurityException) {
        Result.failure(DomainException(DomainError.PermissionMissing, security))
    } catch (domain: DomainException) {
        Result.failure(domain)
    } catch (other: Exception) {
        Result.failure(DomainException(DomainError.QueryFailed(other.message), other))
    }

/** Thrown inside [safeProviderCall] when the provider returns a null cursor. */
fun providerUnavailable(): Nothing = throw DomainException(DomainError.ProviderUnavailable)
