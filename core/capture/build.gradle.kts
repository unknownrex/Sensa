plugins {
    id("sensa.android.library")
    id("sensa.android.koin")
    id("sensa.android.junit5")
}

dependencies {
    add("api", project(":core:domain"))
}
