plugins {
    id("calendarwidget.android.library.compose")
}

android {
    namespace = "com.darach.calendarwidget.core.designsystem"
}

dependencies {
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.kotlinx.collections.immutable)
}
