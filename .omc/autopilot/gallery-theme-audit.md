# Gallery Theme System Audit
**Source**: google-ai-edge/gallery (Android/src/)
**Audited**: 2026-04-06
**Auditor**: theme-styling-analyst

---

## 1. Theme Architecture

### Approach
- **Pure Material3** — no custom design system or token layer beyond M3 ColorScheme
- **Two color schemes**: `lightScheme` and `darkScheme` (standard M3 `lightColorScheme`/`darkColorScheme`)
- **Extended custom colors**: `CustomColors` data class injected via `CompositionLocalProvider` → `LocalCustomColors`
- **Theme entry point**: `GalleryTheme {}` composable — no dynamic color / Material You
- **Theme switching**: `ThemeSettings.themeOverride` (mutableStateOf `Theme.THEME_AUTO | THEME_LIGHT | THEME_DARK`) — proto-backed enum, user-settable at runtime
- **Font**: Nunito (custom — bundled in `res/font/`): Regular, ExtraLight, Light, Medium, SemiBold, Bold, ExtraBold, Black
- **Shapes**: Not defined in Gallery (uses M3 defaults); bubble corner radius is 24dp via `dimens.xml`
- **Status bar**: `WindowCompat.getInsetsController` — light icons on dark theme, dark icons on light theme; navigation bar contrast enforcement disabled on API Q+

---

## 2. Complete Color Palette

### 2a. Light Theme — Material3 ColorScheme

| Token | Hex | RGB |
|---|---|---|
| primary | `#0B57D0` | rgb(11, 87, 208) |
| onPrimary | `#FFFFFF` | rgb(255, 255, 255) |
| primaryContainer | `#D3E3FD` | rgb(211, 227, 253) |
| onPrimaryContainer | `#0842A0` | rgb(8, 66, 160) |
| secondary | `#00639B` | rgb(0, 99, 155) |
| onSecondary | `#FFFFFF` | rgb(255, 255, 255) |
| secondaryContainer | `#C2E7FF` | rgb(194, 231, 255) |
| onSecondaryContainer | `#004A77` | rgb(0, 74, 119) |
| tertiary | `#146C2E` | rgb(20, 108, 46) |
| onTertiary | `#FFFFFF` | rgb(255, 255, 255) |
| tertiaryContainer | `#C4EED0` | rgb(196, 238, 208) |
| onTertiaryContainer | `#0F5223` | rgb(15, 82, 35) |
| error | `#B3261E` | rgb(179, 38, 30) |
| onError | `#FFFFFF` | rgb(255, 255, 255) |
| errorContainer | `#F9DEDC` | rgb(249, 222, 220) |
| onErrorContainer | `#8C1D18` | rgb(140, 29, 24) |
| background | `#FFFFFF` | rgb(255, 255, 255) |
| onBackground | `#1F1F1F` | rgb(31, 31, 31) |
| surface | `#FFFFFF` | rgb(255, 255, 255) |
| onSurface | `#1F1F1F` | rgb(31, 31, 31) |
| surfaceVariant | `#E1E3E1` | rgb(225, 227, 225) |
| onSurfaceVariant | `#444746` | rgb(68, 71, 70) |
| surfaceContainerLowest | `#FFFFFF` | rgb(255, 255, 255) |
| surfaceContainerLow | `#F8FAFD` | rgb(248, 250, 253) |
| surfaceContainer | `#F0F4F9` | rgb(240, 244, 249) |
| surfaceContainerHigh | `#E9EEF6` | rgb(233, 238, 246) |
| surfaceContainerHighest | `#DDE3EA` | rgb(221, 227, 234) |
| inverseSurface | `#303030` | rgb(48, 48, 48) |
| inverseOnSurface | `#F2F2F2` | rgb(242, 242, 242) |
| outline | `#747775` | rgb(116, 119, 117) |
| outlineVariant | `#C4C7C5` | rgb(196, 199, 197) |
| inversePrimary | `#A8C7FA` | rgb(168, 199, 250) |
| surfaceDim | `#D3DBE5` | rgb(211, 219, 229) |
| surfaceBright | `#FFFFFF` | rgb(255, 255, 255) |
| scrim | `#000000` | rgb(0, 0, 0) |

### 2b. Dark Theme — Material3 ColorScheme

