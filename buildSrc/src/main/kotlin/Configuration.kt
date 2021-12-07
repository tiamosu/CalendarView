@file:Suppress("unused", "SpellCheckingInspection")

object Android {
    const val applicationId = "com.tiamosu.calendarview.app"

    const val compileSdk = 31
    const val minSdk = 21
    const val targetSdk = 30
    const val versionName = "1.0"
    const val versionCode = 1
}

object Versions {
    const val kotlin = "1.6.0"
    const val appcompat = "1.4.0"
    const val constraintlayout = "2.1.2"
    const val recyclerview = "1.2.1"
}

object Deps {
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val constraintlayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}"
    const val recyclerview = "androidx.recyclerview:recyclerview:${Versions.recyclerview}"
}