plugins {
    id("calendarwidget.android.library")
    id("calendarwidget.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.darach.calendarwidget.core.data"
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":core:domain"))

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
}
