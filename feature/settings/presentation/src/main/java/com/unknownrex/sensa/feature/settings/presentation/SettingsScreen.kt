package com.unknownrex.sensa.feature.settings.presentation

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unknownrex.sensa.core.designsystem.component.SensaCard
import com.unknownrex.sensa.core.designsystem.component.SensaSecondaryButton
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaSpacing
import com.unknownrex.sensa.core.presentation.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoot(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onAction(SettingsAction.OnRefreshNotificationAccess)
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SettingsEvent.OpenNotificationSettings -> {
                context.startActivity(
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
    }

    SettingsScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
) {
    val colors = LocalSensaColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = SensaSpacing.screen,
            end = SensaSpacing.screen,
            top = SensaSpacing.lg,
            bottom = SensaSpacing.xxl + SensaSpacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(SensaSpacing.md),
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onSurface,
            )
        }

        item {
            SensaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(SensaSpacing.md)) {
                    Text(
                        text = "Capture",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceMuted,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = SensaSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SensaSpacing.sm),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (state.notificationAccessEnabled) colors.success else colors.warning,
                                ),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notification access",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSurface,
                            )
                            Text(
                                text = if (state.notificationAccessEnabled) {
                                    "Enabled — payments are captured on this device."
                                } else {
                                    "Off — Sensa cannot read GoPay or BCA notifications yet."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceMuted,
                            )
                        }
                    }
                    SensaSecondaryButton(
                        onClick = { onAction(SettingsAction.OnOpenNotificationSettings) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = if (state.notificationAccessEnabled) "Manage access" else "Grant access",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }

        item {
            SensaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = SensaSpacing.md, vertical = SensaSpacing.xs)) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceMuted,
                        modifier = Modifier.padding(top = SensaSpacing.sm, bottom = SensaSpacing.xs),
                    )
                    SettingsSwitchRow(
                        title = "Dynamic color",
                        subtitle = "Follow the system wallpaper palette. Off keeps Sensa mint.",
                        checked = state.settings.dynamicColor,
                        onCheckedChange = { onAction(SettingsAction.OnDynamicColorChange(it)) },
                    )
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.4f))
                    SettingsSwitchRow(
                        title = "Reduce transparency",
                        subtitle = "Replace glass surfaces with solid cards.",
                        checked = state.settings.reduceTransparency,
                        onCheckedChange = { onAction(SettingsAction.OnReduceTransparencyChange(it)) },
                    )
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.4f))
                    SettingsSwitchRow(
                        title = "Reduce motion",
                        subtitle = "Turn off chart and list entrance animations.",
                        checked = state.settings.reduceMotion,
                        onCheckedChange = { onAction(SettingsAction.OnReduceMotionChange(it)) },
                    )
                }
            }
        }

        item {
            SensaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(SensaSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(SensaSpacing.xxs),
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceMuted,
                    )
                    Text(
                        text = "Sensa ${state.appVersion}",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                    )
                    Text(
                        text = "Your transactions never leave this device. Sensa has no internet " +
                            "permission and no account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = LocalSensaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = SensaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SensaSpacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceMuted,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.onSurfaceMuted,
                uncheckedTrackColor = colors.surfaceVariant,
            ),
        )
    }
}
