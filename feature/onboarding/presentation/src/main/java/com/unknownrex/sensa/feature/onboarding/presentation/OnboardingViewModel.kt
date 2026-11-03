package com.unknownrex.sensa.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unknownrex.sensa.core.capture.CaptureAccessChecks
import com.unknownrex.sensa.core.domain.repository.SettingsRepository
import com.unknownrex.sensa.feature.onboarding.domain.InstalledPaymentApp
import com.unknownrex.sensa.feature.onboarding.domain.PaymentAppDetector
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep { Welcome, Permission, Battery, Notify, Ready }

/** What Sensa can tell the user about capture before it ever asks for the permission. */
data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    val installedApps: List<InstalledPaymentApp> = emptyList(),
    val accessGranted: Boolean = false,
    val batteryUnrestricted: Boolean = false,
)

sealed interface OnboardingAction {
    data object OnNext : OnboardingAction
    data object OnBack : OnboardingAction
    data object OnGrantAccess : OnboardingAction
    data object OnRequestBatteryExemption : OnboardingAction
    data object OnRefreshAccess : OnboardingAction
    data class OnNotifyChoice(val enabled: Boolean) : OnboardingAction
    data object OnFinish : OnboardingAction
}

sealed interface OnboardingEvent {
    data object OpenNotificationSettings : OnboardingEvent
    data object OpenBatterySettings : OnboardingEvent
    data object RequestNotificationPermission : OnboardingEvent
    data object Finished : OnboardingEvent
}

/**
 * Permission steps are re-checked on resume rather than trusted from a callback, so granting in the
 * system sheet advances the flow and a refusal leaves the user where they were.
 */
class OnboardingViewModel(
    private val paymentAppDetector: PaymentAppDetector,
    private val settingsRepository: SettingsRepository,
    private val accessChecks: CaptureAccessChecks,
) : ViewModel() {

    private val _state = MutableStateFlow(
        OnboardingState(installedApps = paymentAppDetector.detectInstalledApps()),
    )
    val state = _state.asStateFlow()

    private val _events = Channel<OnboardingEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.OnNext -> _state.update { it.copy(step = it.step.next()) }
            OnboardingAction.OnBack -> _state.update { it.copy(step = it.step.previous()) }

            OnboardingAction.OnGrantAccess -> viewModelScope.launch {
                _events.send(OnboardingEvent.OpenNotificationSettings)
            }

            OnboardingAction.OnRequestBatteryExemption -> viewModelScope.launch {
                _events.send(OnboardingEvent.OpenBatterySettings)
            }

            OnboardingAction.OnRefreshAccess -> _state.update {
                val access = accessChecks.isNotificationAccessEnabled()
                val battery = accessChecks.isUnrestrictedByBatterySaver()
                // Only a fresh grant advances; returning to the app must not skip a step.
                val next = when {
                    it.step == OnboardingStep.Permission && access && !it.accessGranted -> it.step.next()
                    it.step == OnboardingStep.Battery && battery && !it.batteryUnrestricted -> it.step.next()
                    else -> it.step
                }
                it.copy(accessGranted = access, batteryUnrestricted = battery, step = next)
            }

            is OnboardingAction.OnNotifyChoice -> viewModelScope.launch {
                settingsRepository.setNotifyOnCapture(action.enabled)
                if (action.enabled) _events.send(OnboardingEvent.RequestNotificationPermission)
                _state.update { it.copy(step = it.step.next()) }
            }

            OnboardingAction.OnFinish -> viewModelScope.launch {
                settingsRepository.setOnboardingCompleted(true)
                _events.send(OnboardingEvent.Finished)
            }
        }
    }
}

private fun OnboardingStep.next(): OnboardingStep = when (this) {
    OnboardingStep.Welcome -> OnboardingStep.Permission
    OnboardingStep.Permission -> OnboardingStep.Battery
    OnboardingStep.Battery -> OnboardingStep.Notify
    OnboardingStep.Notify -> OnboardingStep.Ready
    OnboardingStep.Ready -> OnboardingStep.Ready
}

private fun OnboardingStep.previous(): OnboardingStep = when (this) {
    OnboardingStep.Welcome -> OnboardingStep.Welcome
    OnboardingStep.Permission -> OnboardingStep.Welcome
    OnboardingStep.Battery -> OnboardingStep.Permission
    OnboardingStep.Notify -> OnboardingStep.Battery
    OnboardingStep.Ready -> OnboardingStep.Notify
}
