package id.tilik.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand Colors matching Tilik Logo (Indigo / Royal Blue & Violet)
val BrandPrimary = Color(0xFF4F46E5)       // Deep Indigo from Logo
val BrandSecondary = Color(0xFF7C5AF6)     // Violet Accent from Logo
val BrandDark = Color(0xFF0F172A)

// Neutral 21st-Century Clean Light Architecture
val AppBackground = Color(0xFFF8FAFC)      // Clean neutral off-white
val AppSurface = Color(0xFFFFFFFF)         // Crisp white card
val AppSurfaceSubtle = Color(0xFFF1F5F9)   // Subtle gray container
val AppBorder = Color(0xFFE2E8F0)          // Crisp hairline border
val AppBorderLight = Color(0xFFF1F5F9)

// Neutral State Colors (No flashy low-opacity borders)
val StatusSuccess = Color(0xFF16A34A)      // Neutral dark green
val StatusSuccessBg = Color(0xFFF0FDF4)
val StatusDanger = Color(0xFFDC2626)       // Neutral clean red
val StatusDangerBg = Color(0xFFFEF2F2)
val StatusWarning = Color(0xFFD97706)      // Neutral warm amber
val StatusWarningBg = Color(0xFFFFFBEB)
val StatusNeutral = Color(0xFF64748B)
val StatusNeutralBg = Color(0xFFF1F5F9)

// High-contrast clean typography
val TextPrimary = Color(0xFF0F172A)        // Deep slate black
val TextSecondary = Color(0xFF475569)      // Slate body
val TextMuted = Color(0xFF94A3B8)          // Subtle caption

// Compatibility mappings
val DarkSlateBackground = AppBackground
val DarkSlateSurface = AppSurface
val DarkSlateBorder = AppBorder
val AccentBlue = BrandPrimary
val VerdictValid = StatusSuccess
val VerdictMisleading = StatusWarning
val VerdictInvalid = StatusDanger

private val CleanLightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    background = AppBackground,
    surface = AppSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun TilikTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CleanLightColorScheme,
        typography = AppTypography
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalTextStyle provides androidx.compose.ui.text.TextStyle(
                fontFamily = DMSansFontFamily
            ),
            content = content
        )
    }
}

