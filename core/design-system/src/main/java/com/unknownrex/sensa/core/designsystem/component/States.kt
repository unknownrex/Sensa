package com.unknownrex.sensa.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaShapes
import com.unknownrex.sensa.core.designsystem.theme.SensaSpacing

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = LocalSensaColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SensaSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SensaSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = colors.onSurfaceMuted)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceMuted,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            SensaPrimaryButton(onClick = onAction) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSensaColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SensaSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SensaSpacing.sm),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.error,
            textAlign = TextAlign.Center,
        )
        SensaTextButton(onClick = onRetry) {
            Text(text = "Retry", style = MaterialTheme.typography.labelLarge, color = colors.primary)
        }
    }
}

/**
 * Skeleton shimmer for list loading. The animation is read in the draw phase so it never
 * recomposes the row.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = SensaShapes.sm,
) {
    val colors = LocalSensaColors.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift = transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerShift",
    )
    val base = colors.surfaceVariant
    val highlight = colors.outline.copy(alpha = 0.55f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(base)
            .drawShimmer(shift = { shift.value }, base = base, highlight = highlight),
    )
}

private fun Modifier.drawShimmer(
    shift: () -> Float,
    base: androidx.compose.ui.graphics.Color,
    highlight: androidx.compose.ui.graphics.Color,
): Modifier = drawBehind {
    val x = shift() * size.width
    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(x - size.width * 0.4f, 0f),
        end = Offset(x, size.height),
    )
    drawRect(brush = brush)
}

@Composable
fun ShimmerTransactionRows(
    count: Int = 6,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SensaSpacing.xs),
    ) {
        repeat(count) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SensaSpacing.md, vertical = SensaSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SensaSpacing.sm),
            ) {
                ShimmerBox(modifier = Modifier.size(40.dp), shape = CircleShape)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(SensaSpacing.xxs),
                ) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.55f).height(14.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.35f).height(12.dp))
                }
                ShimmerBox(modifier = Modifier.size(width = 64.dp, height = 16.dp))
            }
        }
    }
}
