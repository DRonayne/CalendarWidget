package com.darach.calendarwidget.core.data.di

import com.darach.calendarwidget.core.data.config.WidgetConfigRepository
import com.darach.calendarwidget.core.data.config.WidgetConfigRepositoryImpl
import com.darach.calendarwidget.core.data.repository.CalendarRepository
import com.darach.calendarwidget.core.data.repository.CalendarRepositoryImpl
import com.darach.calendarwidget.core.data.snapshot.SnapshotRepository
import com.darach.calendarwidget.core.data.snapshot.SnapshotRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun calendarRepository(impl: CalendarRepositoryImpl): CalendarRepository

    @Binds
    abstract fun widgetConfigRepository(impl: WidgetConfigRepositoryImpl): WidgetConfigRepository

    @Binds
    abstract fun snapshotRepository(impl: SnapshotRepositoryImpl): SnapshotRepository
}
