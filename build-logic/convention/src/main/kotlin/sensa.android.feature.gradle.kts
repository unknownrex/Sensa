import com.unknownrex.sensa.buildlogic.lib

plugins {
    id("sensa.android.library")
    id("sensa.android.compose.library")
    id("sensa.android.koin")
    id("sensa.android.serialization")
}

dependencies {
    add("api", project(":core:domain"))
    add("implementation", project(":core:presentation"))
    add("implementation", project(":core:design-system"))
    add("implementation", lib("androidx-lifecycle-runtime-compose"))
    add("implementation", lib("androidx-lifecycle-viewmodel-compose"))
    add("implementation", lib("androidx-navigation-compose"))
}
