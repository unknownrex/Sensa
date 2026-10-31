plugins {
    id("sensa.android.library")
    id("sensa.android.room")
    id("sensa.android.junit5")
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("api", project(":core:domain"))
}
