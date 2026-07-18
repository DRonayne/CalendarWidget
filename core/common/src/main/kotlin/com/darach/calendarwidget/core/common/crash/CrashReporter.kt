package com.darach.calendarwidget.core.common.crash

/**
 * Handled-error reporting at the Result-mapping edges. Backed by Crashlytics
 * when Firebase is configured; no-op otherwise.
 */
interface CrashReporter {
    fun recordNonFatal(throwable: Throwable)

    fun log(message: String)
}

object NoOpCrashReporter : CrashReporter {
    override fun recordNonFatal(throwable: Throwable) = Unit

    override fun log(message: String) = Unit
}
