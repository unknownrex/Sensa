package com.unknownrex.sensa.core.designsystem.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unknownrex.sensa.core.designsystem.theme.LocalReduceTransparency
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaHaze
import com.unknownrex.sensa.core.designsystem.theme.SensaShapes
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * Glass recipe from design.md §5.5. Blur requires API 31+; Haze falls back to its tint on older
 * devices and [LocalReduceTransparency] swaps the whole recipe for a solid surface.
 */
@Composable
fun Modifier.glassSurface(
    shape: Shape = SensaShapes.lg,
    blurRadius: Dp = 22.dp,
    fillAlpha: Float = 1f,
): Modifier {
    val colors = LocalSensaColors.current
    val reduceTransparency = LocalReduceTransparency.current
    val hazeState = LocalSensaHaze.current

    val solid = clip(shape)
        .background(colors.surface.copy(alpha = 0.94f))
        .border(1.dp, colors.outline, shape)

    if (reduceTransparency) return solid

    val style = remember(colors, blurRadius) {
        HazeStyle(
            backgroundColor = colors.surface.copy(alpha = 0.30f),
            tints = listOf(HazeTint(colors.glassFill)),
            blurRadius = blurRadius,
            noiseFactor = 0.02f,
        )
    }

    return clip(shape)
        .hazeEffect(state = hazeState, style = style)
        .background(colors.glassFill.copy(alpha = colors.glassFill.alpha * fillAlpha))
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(colors.glassBorderStart, colors.glassBorderEnd),
            ),
            shape = shape,
        )
}

/** Floating glass container. Maximum two visible per screen (design.md §5.4). */
@Composable
fun GlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = SensaShapes.lg,
    blurRadius: Dp = 22.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.glassSurface(shape = shape, blurRadius = blurRadius),
        content = content,
    )
}
