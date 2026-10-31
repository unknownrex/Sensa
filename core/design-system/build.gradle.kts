plugins {
    id("sensa.android.library")
    id("sensa.android.compose.library")
}

dependencies {
    api(project(":core:domain"))
    implementation(libs.haze)
}
