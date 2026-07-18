plugins {
    id("calendarwidget.jvm.library")
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:common"))

    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)
}
