package com.unknownrex.sensa.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Brand palette from design.md §3. Dark is the primary target; light must stay fully supported.
 */
internal object Palette {
    val Mint = Color(0xFF3DDC97)
    val MintDeep = Color(0xFF0B7A47)
    val MintGlow = Color(0xFF12B76A)
    val Ink = Color(0xFF071513)
    val InkSurface = Color(0xFF0F2724)
    val InkSurfaceVariant = Color(0xFF163330)
    val InkOutline = Color(0xFF2A4A45)
    val Mist = Color(0xFFEEF7F4)
    val MistMuted = Color(0xFF9DB5AE)
    val Paper = Color(0xFFF2F5F4)
    val PaperVariant = Color(0xFFE6EEEC)
    val PaperOutline = Color(0xFFD5DEDB)
    val PaperInk = Color(0xFF0B1F1E)
    val PaperMuted = Color(0xFF4A5A56)
    val OnMint = Color(0xFF04281A)
    val MintContainer = Color(0xFF12432F)
    val MintContainerLight = Color(0xFFD7F5E6)
    val WarningDark = Color(0xFFFFB454)
    val WarningLight = Color(0xFFB26A00)
    val ErrorDark = Color(0xFFFF6B6B)
    val ErrorLight = Color(0xFFC62828)
}

/**
 * Extra tokens that Material 3 does not model. Exposed through [LocalSensaColors].
 */
data class SensaColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val outline: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val accentGlow: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
    val warning: Color,
    val error: Color,
    val success: Color,
    val glassFill: Color,
    val glassBorderStart: Color,
    val glassBorderEnd: Color,
)

internal val DarkSensaColors = SensaColors(
    background = Palette.Ink,
    surface = Palette.InkSurface,
    surfaceVariant = Palette.InkSurfaceVariant,
    outline = Palette.InkOutline,
    primary = Palette.Mint,
    onPrimary = Palette.OnMint,
    primaryContainer = Palette.MintContainer,
    accentGlow = Palette.Mint,
    onSurface = Palette.Mist,
    onSurfaceMuted = Palette.MistMuted,
    warning = Palette.WarningDark,
    error = Palette.ErrorDark,
    success = Palette.Mint,
    glassFill = Color.White.copy(alpha = 0.09f),
    glassBorderStart = Color.White.copy(alpha = 0.24f),
    glassBorderEnd = Color.White.copy(alpha = 0.04f),
)

internal val LightSensaColors = SensaColors(
    background = Palette.Paper,
    surface = Color.White,
    surfaceVariant = Palette.PaperVariant,
    outline = Palette.PaperOutline,
    primary = Palette.MintDeep,
    onPrimary = Color.White,
    primaryContainer = Palette.MintContainerLight,
    accentGlow = Palette.MintGlow,
    onSurface = Palette.PaperInk,
    onSurfaceMuted = Palette.PaperMuted,
    warning = Palette.WarningLight,
    error = Palette.ErrorLight,
    success = Palette.MintDeep,
    glassFill = Color.White.copy(alpha = 0.60f),
    glassBorderStart = Color.White.copy(alpha = 0.80f),
    glassBorderEnd = Color.White.copy(alpha = 0.20f),
)

val LocalSensaColors = staticCompositionLocalOf<SensaColors> {
    error("SensaColors not provided. Wrap the content in SensaTheme.")
}

/**
 * Category colors from design.md §3.2. Every category is also paired with an icon and label so
 * color is never the only signal.
 */
object CategoryPalette {
    val Food = Color(0xFFFFB454)
    val Groceries = Color(0xFF8BE28B)
    val Transport = Color(0xFF6AB7FF)
    val Shopping = Color(0xFFFF8FB1)
    val Subscriptions = Color(0xFFB79CFF)
    val Bills = Color(0xFFF5E27A)
    val Health = Color(0xFFFF7A7A)
    val Education = Color(0xFF5EEAD4)
    val Entertainment = Color(0xFFFF9F6B)
    val Uncategorized = Color(0xFF9DB5AE)

    fun colorFor(categoryKey: String): Color = when (categoryKey) {
        "food" -> Food
        "groceries" -> Groceries
        "transport" -> Transport
        "shopping" -> Shopping
        "subscriptions" -> Subscriptions
        "bills" -> Bills
        "health" -> Health
        "education" -> Education
        "entertainment" -> Entertainment
        else -> Uncategorized
    }
}
