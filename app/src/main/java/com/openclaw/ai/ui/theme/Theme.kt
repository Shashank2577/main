package com.openclaw.ai.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.provides
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Holder for custom colors that are not part of the Material3 color scheme but
 * are needed throughout the app (chat bubbles, badges, tool result borders,
 * code blocks, gradient chips, section labels).
 */
@Immutable
data class OpenClawColors(
    val userBubble: Color,
    val assistantBubble: Color,
    val toolResultBorder: Color,
    val localBadgeGreen: Color,
    val cloudBadgeBlue: Color,
    val linkColor: Color,
    val codeBlockBg: Color,
    val codeBlockText: Color,
    val chipGradientStart: Color,
    val chipGradientEnd: Color,
    val sectionLabel: Color,
)

val LightOpenClawColors = OpenClawColors(
    userBubble = UserBubbleLight,
    assistantBubble = AssistantBubbleLight,
    toolResultBorder = ToolResultBorderLight,
    localBadgeGreen = LocalBadgeGreen,
    cloudBadgeBlue = CloudBadgeBlue,
    linkColor = Purple40,
    codeBlockBg = CodeBlockBg,
    codeBlockText = CodeBlockText,
    chipGradientStart = ChipGradientStart,
    chipGradientEnd = ChipGradientEnd,
    sectionLabel = SectionLabel,
)

val DarkOpenClawColors = OpenClawColors(
    userBubble = UserBubbleDark,
    assistantBubble = AssistantBubbleDark,
    toolResultBorder = ToolResultBorderDark,
    localBadgeGreen = LocalBadgeGreen,
    cloudBadgeBlue = CloudBadgeBlue,
    linkColor = Purple80,
    codeBlockBg = CodeBlockBg,
    codeBlockText = CodeBlockText,
    chipGradientStart = ChipGradientStart,
    chipGradientEnd = ChipGradientEnd,
    sectionLabel = SectionLabel,
)

val LocalOpenClawColors: CompositionLocal<OpenClawColors> = staticCompositionLocalOf {
    LightOpenClawColors
}

/** Convenience accessor — call from any composable inside [OpenClawAITheme]. */
val MaterialTheme.customColors: OpenClawColors
    @Composable
    @ReadOnlyComposable
    get() = LocalOpenClawColors.current

/**
 * App-level theme composable.
 *
 * @param darkTheme      Force dark/light mode. Defaults to system setting.
 * @param dynamicColor   Use Material You dynamic colors on Android 12+.
 * @param content        Composable content.
 */
@Composable
fun OpenClawAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val customColors = if (darkTheme) DarkOpenClawColors else LightOpenClawColors

    CompositionLocalProvider(
        LocalOpenClawColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