| Token | Hex | RGB |
|---|---|---|
| primary | `#A8C7FA` | rgb(168, 199, 250) |
| onPrimary | `#062E6F` | rgb(6, 46, 111) |
| primaryContainer | `#0842A0` | rgb(8, 66, 160) |
| onPrimaryContainer | `#D3E3FD` | rgb(211, 227, 253) |
| secondary | `#7FCFFF` | rgb(127, 207, 255) |
| onSecondary | `#003355` | rgb(0, 51, 85) |
| secondaryContainer | `#004A77` | rgb(0, 74, 119) |
| onSecondaryContainer | `#C2E7FF` | rgb(194, 231, 255) |
| tertiary | `#6DD58C` | rgb(109, 213, 140) |
| onTertiary | `#0A3818` | rgb(10, 56, 24) |
| tertiaryContainer | `#0F5223` | rgb(15, 82, 35) |
| onTertiaryContainer | `#C4EED0` | rgb(196, 238, 208) |
| error | `#F2B8B5` | rgb(242, 184, 181) |
| onError | `#601410` | rgb(96, 20, 16) |
| errorContainer | `#8C1D18` | rgb(140, 29, 24) |
| onErrorContainer | `#F9DEDC` | rgb(249, 222, 220) |
| background | `#131314` | rgb(19, 19, 20) |
| onBackground | `#E3E3E3` | rgb(227, 227, 227) |
| surface | `#131314` | rgb(19, 19, 20) |
| onSurface | `#E3E3E3` | rgb(227, 227, 227) |
| surfaceVariant | `#444746` | rgb(68, 71, 70) |
| onSurfaceVariant | `#C4C7C5` | rgb(196, 199, 197) |
| surfaceContainerLowest | `#0E0E0E` | rgb(14, 14, 14) |
| surfaceContainerLow | `#1B1B1B` | rgb(27, 27, 27) |
| surfaceContainer | `#1E1F20` | rgb(30, 31, 32) |
| surfaceContainerHigh | `#282A2C` | rgb(40, 42, 44) |
| surfaceContainerHighest | `#333537` | rgb(51, 53, 55) |
| inverseSurface | `#E3E3E3` | rgb(227, 227, 227) |
| inverseOnSurface | `#303030` | rgb(48, 48, 48) |
| outline | `#8E918F` | rgb(142, 145, 143) |
| outlineVariant | `#444746` | rgb(68, 71, 70) |
| inversePrimary | `#0B57D0` | rgb(11, 87, 208) |
| surfaceDim | `#131314` | rgb(19, 19, 20) |
| surfaceBright | `#37393B` | rgb(55, 57, 59) |
| scrim | `#000000` | rgb(0, 0, 0) |

### 2c. Custom Colors (Light)

| Token | Hex | RGB | Usage |
|---|---|---|---|
| appTitleGradientColors[0] | `#85B1F8` | rgb(133, 177, 248) | App title gradient start |
| appTitleGradientColors[1] | `#3174F1` | rgb(49, 116, 241) | App title gradient end |
| tabHeaderBgColor | `#3174F1` | rgb(49, 116, 241) | Tab header background |
| taskCardBgColor | surfaceContainerLowestLight = `#FFFFFF` | rgb(255, 255, 255) | Task card background |
| taskBgColors[0] (red) | `#FFF5F5` | rgb(255, 245, 245) | Task bg — red category |
| taskBgColors[1] (green) | `#F4FBF6` | rgb(244, 251, 246) | Task bg — green category |
| taskBgColors[2] (blue) | `#F1F6FE` | rgb(241, 246, 254) | Task bg — blue category |
| taskBgColors[3] (yellow) | `#FFFBF0` | rgb(255, 251, 240) | Task bg — yellow category |
| taskIconColors[0] (red) | `#DB372D` | rgb(219, 55, 45) | Task icon — red |
| taskIconColors[1] (green) | `#128937` | rgb(18, 137, 55) | Task icon — green |
| taskIconColors[2] (blue) | `#3174F1` | rgb(49, 116, 241) | Task icon — blue |
| taskIconColors[3] (yellow) | `#CAA12A` | rgb(202, 161, 42) | Task icon — yellow |
| homeBottomGradient[0] | `#00F8F9FF` (transparent) | rgba(248, 249, 255, 0) | Home bottom fade |
| homeBottomGradient[1] | `#FFEFC9` | rgb(255, 239, 201) | Home bottom warm tint |
| agentBubbleBgColor | `#E9EEF6` | rgb(233, 238, 246) | AI message bubble bg |
| userBubbleBgColor | `#32628D` | rgb(50, 98, 141) | User message bubble bg |
| linkColor | `#32628D` | rgb(50, 98, 141) | Hyperlinks |
| successColor | `#3D860B` | rgb(61, 134, 11) | Success state |
| recordButtonBgColor | `#EE675C` | rgb(238, 103, 92) | Record button |
| waveFormBgColor | `#AAAAAA` | rgb(170, 170, 170) | Audio waveform |
| warningContainerColor | `#FEF7E0` | rgb(254, 247, 224) | Warning container |
| warningTextColor | `#E37400` | rgb(227, 116, 0) | Warning text |
| errorContainerColor | `#FCE8E6` | rgb(252, 232, 230) | Error container |
| errorTextColor | `#D93025` | rgb(217, 48, 37) | Error text |
| newFeatureContainerColor | `#EEDCFE` | rgb(238, 220, 254) | New feature badge bg |
| newFeatureTextColor | `#400B84` | rgb(64, 11, 132) | New feature badge text |
| bgStarColor | `#3A669AF5` (24% opacity) | rgba(102, 154, 245, 0.23) | Background star decoration |

