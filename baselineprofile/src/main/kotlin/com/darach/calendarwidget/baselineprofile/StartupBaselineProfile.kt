package com.darach.calendarwidget.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Cold-start profile for the config UI (the widget renders out-of-process). */
@RunWith(AndroidJUnit4::class)
class StartupBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(packageName = "com.darach.calendarwidget") {
            pressHome()
            startActivityAndWait()
        }
    }
}
