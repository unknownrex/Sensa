package com.unknownrex.sensa.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unknownrex.sensa.core.domain.repository.SettingsRepository
import com.unknownrex.sensa.feature.onboarding.domain.InstalledPaymentApp
import com.unknownrex.sensa.feature.onboarding.domain.PaymentAppDetector
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep { Welcome, Permission, Ready }

/** What Sensa can tell the user about capture before it ever asks for the permission. */
data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    val installedApps: List<InstalledPaymentApp> = emptyList(),
)

sealed interface OnboardingAction {
    data object OnNext : OnboardingAction
    data object OnBack : OnboardingAction
    data object OnGrantAccess : OnboardingAction
    data object OnFinish : OnboardingAction
}

sealed interface OnboardingEvent {
    data object OpenNotificationSettings : OnboardingEvent
    data object Finished : OnboardingEvent
}

/**
 * Onboarding never claims to know whether access is granted: the system sheet is the only source
 * of truth, and the banner on Transactions surfaces it once the user is past onboarding.
 */
class OnboardingViewModel(
    private val paymentAppDetector: PaymentAppDetector,
    private val settingsRepository: SettingsRepository,
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

            OnboardingAction.OnFinish -> viewModelScope.launch {
                settingsRepository.setOnboardingCompleted(true)
                _events.send(OnboardingEvent.Finished)
            }
        }
    }
}

private fun OnboardingStep.next(): OnboardingStep = when (this) {
    OnboardingStep.Welcome -> OnboardingStep.Permission
    OnboardingStep.Permission -> OnboardingStep.Ready
    OnboardingStep.Ready -> OnboardingStep.Ready
}

private fun OnboardingStep.previous(): OnboardingStep = when (this) {
    OnboardingStep.Welcome -> OnboardingStep.Welcome
    OnboardingStep.Permission -> OnboardingStep.Welcome
    OnboardingStep.Ready -> OnboardingStep.Permission
}