### 2d. Custom Colors (Dark) — Key Differences

| Token | Hex | RGB |
|---|---|---|
| taskCardBgColor | surfaceContainerHighDark = `#282A2C` | rgb(40, 42, 44) |
| taskBgColors[0] (red) | `#181210` | rgb(24, 18, 16) |
| taskBgColors[1] (green) | `#131711` | rgb(19, 23, 17) |
| taskBgColors[2] (blue) | `#191924` | rgb(25, 25, 36) |
| taskBgColors[3] (yellow) | `#1A1813` | rgb(26, 24, 19) |
| agentBubbleBgColor | `#1B1C1D` | rgb(27, 28, 29) |
| userBubbleBgColor | `#1F3760` | rgb(31, 55, 96) |
| linkColor | `#9DCAFC` | rgb(157, 202, 252) |
| successColor | `#A1CE83` | rgb(161, 206, 131) |
| warningContainerColor | `#554C33` | rgb(85, 76, 51) |
| warningTextColor | `#FCC934` | rgb(252, 201, 52) |
| errorContainerColor | `#523A3B` | rgb(82, 58, 59) |
| errorTextColor | `#EE675C` | rgb(238, 103, 92) |
| taskIconShapeBgColor | `#202124` | rgb(32, 33, 36) |

---

## 3. Typography System

### Font Family
- **Gallery**: **Nunito** (custom, bundled) — 8 weights: ExtraLight, Light, Normal, Medium, SemiBold, Bold, ExtraBold, Black
- **OpenClaw**: `FontFamily.Default` (Roboto on Android)

### Gallery Typography (M3 Baseline Sizes with Nunito substituted)

Gallery uses M3 baseline `Typography()` sizes with `fontFamily = appFontFamily` applied. M3 baseline sizes are:

| Style | Size (sp) | Line Height (sp) | Weight (M3 default) |
|---|---|---|---|
| displayLarge | 57 | 64 | W400 |
| displayMedium | 45 | 52 | W400 |
| displaySmall | 36 | 44 | W400 |
| headlineLarge | 32 | 40 | W400 |
| headlineMedium | 28 | 36 | W400 |
| headlineSmall | 24 | 32 | W400 |
| titleLarge | 22 | 28 | W400 |
| titleMedium | 16 | 24 | W500 |
| titleSmall | 14 | 20 | W500 |
| bodyLarge | 16 | 24 | W400 |
| bodyMedium | 14 | 20 | W400 |
| bodySmall | 12 | 16 | W400 |
| labelLarge | 14 | 20 | W500 |
| labelMedium | 12 | 16 | W500 |
| labelSmall | 11 | 16 | W500 |

### Gallery Custom Typography Variants

| Name | Size (sp) | Line Height | Weight | Notes |
|---|---|---|---|---|
| titleMediumNarrow | 16 | 24 | W500 | letterSpacing 0.0 |
| titleSmaller | 12 | 20 | Bold | Custom small title |
| labelSmallNarrow | 11 | 16 | W500 | letterSpacing 0.0 |
| labelSmallNarrowMedium | 11 | 16 | Medium | letterSpacing 0.0 |
| bodySmallNarrow | 12 | 16 | W400 | letterSpacing 0.0 |
| bodySmallMediumNarrow | 14 | 16 | W400 | letterSpacing 0.0 |
| bodySmallMediumNarrowBold | 14 | 16 | Bold | letterSpacing 0.0 |
| homePageTitleStyle | 48 | 48 | Medium | letterSpacing -1.0, hero title |
| bodyLargeNarrow | 16 | 24 | W400 | letterSpacing 0.2 |
| headlineLargeMedium | 32 | 40 | Medium | |
| emptyStateTitle | 37 | 50 | W400 | Empty screen heading |
| emptyStateContent | 16 | 22 | W400 | Empty screen body |

### OpenClaw Typography (Current)

