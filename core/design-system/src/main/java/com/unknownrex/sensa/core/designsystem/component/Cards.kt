package com.unknownrex.sensa.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaAmountStyles
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaShapes
import com.unknownrex.sensa.core.designsystem.theme.SensaSpacing

/** Solid card for dense content. Glass is reserved for floating surfaces. */
@Composable
fun SensaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = LocalSensaColors.current
    val base = modifier
        .clip(SensaShapes.md)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .background(colors.surface)

    Box(modifier = base, content = { content() })
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = LocalSensaColors.current.onSurface,
        )
        action?.invoke()
    }
}

/**
 * Transaction row: 40 dp tinted category icon, two-line text and a right-aligned tabular amount.
 * Rows are always solid, never glass (design.md §8.3).
 */
@Composable
fun TransactionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    amount: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    trailingLabel: String? = null,
) {
    val colors = LocalSensaColors.current
    val amountStyles = LocalSensaAmountStyles.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .clip(SensaShapes.md)
            .clickable(onClick = onClick)
            .padding(horizontal = SensaSpacing.md, vertical = SensaSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SensaSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (trailingLabel != null) {
                    Text(
                        text = trailingLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = iconTint,
                    )
                }
            }
        }

        Text(
            text = amount,
            style = amountStyles.amount,
            color = colors.onSurface,
        )
    }
}
