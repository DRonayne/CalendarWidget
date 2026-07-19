package com.darach.calendarwidget.di

import com.darach.calendarwidget.core.common.analytics.Analytics
import com.darach.calendarwidget.core.common.crash.CrashReporter
import com.darach.calendarwidget.core.common.flags.FeatureFlags
import com.darach.calendarwidget.core.data.refresh.PlacedWidgets
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import com.darach.calendarwidget.firebase.CrashlyticsReporter
import com.darach.calendarwidget.firebase.FirebaseAnalyticsSink
import com.darach.calendarwidget.firebase.RemoteConfigFeatureFlags
import com.darach.calendarwidget.widget.refresh.PlacedWidgetsImpl
import com.darach.calendarwidget.widget.refresh.WidgetRefresherImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun widgetRefresher(impl: WidgetRefresherImpl): WidgetRefresher

    @Binds
    abstract fun placedWidgets(impl: PlacedWidgetsImpl): PlacedWidgets

    @Binds
    abstract fun crashReporter(impl: CrashlyticsReporter): CrashReporter

    @Binds
    abstract fun featureFlags(impl: RemoteConfigFeatureFlags): FeatureFlags

    @Binds
    abstract fun analytics(impl: FirebaseAnalyticsSink): Analytics
}
