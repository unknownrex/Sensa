plugins {
    id("sensa.android.library")
    id("sensa.android.compose.library")
    id("sensa.android.junit5")
}

dependencies {
    api(project(":core:domain"))
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.lifecycle.runtime.ktx)
}
