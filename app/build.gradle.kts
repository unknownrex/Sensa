plugins {
    id("sensa.android.application")
    id("sensa.android.compose.application")
    id("sensa.android.koin")
    id("sensa.android.serialization")
    id("sensa.android.junit5")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:presentation"))
    implementation(project(":core:design-system"))
    implementation(project(":core:data"))
    implementation(project(":core:capture"))

    implementation(project(":feature:onboarding:data"))
    implementation(project(":feature:onboarding:presentation"))
    implementation(project(":feature:dashboard:presentation"))
    implementation(project(":feature:transactions:presentation"))
    implementation(project(":feature:review:presentation"))
    implementation(project(":feature:settings:presentation"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
