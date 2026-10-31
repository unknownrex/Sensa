package com.unknownrex.sensa.core.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.unknownrex.sensa.core.domain.model.SensaSettings
import com.unknownrex.sensa.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sensaSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sensa_settings",
)

fun provideSettingsDataStore(context: Context): DataStore<Preferences> = context.sensaSettingsDataStore

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override fun observeSettings(): Flow<SensaSettings> = dataStore.data.map { prefs ->
        SensaSettings(
            onboardingCompleted = prefs[Keys.OnboardingCompleted] ?: false,
            dynamicColor = prefs[Keys.DynamicColor] ?: false,
            reduceTransparency = prefs[Keys.ReduceTransparency] ?: false,
            reduceMotion = prefs[Keys.ReduceMotion] ?: false,
        )
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.OnboardingCompleted] = completed }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DynamicColor] = enabled }
    }

    override suspend fun setReduceTransparency(enabled: Boolean) {
        dataStore.edit { it[Keys.ReduceTransparency] = enabled }
    }

    override suspend fun setReduceMotion(enabled: Boolean) {
        dataStore.edit { it[Keys.ReduceMotion] = enabled }
    }

    private object Keys {
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val DynamicColor = booleanPreferencesKey("dynamic_color")
        val ReduceTransparency = booleanPreferencesKey("reduce_transparency")
        val ReduceMotion = booleanPreferencesKey("reduce_motion")
    }
}
