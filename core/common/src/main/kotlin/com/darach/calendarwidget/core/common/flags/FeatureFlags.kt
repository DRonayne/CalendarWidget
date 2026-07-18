package com.darach.calendarwidget.core.common.flags

/**
 * Remote flags gate risky/experimental features only. Backed by Firebase
 * Remote Config in the app; defaults apply when no fetch has happened.
 */
interface FeatureFlags {
    fun isEnabled(flag: Flag): Boolean
}

enum class Flag(
    val key: String,
    val defaultValue: Boolean,
) {
    /** SDK-37-only snap-scroll agenda mode (Glance VerticalScrollMode). */
    SNAP_SCROLL("snap_scroll_enabled", false),
}

/** Static defaults — used until Remote Config is wired and as its fallback. */
object DefaultFeatureFlags : FeatureFlags {
    override fun isEnabled(flag: Flag): Boolean = flag.defaultValue
}
