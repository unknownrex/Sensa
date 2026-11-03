package com.unknownrex.sensa.feature.onboarding.presentation

import assertk.assertThat
import app.cash.turbine.test
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.unknownrex.sensa.core.capture.CaptureAccessChecks
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
    private val checks = FakeAccessChecks()

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
        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Battery)

        viewModel.onAction(OnboardingAction.OnNext)
        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Notify)

        viewModel.onAction(OnboardingAction.OnNext)
        viewModel.onAction(OnboardingAction.OnNext)
        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Ready)

        viewModel.onAction(OnboardingAction.OnBack)
        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Notify)
    }

    @Test
    fun `granting notification access on resume advances to the battery step`() {
        val viewModel = viewModel()
        viewModel.onAction(OnboardingAction.OnNext)

        checks.access = true
        viewModel.onAction(OnboardingAction.OnRefreshAccess)

        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Battery)
        assertThat(viewModel.state.value.accessGranted).isTrue()
    }

    @Test
    fun `granting the battery exemption on resume advances to the notify step`() {
        val viewModel = viewModel()
        repeat(2) { viewModel.onAction(OnboardingAction.OnNext) }

        checks.battery = true
        viewModel.onAction(OnboardingAction.OnRefreshAccess)

        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Notify)
        assertThat(viewModel.state.value.batteryUnrestricted).isTrue()
    }

    @Test
    fun `resume without a grant does not advance twice`() {
        val viewModel = viewModel()
        viewModel.onAction(OnboardingAction.OnNext)

        checks.access = true
        viewModel.onAction(OnboardingAction.OnRefreshAccess)
        // Second resume with the grant still in place must not skip the battery step.
        viewModel.onAction(OnboardingAction.OnRefreshAccess)

        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Battery)
    }

    @Test
    fun `accepting notifications stores the choice and requests permission`() = runTest {
        val viewModel = viewModel()
        repeat(3) { viewModel.onAction(OnboardingAction.OnNext) }

        viewModel.events.test {
            viewModel.onAction(OnboardingAction.OnNotifyChoice(enabled = true))
            assertThat(awaitItem()).isEqualTo(OnboardingEvent.RequestNotificationPermission)
        }
        assertThat(settings.settings.value.notifyOnCapture).isTrue()
        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Ready)
    }

    @Test
    fun `declining notifications stores false and skips the permission`() = runTest {
        val viewModel = viewModel()
        repeat(3) { viewModel.onAction(OnboardingAction.OnNext) }

        viewModel.events.test {
            viewModel.onAction(OnboardingAction.OnNotifyChoice(enabled = false))
            expectNoEvents()
        }
        assertThat(settings.settings.value.notifyOnCapture).isFalse()
        assertThat(viewModel.state.value.step).isEqualTo(OnboardingStep.Ready)
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

    private fun viewModel() =
        OnboardingViewModel(FakeDetector(), settings, checks)

    private class FakeDetector : PaymentAppDetector {
        override fun detectInstalledApps(): List<InstalledPaymentApp> = listOf(
            InstalledPaymentApp("com.gojek.app", PaymentSource.GOPAY, "GoPay"),
        )
    }

    private class FakeAccessChecks(
        var access: Boolean = false,
        var battery: Boolean = false,
    ) : CaptureAccessChecks {
        override fun isNotificationAccessEnabled(): Boolean = access
        override fun isUnrestrictedByBatterySaver(): Boolean = battery
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

        override suspend fun setNotifyOnCapture(enabled: Boolean) {
            settings.value = settings.value.copy(notifyOnCapture = enabled)
        }
    }
}
