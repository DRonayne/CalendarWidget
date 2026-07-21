package com.darach.calendarwidget.core.model

import kotlinx.serialization.Serializable

/** Recoverable failures surfaced to UI/widget as states, never as crashes. */
@Serializable
sealed interface DomainError {
    /** READ_CALENDAR (or READ_CONTACTS where relevant) is not granted. */
    @Serializable
    data object PermissionMissing : DomainError

    /** The calendar provider is missing or refused the connection. */
    @Serializable
    data object ProviderUnavailable : DomainError

    @Serializable
    data class QueryFailed(
        val message: String?,
    ) : DomainError
}

/** Carrier making [DomainError] transportable through kotlin.Result. */
class DomainException(
    val error: DomainError,
    cause: Throwable? = null,
) : Exception(error.toString(), cause)

/** The [DomainError] behind a failed Result, if it failed with one. */
fun Throwable.domainError(): DomainError? = (this as? DomainException)?.error