| Style | Size (sp) | Line Height (sp) | Weight |
|---|---|---|---|
| displayLarge | 34 | 41 | Bold |
| displayMedium | 28 | 34 | Bold |
| displaySmall | 24 | 30 | SemiBold |
| headlineLarge | 22 | 28 | Bold |
| headlineMedium | 20 | 26 | SemiBold |
| headlineSmall | 18 | 24 | SemiBold |
| titleLarge | 17 | 22 | SemiBold |
| titleMedium | 16 | 22 | Medium |
| titleSmall | 14 | 20 | Medium |
| bodyLarge | 17 | 24 | Normal |
| bodyMedium | 15 | 22 | Normal |
| bodySmall | 13 | 18 | Normal |
| labelLarge | 15 | 20 | Medium |
| labelMedium | 12 | 16 | Medium |
| labelSmall | 11 | 14 | Medium |

---

## 4. Spacing & Shape System

### Gallery Dimensions (dimens.xml)
| Name | Value |
|---|---|
| model_selector_height | 54dp |
| chat_bubble_corner_radius | 24dp |

### Gallery MessageBubbleShape
- Custom `Shape` implementation with configurable radius
- Hard corner (0dp) on one side (top-left for user, top-right for agent)
- All other corners: `chat_bubble_corner_radius` = 24dp
- User bubble: hard top-right corner → speech bubble pointing right
- Agent bubble: hard top-left corner → speech bubble pointing left

### OpenClaw AppShapes (M3 Shapes)
| Name | Value |
|---|---|
| extraSmall | 6dp |
| small | 10dp |
| medium | 16dp |
| large | 22dp |
| extraLarge | 28dp |

---

## 5. Dark/Light Mode Implementation

### Gallery
- `ThemeSettings.themeOverride`: `mutableStateOf<Theme>` (proto enum: THEME_AUTO, THEME_LIGHT, THEME_DARK)
- Logic: `darkTheme = (isSystemInDarkTheme() || override == THEME_DARK) && override != THEME_LIGHT`
- Runtime switchable — stored in proto (persistent across sessions)
- Navigation bar contrast enforcement disabled on Q+ for transparency
- Status bar icons: light on dark theme, dark on light theme via `WindowCompat`

### OpenClaw
- `isSystemInDarkTheme()` only — no manual override
- Status bar: always transparent; navigation bar color = `colorScheme.background`
- `WindowCompat.setDecorFitsSystemWindows(window, false)` for edge-to-edge

---

## 6. Styling Approach Comparison

| Aspect | Gallery | OpenClaw |
|---|---|---|
| Design system | Material3 (pure) | Material3 (pure) |
| Custom colors | `CustomColors` via CompositionLocal | None — all inline or in Color.kt |
| Font | Nunito (custom, bundled) | FontFamily.Default (Roboto) |
| Theme override | Proto-backed runtime enum (3 modes) | System-only |
| Chat bubble shape | Custom `Shape` class, 24dp corner, hard corner on one side | Standard `RoundedCornerShape` |
| Shapes | M3 defaults | Custom AppShapes (6/10/16/22/28dp) |
| Color token count | ~35 M3 + ~25 custom = ~60 total | ~40 semantic colors |
| Gradient presets | Yes (promo banner brush, task gradients, home fade) | Yes (brand gradient, glass, success, welcome) |

---

## 7. Accessibility Considerations

### Contrast Ratios (approximate, calculated from RGB values)

**Light Theme — Critical Pairs**
| Foreground | Background | Contrast | WCAG |
|---|---|---|---|
| onSurface `#1F1F1F` on surface `#FFFFFF` | 18.1:1 | AAA |
| primary `#0B57D0` on background `#FFFFFF` | 5.9:1 | AA |
| userBubbleBgColor text `#FFFFFF` on `#32628D` | ~4.6:1 | AA |
| agentBubble text `#1F1F1F` on `#E9EEF6` | ~14.2:1 | AAA |
| warningTextColor `#E37400` on `#FEF7E0` | ~3.1:1 | AA Large only |
| errorTextColor `#D93025` on `#FCE8E6` | ~4.5:1 | AA |

**Dark Theme — Critical Pairs**
| Foreground | Background | Contrast | WCAG |
|---|---|---|---|
| onSurface `#E3E3E3` on surface `#131314` | ~14.4:1 | AAA |
| primary `#A8C7FA` on background `#131314` | ~9.4:1 | AAA |
| userBubble text `#E3E3E3` on `#1F3760` | ~5.2:1 | AA |
| agentBubble text `#E3E3E3` on `#1B1C1D` | ~14.3:1 | AAA |
| warningTextColor `#FCC934` on `#554C33` | ~5.6:1 | AA |
| errorTextColor `#EE675C` on `#523A3B` | ~3.9:1 | AA |

