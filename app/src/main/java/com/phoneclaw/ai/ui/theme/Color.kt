package com.phoneclaw.ai.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ─── Accent palette ───────────────────────────────────────────────────────────
val AccentAmber       = Color(0xFFF59E0B)
val AccentAmberLight  = Color(0xFFFCD34D)
val AccentBlue        = Color(0xFF0EA5E9)
val AccentBlueLight   = Color(0xFF7DD3FC)
val AccentGreen       = Color(0xFF10B981)
val AccentGreenLight  = Color(0xFF6EE7B7)
val AccentPink        = Color(0xFFDB2777)
val AccentPinkLight   = Color(0xFFF472B6)
val AccentViolet      = Color(0xFF7C3AED)
val AccentVioletLight = Color(0xFFA78BFA)
val AccentRed         = Color(0xFFC46B5E)

// ─── Background & Surface tokens ───────────────────────────────────────────────
val CanvasBg         = Color(0xFFF0ECF8)   // slightly richer lavender canvas
val SurfaceCard      = Color(0xFFE8E4F2)   // default card surface (lavender tint)
val SurfaceCardLight = Color(0xFFF0EDF9)   // lighter card for layered content
val SurfacePressed   = Color(0xFFDED9EE)   // pressed/active state
val SurfaceInverse   = Color(0xFF2A2635)   // dark surface

// ─── Tinted card surfaces (for claymorphism variety) ──────────────────────────
val SurfaceVioletCard = Color(0xFFEAE4FF)  // violet-tinted card
val SurfacePinkCard   = Color(0xFFFFE6F3)  // pink-tinted card
val SurfaceBlueCard   = Color(0xFFE2F1FF)  // blue-tinted card
val SurfaceGreenCard  = Color(0xFFE2F7EF)  // green-tinted card
val SurfaceAmberCard  = Color(0xFFFFF5E2)  // amber-tinted card
val SurfaceWhite      = Color(0xFFFFFFFF)  // explicit white — use only when absolutely needed

// ─── Foreground tokens ─────────────────────────────────────────────────────────
val ForegroundPrimary   = Color(0xFF221E2A)  // near-black, max contrast on light
val ForegroundSecondary = Color(0xFF4D495A)  // medium, 6:1+ on SurfaceCard
val ForegroundMuted     = Color(0xFF7A7685)  // muted, 4.5:1 on SurfaceCard
val ForegroundInverse   = Color(0xFFFFFFFF)  // white — for colored backgrounds

// ─── Border tokens ─────────────────────────────────────────────────────────────
val BorderMuted = Color(0x18000000)
val BorderLight = Color(0x40FFFFFF)
val BorderCard  = Color(0x22000000)  // subtle border for card definition

// ─── Shadow tokens (for colored drop shadows) ─────────────────────────────────
val ShadowButtonDrop  = Color(0x4D7C3AED)
val ShadowCardDrop    = Color(0x28A096B4)
val ShadowVioletCard  = Color(0x337C3AED)
val ShadowPinkCard    = Color(0x33DB2777)
val ShadowBlueCard    = Color(0x330EA5E9)
val ShadowGreenCard   = Color(0x3310B981)
val ShadowAmberCard   = Color(0x33F59E0B)

// ─── Chat-specific ─────────────────────────────────────────────────────────────
val UserBubbleLight       = AccentViolet
val UserBubbleDark        = AccentViolet
val AssistantBubbleLight  = SurfaceCard       // was ForegroundInverse (WHITE) — FIXED
val AssistantBubbleDark   = Color(0xFF2A2635)

val LocalBadgeGreen       = AccentGreen
val CloudBadgeBlue        = AccentBlue
val ToolResultBorderLight = AccentViolet
val ToolResultBorderDark  = AccentViolet

val CodeBlockBg   = Color(0xFF1E1033)
val CodeBlockText = Color(0xFFA5F3C4)

val ChipGradientStart = AccentViolet
val ChipGradientEnd   = AccentVioletLight

val SectionLabel = ForegroundMuted

// ─── Material3 light color scheme ──────────────────────────────────────────────
val LightColorScheme = lightColorScheme(
    primary             = AccentViolet,
    onPrimary           = ForegroundInverse,
    primaryContainer    = SurfaceVioletCard,
    onPrimaryContainer  = ForegroundPrimary,

    secondary           = AccentBlue,
    onSecondary         = ForegroundInverse,
    secondaryContainer  = SurfaceBlueCard,
    onSecondaryContainer= ForegroundPrimary,

    tertiary            = AccentGreen,
    onTertiary          = ForegroundInverse,
    tertiaryContainer   = SurfaceGreenCard,
    onTertiaryContainer = ForegroundPrimary,

    background          = CanvasBg,
    onBackground        = ForegroundPrimary,

    surface             = SurfaceCard,        // KEY FIX: was white (ForegroundInverse)
    onSurface           = ForegroundPrimary,
    surfaceVariant      = SurfaceCardLight,
    onSurfaceVariant    = ForegroundSecondary,

    outline             = ForegroundMuted,
    outlineVariant      = BorderCard,

    error               = Color(0xFFC46B5E),
    onError             = ForegroundInverse,
)

// ─── Material3 dark color scheme ───────────────────────────────────────────────
val DarkColorScheme = darkColorScheme(
    primary             = AccentVioletLight,
    onPrimary           = SurfaceInverse,
    primaryContainer    = AccentViolet,
    onPrimaryContainer  = ForegroundInverse,

    secondary           = AccentBlueLight,
    onSecondary         = SurfaceInverse,
    secondaryContainer  = AccentBlue,
    onSecondaryContainer= ForegroundInverse,

    background          = SurfaceInverse,
    onBackground        = ForegroundInverse,

    surface             = Color(0xFF332F3A),
    onSurface           = ForegroundInverse,
    surfaceVariant      = Color(0xFF3F3B47),
    onSurfaceVariant    = Color(0xFFCCC8D5),

    outline             = ForegroundMuted,
    outlineVariant      = BorderLight,

    error               = Color(0xFFC46B5E),
    onError             = ForegroundInverse,
)
