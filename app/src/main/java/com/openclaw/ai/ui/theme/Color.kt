package com.openclaw.ai.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Primary: purple ───────────────────────────────────────────────────────────
val Purple10 = Color(0xFF1A0033)
val Purple20 = Color(0xFF36006B)
val Purple30 = Color(0xFF5300A4)
val Purple40 = Color(0xFF7C3AED)  // light primary (#7C3AED)
val Purple80 = Color(0xFFA855F7)  // dark primary (#A855F7)
val Purple90 = Color(0xFFEADDFF)

// ── Secondary: sky blue ───────────────────────────────────────────────────────
val SkyBlue40 = Color(0xFF0EA5E9)   // cloud badge / light secondary
val SkyBlue80 = Color(0xFF38BDF8)

// ── Tertiary: green ───────────────────────────────────────────────────────────
val Green40 = Color(0xFF22C55E)     // local badge / light tertiary
val Green80 = Color(0xFF4ADE80)

// ── Neutral / surface ─────────────────────────────────────────────────────────
val Neutral10 = Color(0xFF0F0A1A)   // dark background
val Neutral15 = Color(0xFF1A1425)   // dark surface
val Neutral20 = Color(0xFF241B35)   // dark card / surfaceVariant
val Neutral90 = Color(0xFFE6E1EC)
val Neutral95 = Color(0xFFF3EFF8)
val Neutral99 = Color(0xFFF8F5FF)   // light background (#F8F5FF)

val NeutralVariant30 = Color(0xFF4A4459)
val NeutralVariant50 = Color(0xFF7B7589)
val NeutralVariant60 = Color(0xFF9CA3AF)  // section label color
val NeutralVariant80 = Color(0xFFCAC4D4)
val NeutralVariant90 = Color(0xFFE7E0F0)

// ── Chat bubble surfaces ──────────────────────────────────────────────────────
val UserBubbleLight = Color(0xFF7C3AED)   // solid purple (#7C3AED)
val UserBubbleDark = Color(0xFF7C3AED)    // same purple in dark
val AssistantBubbleLight = Color(0xFFFFFFFF) // white
val AssistantBubbleDark = Color(0xFF241B35)  // dark card (#241B35)

// ── Semantic / badge colors ───────────────────────────────────────────────────
val LocalBadgeGreen = Color(0xFF22C55E)
val CloudBadgeBlue = Color(0xFF0EA5E9)
val ToolResultBorderLight = Color(0xFF7C3AED)
val ToolResultBorderDark = Color(0xFF7C3AED)

// ── Code block ────────────────────────────────────────────────────────────────
val CodeBlockBg = Color(0xFF1E1033)
val CodeBlockText = Color(0xFFA5F3C4)

// ── Chip gradient ─────────────────────────────────────────────────────────────
val ChipGradientStart = Color(0xFF7C3AED)
val ChipGradientEnd = Color(0xFFA855F7)

// ── Section label ─────────────────────────────────────────────────────────────
val SectionLabel = Color(0xFF9CA3AF)

// ── Material3 light color scheme ──────────────────────────────────────────────
val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Purple90,
    onPrimaryContainer = Purple10,

    secondary = SkyBlue40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0C4A6E),

    tertiary = Green40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCFCE7),
    onTertiaryContainer = Color(0xFF14532D),

    background = Neutral99,
    onBackground = Neutral10,

    surface = Color.White,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,

    outline = NeutralVariant50,
    outlineVariant = NeutralVariant80,

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

// ── Material3 dark color scheme ───────────────────────────────────────────────
val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Purple20,
    primaryContainer = Purple30,
    onPrimaryContainer = Purple90,

    secondary = SkyBlue80,
    onSecondary = Color(0xFF0C4A6E),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFE0F2FE),

    tertiary = Green80,
    onTertiary = Color(0xFF14532D),
    tertiaryContainer = Color(0xFF166534),
    onTertiaryContainer = Color(0xFFDCFCE7),

    background = Neutral10,
    onBackground = Neutral90,

    surface = Neutral15,
    onSurface = Neutral90,
    surfaceVariant = Neutral20,
    onSurfaceVariant = NeutralVariant80,

    outline = NeutralVariant60,
    outlineVariant = NeutralVariant30,

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
)