**Accessibility Features Observed**
- `semantics { contentDescription = ... }` on message bubbles
- `liveRegion = LiveRegionMode.Polite` on agent responses (announced when complete)
- `SelectionContainer` wrapping text (copy-able)
- `contentDescription` on interactive elements throughout

---

## 8. Compatibility with OpenClaw's Existing Theme

### Structural Compatibility
Both apps use:
- Material3 `lightColorScheme` / `darkColorScheme`
- Same token names (primary, surface, onSurface, etc.)
- `MaterialTheme` composable entry point
- Compose UI with no XML theming

### Key Divergences

| Dimension | Gallery | OpenClaw | Migration Effort |
|---|---|---|---|
| Primary color | Blue `#0B57D0` | Near-black `#1C1C1E` | High — brand identity change |
| Brand accent | Google Blue family | Teal `#2AC4A0` | Medium — add tertiary mapping |
| Background dark | Very dark gray `#131314` | True black `#000000` | Low — tweak one token |
| Surface dark | `#131314` | `#1C1C1E` | Low — single token |
| Font | Nunito (rounded, friendly) | Roboto (default) | Medium — add font assets |
| Theme override | Proto-backed 3-mode | System-only | Medium — add ThemeSettings object |
| Custom colors | Rich `CustomColors` system | None | Medium — add CompositionLocal |
| Bubble shape | Hard corner one side | Not defined | Low — port `MessageBubbleShape` |
| Typography sizes | M3 baseline (larger) | iOS-inspired (smaller) | Low — cosmetic only |

---

## 9. Recommended Approach for Phase 1

### Strategy: Additive — Extend OpenClaw, Do Not Replace

**Step 1 — Add `CustomColors` system (1 day)**
- Port Gallery's `CustomColors` data class to OpenClaw
- Create `lightCustomColors` and `darkCustomColors` instances
- Inject via `CompositionLocalProvider` in `OpenClawTheme`
- Map Gallery's chat bubble colors to OpenClaw brand palette:
  - `userBubbleBgColor` → keep current `UserBubbleGradientStart` (`#E5E5EA`)
  - `agentBubbleBgColor` → keep current `AssistantBubbleDark`/`Light`

**Step 2 — Port `MessageBubbleShape` (0.5 day)**
- Copy Gallery's `MessageBubbleShape.kt` verbatim — self-contained, no dependencies
- Use 24dp radius (matching Gallery) or OpenClaw's 16dp medium shape — design decision

**Step 3 — Add theme override (0.5 day)**
- Port `ThemeSettings` singleton (can use `DataStore` instead of proto)
- Add to `OpenClawTheme` alongside existing `isSystemInDarkTheme()` logic

**Step 4 — Optional: Add Nunito font (1 day)**
- Download Nunito from Google Fonts (OFL license)
- Bundle in `res/font/`
- Replace `FontFamily.Default` in `AppTypography`
- Note: Roboto is adequate for Phase 1; Nunito is a differentiator for polish

**What NOT to change in Phase 1**
- Primary color identity (teal brand vs Google blue — keep OpenClaw's teal)
- Typography scale sizes (OpenClaw's iOS-inspired sizes are smaller/tighter — appropriate for chat density)
- Neutral palette (OpenClaw's iOS gray scale is already well-designed)

### Effort Estimate
| Work | Effort |
|---|---|
| CustomColors system | 1 day |
| MessageBubbleShape port | 0.5 day |
| ThemeSettings override | 0.5 day |
| Nunito font (optional) | 1 day |
| **Total (without font)** | **2 days** |
| **Total (with font)** | **3 days** |

---

## 10. Files Audited

**Gallery (google-ai-edge/gallery)**
- `Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/theme/Color.kt`
- `Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/theme/Theme.kt`
- `Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/theme/Type.kt`
- `Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/theme/ThemeSettings.kt`
- `Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/common/ColorUtils.kt`
- `Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/common/chat/MessageBubbleShape.kt`
- `Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/common/chat/MessageBodyText.kt`
- `Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/common/chat/ChatPanel.kt` (partial)
- `Android/src/app/src/main/res/values/dimens.xml`

**OpenClaw (openClaw-android)**
- `app/src/main/java/com/openclaw/android/ui/theme/Color.kt`
- `app/src/main/java/com/openclaw/android/ui/theme/Theme.kt`
- `app/src/main/java/com/openclaw/android/ui/theme/Type.kt`
