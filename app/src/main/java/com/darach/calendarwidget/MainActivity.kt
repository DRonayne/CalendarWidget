package com.darach.calendarwidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import com.darach.calendarwidget.core.designsystem.theme.CalendarWidgetTheme
import com.darach.calendarwidget.firebase.RemoteConfigFeatureFlags
import com.darach.calendarwidget.widget.observer.CalendarChangeObserver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var refresher: WidgetRefresher

    @Inject
    lateinit var calendarChangeObserver: CalendarChangeObserver

    @Inject
    lateinit var remoteConfigFlags: RemoteConfigFeatureFlags

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val configureWidgetId = configureAppWidgetId()
        if (configureWidgetId != null) {
            // Widget is deleted unless RESULT_OK arrives; be explicit up front.
            setResult(RESULT_CANCELED, resultIntent(configureWidgetId))
        }
        val startRoute: NavKey =
            if (configureWidgetId != null) ConfigureRoute(configureWidgetId) else SettingsRoute
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()

        setContent {
            CalendarWidgetTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PermissionGate(
                        onGranted = {
                            calendarChangeObserver.register()
                            refresher.requestRefresh(RefreshReason.APP_OPENED)
                        },
                    ) {
                        MainNavigation(
                            startRoute = startRoute,
                            versionName = versionName,
                            onFinishConfigure = { appWidgetId ->
                                setResult(RESULT_OK, resultIntent(appWidgetId))
                                finish()
                            },
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        remoteConfigFlags.fetchOnAppOpen()
        refresher.requestRefresh(RefreshReason.APP_OPENED)
    }

    private fun configureAppWidgetId(): Int? {
        if (intent.action != AppWidgetManager.ACTION_APPWIDGET_CONFIGURE) return null
        val id =
            intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            )
        return id.takeIf { it != AppWidgetManager.INVALID_APPWIDGET_ID }
    }

    private fun resultIntent(appWidgetId: Int): Intent =
        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
}
