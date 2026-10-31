plugins {
    id("sensa.android.feature")
    id("sensa.android.junit5")
}

dependencies {
    implementation(project(":feature:onboarding:domain"))
}
