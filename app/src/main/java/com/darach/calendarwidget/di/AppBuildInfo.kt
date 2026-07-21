package com.darach.calendarwidget.di

import com.darach.calendarwidget.BuildConfig
import com.darach.calendarwidget.core.common.buildinfo.BuildInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBuildInfo
    @Inject
    constructor() : BuildInfo {
        override val isDebug: Boolean = BuildConfig.DEBUG
    }
