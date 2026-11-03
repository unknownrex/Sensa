package com.unknownrex.sensa.feature.onboarding.presentation

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unknownrex.sensa.core.designsystem.component.SensaCard
import com.unknownrex.sensa.core.designsystem.component.SensaMark
import com.unknownrex.sensa.core.designsystem.component.SensaPrimaryButton
import com.unknownrex.sensa.core.designsystem.component.SensaSecondaryButton
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaShapes
import com.unknownrex.sensa.core.designsystem.theme.SensaSpacing
import com.unknownrex.sensa.core.presentation.ObserveAsEvents
import com.unknownrex.sensa.feature.onboarding.domain.InstalledPaymentApp
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingRoot(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // The result is not needed: CaptureNotifier checks areNotificationsEnabled() before posting.
    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) {}

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            OnboardingEvent.OpenNotificationSettings -> context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )

            // Sensa's own App info page, not the global battery list: the per-app page is where
            // the battery setting lives on every OEM skin. No REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            // which needs a restricted permission the direct dialog would require.
            OnboardingEvent.OpenBatterySettings -> context.startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    "package:${context.packageName}".toUri(),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )

            OnboardingEvent.RequestNotificationPermission ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

            OnboardingEvent.Finished -> onFinished()
        }
    }

    // Returning from either system sheet is the only source of truth for "granted".
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onAction(OnboardingAction.OnRefreshAccess)
    }

    OnboardingScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun OnboardingScreen(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = SensaSpacing.screen, vertical = SensaSpacing.lg),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        StepDots(step = state.step)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = SensaSpacing.md),
            contentAlignment = Alignment.Center,
        ) {
            when (state.step) {
                OnboardingStep.Welcome -> WelcomeStep()
                OnboardingStep.Permission -> PermissionStep(installedApps = state.installedApps)
                OnboardingStep.Battery -> BatteryStep()
                OnboardingStep.Notify -> NotifyStep()
                OnboardingStep.Ready -> ReadyStep()
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SensaSpacing.xs),
        ) {
            when (state.step) {
                OnboardingStep.Welcome, OnboardingStep.Permission, OnboardingStep.Battery -> {
                    when (state.step) {
                        OnboardingStep.Permission -> {
                            SensaPrimaryButton(
                                onClick = { onAction(OnboardingAction.OnGrantAccess) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = "Allow access", style = MaterialTheme.typography.labelLarge)
                            }
                            SensaSecondaryButton(
                                onClick = { onAction(OnboardingAction.OnNext) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = "Later", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        OnboardingStep.Battery -> {
                            SensaPrimaryButton(
                                onClick = { onAction(OnboardingAction.OnRequestBatteryExemption) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = "Allow", style = MaterialTheme.typography.labelLarge)
                            }
                            SensaSecondaryButton(
                                onClick = { onAction(OnboardingAction.OnNext) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = "Later", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        else -> SensaPrimaryButton(
                            onClick = { onAction(OnboardingAction.OnNext) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = "Get started", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                OnboardingStep.Notify -> {
                    SensaPrimaryButton(
                        onClick = { onAction(OnboardingAction.OnNotifyChoice(enabled = true)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = "Yes, notify me", style = MaterialTheme.typography.labelLarge)
                    }
                    SensaSecondaryButton(
                        onClick = { onAction(OnboardingAction.OnNotifyChoice(enabled = false)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = "No thanks", style = MaterialTheme.typography.labelLarge)
                    }
                }

                OnboardingStep.Ready -> SensaPrimaryButton(
                    onClick = { onAction(OnboardingAction.OnFinish) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Open Sensa", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun StepDots(step: OnboardingStep) {
    val colors = LocalSensaColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SensaSpacing.xxs, Alignment.CenterHorizontally),
    ) {
        OnboardingStep.entries.forEach { candidate ->
            Box(
                modifier = Modifier
                    .size(if (candidate == step) 20.dp else 8.dp, 8.dp)
                    .clip(CircleShape)
                    .background(if (candidate == step) colors.primary else colors.outline),
            )
        }
    }
}

@Composable
private fun WelcomeStep() {
    val colors = LocalSensaColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SensaSpacing.md),
    ) {
        SensaMark(size = 96.dp)
        Text(
            text = "Sensa",
            style = MaterialTheme.typography.displayLarge,
            color = colors.onSurface,
        )
        Text(
            text = "Know where your money goes, without typing a single transaction.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PermissionStep(installedApps: List<InstalledPaymentApp>) {
    val colors = LocalSensaColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SensaSpacing.md),
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsActive,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = "Notification access",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Android only shares payment notifications with apps you allow. Sensa reads " +
                "them on this device and sends nothing anywhere.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceMuted,
            textAlign = TextAlign.Center,
        )

        SensaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(SensaSpacing.md),
                verticalArrangement = Arrangement.spacedBy(SensaSpacing.xs),
            ) {
                if (installedApps.isEmpty()) {
                    Text(
                        text = "No supported payment app found yet. You can still add " +
                            "transactions by hand.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceMuted,
                    )
                } else {
                    installedApps.forEach { app ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SensaSpacing.xs),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteryStep() {
    val colors = LocalSensaColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SensaSpacing.md),
    ) {
        Icon(
            imageVector = Icons.Outlined.BatterySaver,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = "Run in the background",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Android pauses apps to save battery, and Sensa then misses payment " +
                "notifications. Allow background usage for Sensa so it keeps running.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceMuted,
            textAlign = TextAlign.Center,
        )

        SensaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(SensaSpacing.md),
                verticalArrangement = Arrangement.spacedBy(SensaSpacing.xs),
            ) {
                Text(
                    text = "The button opens Sensa's App info. Battery lives there under one " +
                        "of these names: App battery usage, Battery, or Battery saver.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceMuted,
                )
                Text(
                    text = "Turn off any restriction you find. You can change it any time in " +
                        "Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun NotifyStep() {
    val colors = LocalSensaColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SensaSpacing.md),
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsActive,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = "Get notified?",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Sensa can send you a notification each time a spending is added, so you " +
                "know it was recorded. You can change this later in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReadyStep() {
    val colors = LocalSensaColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SensaSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(SensaShapes.full)
                .background(colors.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(48.dp),
            )
        }
        Text(
            text = "You're set",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface,
        )
        Text(
            text = "Pay as usual. Sensa shows up with categorized transactions, and asks when " +
                "it isn't sure.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceMuted,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(SensaSpacing.xs))
    }
}
