package com.darach.calendarwidget.core.common.coroutines

import javax.inject.Qualifier

/**
 * Qualifier for injected [kotlinx.coroutines.CoroutineDispatcher]s.
 * Never reference Dispatchers.* at call sites — inject these instead.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(
    val dispatcher: CwDispatchers,
)

enum class CwDispatchers {
    Default,
    IO,
}
