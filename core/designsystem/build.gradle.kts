plugins {
    id("calendarwidget.android.library.compose")
}

android {
    namespace = "com.darach.calendarwidget.core.designsystem"
}

dependencies {
    implementation(libs.kotlinx.collections.immutable)
}
