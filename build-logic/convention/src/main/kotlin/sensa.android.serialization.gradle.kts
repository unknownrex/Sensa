import com.unknownrex.sensa.buildlogic.lib

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    add("implementation", lib("kotlinx-serialization-json"))
}
