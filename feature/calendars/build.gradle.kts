plugins {
    id("calendarwidget.android.feature")
}

android {
    namespace = "com.darach.calendarwidget.feature.calendars"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
}
