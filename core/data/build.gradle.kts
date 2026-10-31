plugins {
    id("sensa.android.library")
    id("sensa.android.koin")
    id("sensa.android.junit5")
}

dependencies {
    add("api", project(":core:domain"))
    add("api", project(":core:database"))
    add("implementation", libs.androidx.datastore.preferences)
}
