package com.unknownrex.sensa.core.designsystem.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors

/**
 * The ambient glow behind glass surfaces (design.md §3.3). Drawn once at the app root, never per
 * list item, and static unless motion is explicitly requested.
 */
@Composable
fun AmbientGlow(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    animated: Boolean = false,
) {
    val dark = isSystemInDarkTheme()
    val colors = LocalSensaColors.current
    val baseAlpha = if (dark) 0.22f else 0.10f

    val mint = if (dark) Color(0xFF3DDC97) else colors.primary
    val teal = if (dark) Color(0xFF1AA6A6) else colors.primary
    val blue = if (dark) Color(0xFF3B82F6) else colors.primary

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        fun blob(center: Offset, radius: Float, color: Color, alpha: Float) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }

        blob(
            center = Offset(w * -0.05f, h * 0.02f),
            radius = w * 0.85f,
            color = mint,
            alpha = baseAlpha * intensity,
        )
        blob(
            center = Offset(w * 1.1f, h * 0.12f),
            radius = w * 0.7f,
            color = teal,
            alpha = baseAlpha * 0.8f * intensity,
        )
        blob(
            center = Offset(w * 0.75f, h * 1.05f),
            radius = w * 0.6f,
            color = blue,
            alpha = baseAlpha * 0.55f * intensity,
        )
    }
}

/** Lower-contrast variant for text-heavy screens such as Transactions and Settings. */
@Composable
fun AmbientGlowCalm(modifier: Modifier = Modifier) {
    AmbientGlow(modifier = modifier, intensity = 0.35f)
}
