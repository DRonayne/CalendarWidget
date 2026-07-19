package com.darach.calendarwidget.firebase

import android.content.Context
import android.os.Bundle
import com.darach.calendarwidget.core.common.analytics.Analytics
import com.darach.calendarwidget.core.common.analytics.AnalyticsEvent
import com.darach.calendarwidget.core.common.crash.CrashReporter
import com.darach.calendarwidget.core.common.flags.FeatureFlags
import com.darach.calendarwidget.core.common.flags.Flag
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase activates only when a google-services.json was present at build
 * time AND FirebaseApp initialized. Without it, everything degrades to no-ops
 * (personal builds stay fully functional offline).
 */
@Singleton
class FirebaseAvailability
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        val available: Boolean by lazy { FirebaseApp.getApps(context).isNotEmpty() }
    }

@Singleton
class CrashlyticsReporter
    @Inject
    constructor(
        private val availability: FirebaseAvailability,
    ) : CrashReporter {
        override fun recordNonFatal(throwable: Throwable) {
            if (availability.available) FirebaseCrashlytics.getInstance().recordException(throwable)
        }

        override fun log(message: String) {
            if (availability.available) FirebaseCrashlytics.getInstance().log(message)
        }
    }

/**
 * Firebase Analytics sink for the typed [Analytics] facade. Advertising-ID
 * collection and ad-personalization signals are disabled in the manifest;
 * event content is bounded by the [AnalyticsEvent] privacy contract.
 */
@Singleton
class FirebaseAnalyticsSink
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val availability: FirebaseAvailability,
    ) : Analytics {
        override fun track(event: AnalyticsEvent) {
            if (!availability.available) return
            val bundle = Bundle()
            for ((key, value) in event.params) {
                when (value) {
                    is Boolean -> bundle.putLong(key, if (value) 1L else 0L)
                    is Int -> bundle.putLong(key, value.toLong())
                    is Long -> bundle.putLong(key, value)
                    is Double -> bundle.putDouble(key, value)
                    else -> bundle.putString(key, value.toString())
                }
            }
            FirebaseAnalytics.getInstance(context).logEvent(event.name, bundle)
        }
    }

/**
 * Remote Config flags, fetched on app open only (accepted staleness — these
 * are kill-switch semantics for experimental features).
 */
@Singleton
class RemoteConfigFeatureFlags
    @Inject
    constructor(
        private val availability: FirebaseAvailability,
    ) : FeatureFlags {
        private val remoteConfig: FirebaseRemoteConfig? by lazy {
            if (!availability.available) return@lazy null
            FirebaseRemoteConfig.getInstance().apply {
                setConfigSettingsAsync(
                    remoteConfigSettings {
                        minimumFetchIntervalInSeconds = TimeUnit.HOURS.toSeconds(FETCH_INTERVAL_HOURS)
                    },
                )
                setDefaultsAsync(Flag.entries.associate { it.key to it.defaultValue })
            }
        }

        fun fetchOnAppOpen() {
            remoteConfig?.fetchAndActivate()
        }

        override fun isEnabled(flag: Flag): Boolean = remoteConfig?.getBoolean(flag.key) ?: flag.defaultValue

        private companion object {
            const val FETCH_INTERVAL_HOURS = 6L
        }
    }
