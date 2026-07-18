package com.darach.calendarwidget.core.data.di

import android.content.ContentResolver
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.darach.calendarwidget.core.data.datastore.JsonDataStoreSerializer
import com.darach.calendarwidget.core.model.SnapshotStore
import com.darach.calendarwidget.core.model.WidgetConfigStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    fun contentResolver(
        @ApplicationContext context: Context,
    ): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    @Named("widgetConfig")
    fun widgetConfigDataStore(
        @ApplicationContext context: Context,
    ): DataStore<WidgetConfigStore> =
        DataStoreFactory.create(
            serializer = JsonDataStoreSerializer(WidgetConfigStore.serializer(), WidgetConfigStore()),
            corruptionHandler = ReplaceFileCorruptionHandler { WidgetConfigStore() },
            produceFile = { context.dataStoreFile("widget_config.json") },
        )

    @Provides
    @Singleton
    @Named("agendaSnapshot")
    fun snapshotDataStore(
        @ApplicationContext context: Context,
    ): DataStore<SnapshotStore> =
        DataStoreFactory.create(
            serializer = JsonDataStoreSerializer(SnapshotStore.serializer(), SnapshotStore()),
            corruptionHandler = ReplaceFileCorruptionHandler { SnapshotStore() },
            produceFile = { context.dataStoreFile("agenda_snapshot.json") },
        )
}
