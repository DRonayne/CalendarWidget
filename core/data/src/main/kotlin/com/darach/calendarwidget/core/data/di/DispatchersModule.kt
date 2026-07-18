package com.darach.calendarwidget.core.data.di

import com.darach.calendarwidget.core.common.coroutines.CwDispatchers
import com.darach.calendarwidget.core.common.coroutines.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @Dispatcher(CwDispatchers.IO)
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(CwDispatchers.Default)
    fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
