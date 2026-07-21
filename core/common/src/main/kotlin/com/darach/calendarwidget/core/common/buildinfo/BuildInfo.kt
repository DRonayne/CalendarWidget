package com.darach.calendarwidget.core.common.buildinfo

/** Whether this is a debuggable build — gates extra diagnostic detail in the UI. */
interface BuildInfo {
    val isDebug: Boolean
}
