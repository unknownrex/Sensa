import com.google.devtools.ksp.gradle.KspExtension
import com.unknownrex.sensa.buildlogic.lib

plugins {
    id("com.google.devtools.ksp")
}

extensions.configure<KspExtension> {
    arg("room.generateKotlin", "true")
}

dependencies {
    // `api` so consumers of a module that exposes a RoomDatabase see RoomDatabase itself.
    add("api", lib("androidx-room-runtime"))
    add("implementation", lib("androidx-room-ktx"))
    add("ksp", lib("androidx-room-compiler"))
}
