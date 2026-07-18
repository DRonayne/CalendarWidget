plugins {
    id("calendarwidget.android.library.compose")
    id("calendarwidget.hilt")
}

android {
    namespace = "com.darach.calendarwidget.widget"

    testOptions {
        // Required by the Glance unit-test API (androidx.glance:glance-appwidget-testing):
        // it touches Bundle/DisplayMetrics stubs that must return defaults on the JVM.
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.glance.appwidget.testing)
}
