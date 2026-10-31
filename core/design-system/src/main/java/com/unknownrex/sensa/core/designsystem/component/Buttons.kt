package com.unknownrex.sensa.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaShapes

@Composable
private fun Modifier.pressScale(pressed: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonPressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Filled primary action: 52 dp tall, subtle mint glow (design.md §9). */
@Composable
fun SensaPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = LocalSensaColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .pressScale(pressed)
            .shadow(
                elevation = 12.dp,
                shape = SensaShapes.sm,
                ambientColor = colors.accentGlow.copy(alpha = 0.20f),
                spotColor = colors.accentGlow.copy(alpha = 0.20f),
            ),
        shape = SensaShapes.sm,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.primary.copy(alpha = 0.38f),
            disabledContentColor = colors.onPrimary.copy(alpha = 0.38f),
        ),
        interactionSource = interactionSource,
        content = content,
    )
}

/** Tonal secondary action. */
@Composable
fun SensaSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = LocalSensaColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .pressScale(pressed),
        shape = SensaShapes.sm,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.surfaceVariant,
            contentColor = colors.onSurface,
            disabledContainerColor = colors.surfaceVariant.copy(alpha = 0.38f),
            disabledContentColor = colors.onSurface.copy(alpha = 0.38f),
        ),
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
fun SensaTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        content = content,
    )
}

/** Destructive action, tonal error. */
@Composable
fun SensaDestructiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = LocalSensaColors.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = SensaShapes.sm,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.error,
            disabledContentColor = colors.error.copy(alpha = 0.38f),
        ),
        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f)),
        content = content,
    )
}

@Composable
fun SensaButtonLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge)
}
