<div align="center">

<img src="docs/assets/app-icon.png" width="120" alt="Calendar Agenda Widget icon">

# Calendar Agenda Widget

</div>

<img src="docs/assets/widget-screenshot.png" width="100%" alt="Screenshot of the widget on a home screen showing an agenda of upcoming events">

<br>

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin%202.3-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Material 3](https://img.shields.io/badge/Material%203-6750A4?logo=materialdesign&logoColor=white)](https://m3.material.io)
[![API](https://img.shields.io/badge/API%2036%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white)](https://gradle.org)
<br>
[![Release](https://img.shields.io/github/v/release/DRonayne/CalendarWidget?include_prereleases&label=Release&logo=github&labelColor=181717&color=181717)](https://github.com/DRonayne/CalendarWidget/releases/latest)
[![Firebase](https://img.shields.io/badge/Firebase-F57C00?logo=firebase&logoColor=white)](https://firebase.google.com)
[![Android CLI](https://img.shields.io/badge/Android%20CLI-0D2818?logo=android&logoColor=3DDC84)](https://developer.android.com/tools/agents/android-cli)
![Fable 5](https://img.shields.io/badge/Fable%205-D97757?logo=claude&logoColor=white)

</div>

A simple calendar agenda widget for Android. A glanceable timeline of your upcoming events, pulled in from your calendar(s).


It's a remake of an older agenda widget I used for years that's not supported on Android 17+.

The architecture here is complete overkill for a widget app. But I wanted to add in patterns and tooling I went for on bigger projects. Multi-module structure, convention plugins, static analysis, baseline profiles, CI, automated releases, etc.

- Kotlin, Jetpack Compose, and Glance for the widget itself
- Modularised into `app`, `widget`, `feature:*`, and `core:*` modules with convention plugins in `build-logic`
- Hilt for DI, DataStore for preferences, WorkManager for background refresh
- Detekt, ktlint (via Spotless), Konsist, and dependency guard
- Kover for coverage, baseline profiles for startup performance
- GitHub Actions CI with Renovate and release-please handling dependencies and versioning

## Download

APK from the [latest release](https://github.com/DRonayne/CalendarWidget/releases/latest).
