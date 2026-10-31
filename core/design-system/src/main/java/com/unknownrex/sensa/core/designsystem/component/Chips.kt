package com.unknownrex.sensa.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaShapes
import com.unknownrex.sensa.core.designsystem.theme.SensaSpacing

/**
 * Category chip: tinted with the category color, icon plus label so color is never the only
 * signal. Selected state is a stronger tint plus a 1 dp border (design.md §9).
 */
@Composable
fun CategoryChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .heightIn(min = SensaSpacing.minTouchTarget)
            .clip(SensaShapes.sm)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .background(color.copy(alpha = if (selected) 0.28f else 0.16f))
            .then(if (selected) Modifier.border(BorderStroke(1.dp, color), SensaShapes.sm) else Modifier)
            .padding(horizontal = SensaSpacing.sm, vertical = SensaSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = LocalSensaColors.current.onSurface,
        )
    }
}

/** Filter chip for lists and search. */
@Composable
fun SensaFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSensaColors.current
    CategoryChip(
        label = label,
        color = if (selected) colors.primary else colors.onSurfaceMuted,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    )
}
