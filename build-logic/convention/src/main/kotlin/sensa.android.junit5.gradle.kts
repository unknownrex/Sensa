import com.unknownrex.sensa.buildlogic.lib
import org.gradle.api.tasks.testing.Test

plugins {
    id("de.mannodermaus.android-junit5")
}

dependencies {
    add("testImplementation", platform(lib("junit-bom")))
    add("testImplementation", lib("junit-jupiter"))
    add("testRuntimeOnly", lib("junit-platform-launcher"))
    add("testImplementation", lib("assertk"))
    add("testImplementation", lib("turbine"))
    add("testImplementation", lib("kotlinx-coroutines-test"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
