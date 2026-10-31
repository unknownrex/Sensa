package com.unknownrex.sensa.feature.onboarding.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.unknownrex.sensa.core.domain.model.PaymentSource
import com.unknownrex.sensa.core.domain.model.SensaSettings
import com.unknownrex.sensa.core.domain.repository.SettingsRepository
import com.unknownrex.sensa.feature.onboarding.domain.InstalledPaymentApp
import com.unknownrex.sensa.feature.onboarding.domain.PaymentAppDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val settings = FakeSettingsRepository()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `steps move forward and back without underflowing`() {
        val viewModel = viewModel()

        viewModel.onAction(OnboardingAction.OnBack)
        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Welcome)

        viewModel.onAction(OnboardingAction.OnNext)
        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Permission)

        viewModel.onAction(OnboardingAction.OnNext)
        viewModel.onAction(OnboardingAction.OnNext)
        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Ready)

        viewModel.onAction(OnboardingAction.OnBack)
        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Permission)
    }

    @Test
    fun `installed apps are detected on creation`() {
        val viewModel = viewModel()

        assertThat(viewModel.state.value.installedApps.map { it.packageName })
            .isEqualTo(listOf("com.gojek.app"))
    }

    @Test
    fun `finishing stores the flag`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(OnboardingAction.OnFinish)

        assertThat(settings.settings.value.onboardingCompleted).isTrue()
    }

    private fun viewModel() = OnboardingViewModel(FakeDetector(), settings)

    private class FakeDetector : PaymentAppDetector {
        override fun detectInstalledApps(): List<InstalledPaymentApp> = listOf(
            InstalledPaymentApp("com.gojek.app", PaymentSource.GOPAY, "GoPay"),
        )
    }

    private class FakeSettingsRepository : SettingsRepository {
        val settings = MutableStateFlow(SensaSettings())

        override fun observeSettings(): Flow<SensaSettings> = settings

        override suspend fun setOnboardingCompleted(completed: Boolean) {
            settings.value = settings.value.copy(onboardingCompleted = completed)
        }

        override suspend fun setDynamicColor(enabled: Boolean) {
            settings.value = settings.value.copy(dynamicColor = enabled)
        }

        override suspend fun setReduceTransparency(enabled: Boolean) {
            settings.value = settings.value.copy(reduceTransparency = enabled)
        }

        override suspend fun setReduceMotion(enabled: Boolean) {
            settings.value = settings.value.copy(reduceMotion = enabled)
        }
    }
}
