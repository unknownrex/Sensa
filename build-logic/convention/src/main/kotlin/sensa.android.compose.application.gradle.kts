import com.android.build.api.dsl.ApplicationExtension
import com.unknownrex.sensa.buildlogic.lib

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.configure<ApplicationExtension> {
    buildFeatures {
        compose = true
    }
}

dependencies {
    add("implementation", platform(lib("androidx-compose-bom")))
    add("implementation", lib("androidx-ui"))
    add("implementation", lib("androidx-ui-graphics"))
    add("implementation", lib("androidx-ui-tooling-preview"))
    add("implementation", lib("androidx-material3"))
    add("implementation", lib("androidx-material-icons-extended"))
    add("debugImplementation", lib("androidx-ui-tooling"))
    add("debugImplementation", lib("androidx-ui-test-manifest"))

    add("androidTestImplementation", platform(lib("androidx-compose-bom")))
    add("androidTestImplementation", lib("androidx-ui-test-junit4"))
    add("androidTestImplementation", lib("androidx-test-junit"))
}
