package com.unknownrex.sensa.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaAmountStyles
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaShapes
import com.unknownrex.sensa.core.designsystem.theme.SensaSpacing

/**
 * Solid input on surfaceVariant with a 12 dp radius and a clear primary focus ring. Labels sit
 * above the field (design.md §9).
 */
@Composable
fun SensaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = LocalSensaColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceMuted,
            modifier = Modifier.padding(bottom = SensaSpacing.xxs, start = SensaSpacing.xxs),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            isError = isError,
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = placeholder?.let {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceMuted,
                    )
                }
            },
            leadingIcon = leading,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = SensaShapes.sm,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                disabledContainerColor = colors.surfaceVariant.copy(alpha = 0.5f),
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline,
                errorBorderColor = colors.error,
                focusedTextColor = colors.onSurface,
                unfocusedTextColor = colors.onSurface,
                cursorColor = colors.primary,
            ),
        )
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) colors.error else colors.onSurfaceMuted,
                modifier = Modifier.padding(top = SensaSpacing.xxs, start = SensaSpacing.xxs),
            )
        }
    }
}

/** Large display-style amount field used by the add/edit sheets. */
@Composable
fun SensaAmountField(    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    val colors = LocalSensaColors.current
    val amountStyles = LocalSensaAmountStyles.current
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isError,
            textStyle = amountStyles.hero.copy(color = colors.onSurface),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = {
                Text(
                    text = "Rp",
                    style = amountStyles.hero.copy(color = colors.onSurfaceMuted),
                )
            },
            shape = SensaShapes.sm,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline,
                errorBorderColor = colors.error,
                cursorColor = colors.primary,
            ),
        )
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) colors.error else colors.onSurfaceMuted,
                modifier = Modifier.padding(top = SensaSpacing.xxs, start = SensaSpacing.xxs),
            )
        }
    }
}
