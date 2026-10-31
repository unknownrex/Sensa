package com.unknownrex.sensa.feature.settings.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unknownrex.sensa.core.capture.NotificationAccessChecker
import com.unknownrex.sensa.core.domain.model.SensaSettings
import com.unknownrex.sensa.core.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val settings: SensaSettings = SensaSettings(),
    val notificationAccessEnabled: Boolean = false,
    val appVersion: String = "",
    val isLoading: Boolean = true,
)

sealed interface SettingsAction {
    data class OnDynamicColorChange(val enabled: Boolean) : SettingsAction
    data class OnReduceTransparencyChange(val enabled: Boolean) : SettingsAction
    data class OnReduceMotionChange(val enabled: Boolean) : SettingsAction
    data object OnOpenNotificationSettings : SettingsAction
    data object OnRefreshNotificationAccess : SettingsAction
}

sealed interface SettingsEvent {
    data object OpenNotificationSettings : SettingsEvent
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val notificationAccessChecker: NotificationAccessChecker,
    context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(appVersion = context.appVersion()),
    )
    val state = _state.asStateFlow()

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observeSettings().collect { settings ->
                _state.update { it.copy(settings = settings, isLoading = false) }
            }
        }
        refreshNotificationAccess()
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnDynamicColorChange -> viewModelScope.launch {
                settingsRepository.setDynamicColor(action.enabled)
            }

            is SettingsAction.OnReduceTransparencyChange -> viewModelScope.launch {
                settingsRepository.setReduceTransparency(action.enabled)
            }

            is SettingsAction.OnReduceMotionChange -> viewModelScope.launch {
                settingsRepository.setReduceMotion(action.enabled)
            }

            SettingsAction.OnOpenNotificationSettings -> viewModelScope.launch {
                _events.send(SettingsEvent.OpenNotificationSettings)
            }

            SettingsAction.OnRefreshNotificationAccess -> refreshNotificationAccess()
        }
    }

    private fun refreshNotificationAccess() {
        _state.update {
            it.copy(notificationAccessEnabled = notificationAccessChecker.isEnabled())
        }
    }
}

@Suppress("DEPRECATION")
private fun Context.appVersion(): String = try {
    packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
} catch (e: Exception) {
    ""
}
