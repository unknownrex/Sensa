import com.unknownrex.sensa.buildlogic.lib
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

extensions.configure<KotlinJvmProjectExtension> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    add("implementation", lib("kotlinx-coroutines-core"))
    add("implementation", lib("kotlinx-serialization-json"))

    add("testImplementation", platform(lib("junit-bom")))
    add("testImplementation", lib("junit-jupiter"))
    add("testImplementation", lib("assertk"))
    add("testImplementation", lib("turbine"))
    add("testImplementation", lib("kotlinx-coroutines-test"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
