package com.unknownrex.sensa.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/** 4 dp grid from design.md §6. */
object SensaSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val screen = 20.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val minTouchTarget = 48.dp
}

object SensaShapes {
    val xs = RoundedCornerShape(8.dp)
    val sm = RoundedCornerShape(12.dp)
    val md = RoundedCornerShape(16.dp)
    val lg = RoundedCornerShape(24.dp)
    val xl = RoundedCornerShape(28.dp)
    val full = RoundedCornerShape(percent = 50)
    val bottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    val material = Shapes(
        extraSmall = xs,
        small = sm,
        medium = md,
        large = lg,
        extraLarge = xl,
    )
}

/**
 * When true, glass surfaces fall back to a solid [SensaColors.surface] fill (design.md §5.6).
 */
val LocalReduceTransparency = staticCompositionLocalOf { false }

/** Backdrop blur source shared by every glass surface in the tree. */
val LocalSensaHaze = staticCompositionLocalOf<HazeState> {
    error("HazeState not provided. Wrap the content in SensaTheme.")
}

private fun sensaColorsFromScheme(scheme: ColorScheme, dark: Boolean): SensaColors {
    val base = if (dark) DarkSensaColors else LightSensaColors
    return base.copy(
        background = scheme.background,
        surface = scheme.surface,
        surfaceVariant = scheme.surfaceVariant,
        outline = scheme.outline,
        primary = scheme.primary,
        onPrimary = scheme.onPrimary,
        primaryContainer = scheme.primaryContainer,
        onSurface = scheme.onSurface,
        onSurfaceMuted = scheme.onSurfaceVariant,
        error = scheme.error,
        success = scheme.primary,
        accentGlow = if (dark) base.accentGlow else scheme.primary,
    )
}

private fun buildColorScheme(colors: SensaColors, dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        primaryContainer = colors.primaryContainer,
        onPrimaryContainer = colors.onSurface,
        background = colors.background,
        onBackground = colors.onSurface,
        surface = colors.surface,
        onSurface = colors.onSurface,
        surfaceVariant = colors.surfaceVariant,
        onSurfaceVariant = colors.onSurfaceMuted,
        outline = colors.outline,
        outlineVariant = colors.outline,
        error = colors.error,
        onError = if (dark) Color(0xFF2A0A0A) else Color.White,
        inverseSurface = colors.onSurface,
        inverseOnSurface = colors.background,
    )
}

/**
 * Sensa theme. Dark-first with the fixed mint brand palette; Material You dynamic color is
 * opt-in because the glass recipe depends on the brand's ambient glow.
 */
@Composable
fun SensaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    reduceTransparency: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val dynamicScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        null
    }

    val colors = remember(dynamicScheme, darkTheme) {
        dynamicScheme?.let { sensaColorsFromScheme(it, darkTheme) }
            ?: if (darkTheme) DarkSensaColors else LightSensaColors
    }
    val colorScheme = remember(colors, darkTheme) { buildColorScheme(colors, darkTheme) }
    val hazeState = rememberHazeState()

    CompositionLocalProvider(
        LocalSensaColors provides colors,
        LocalSensaAmountStyles provides DefaultSensaAmountStyles,
        LocalReduceTransparency provides reduceTransparency,
        LocalSensaHaze provides hazeState,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SensaTypography,
            shapes = SensaShapes.material,
            content = content,
        )
    }
}
