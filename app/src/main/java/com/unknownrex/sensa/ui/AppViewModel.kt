package com.unknownrex.sensa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unknownrex.sensa.core.domain.model.SensaSettings
import com.unknownrex.sensa.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Owns the settings snapshot the app shell needs before it can render anything: theme flags and
 * whether onboarding is done. Null until the first value arrives from DataStore.
 */
class AppViewModel(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<SensaSettings?> = settingsRepository.observeSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )
}
