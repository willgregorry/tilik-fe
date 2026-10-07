package id.tilik.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Reference Design Tokens from investment_app_ui_clone.html
val AppBg = Color(0xFF0A0A0A)              // Deep rich dark background
val AppCard = Color(0xFF141414)            // Elevated dark card surface
val AppCardSubtle = Color(0xFF1A1A1A)      // Secondary subtle container
val AppAccent = Color(0xFFFF5C35)          // Fire coral / modern investment accent
val AppGreen = Color(0xFF22C55E)           // Vibrant bull green
val AppGreenBg = Color(0xFF0F2115)         // Bullish pill container
val AppGreenBorder = Color(0x3322C55E)     // Bullish hairline border
val AppRed = Color(0xFFEF4444)             // Vibrant bear red
val AppRedBg = Color(0xFF241113)           // Bearish pill container
val AppRedBorder = Color(0x33EF4444)       // Bearish hairline border
val AppGray = Color(0xFF888888)            // Neutral muted text
val AppGrayDark = Color(0xFF737373)        // Section header subtitle

// Compatibility Tokens
val BrandPrimary = AppAccent
val BrandSecondary = Color(0xFFFF7E5F)
val BrandDark = AppBg

val AppBackground = AppBg
val AppSurface = AppCard
val AppSurfaceSubtle = AppCardSubtle
val AppBorder = Color(0x14FFFFFF)          // 8% white hairline border (border-white/5)
val AppBorderLight = Color(0x0FFFFFFF)     // 6% white hairline divider

// Status State Colors
val StatusSuccess = AppGreen
val StatusSuccessBg = AppGreenBg
val StatusDanger = AppRed
val StatusDangerBg = AppRedBg
val StatusWarning = Color(0xFFF59E0B)
val StatusWarningBg = Color(0xFF221A0F)
val StatusNeutral = AppGray
val StatusNeutralBg = AppCardSubtle

// Clean Modern Typography Colors
val TextPrimary = Color(0xFFFFFFFF)        // Crisp white
val TextSecondary = AppGray                // Modern gray
val TextMuted = AppGrayDark                // Section header

// Overlay Compatibility mappings
val DarkSlateBackground = AppBackground
val DarkSlateSurface = AppSurface
val DarkSlateBorder = AppBorder
val AccentBlue = AppAccent
val VerdictValid = StatusSuccess
val VerdictMisleading = StatusWarning
val VerdictInvalid = StatusDanger

private val InvestmentDarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = AppAccent,
    secondary = BrandSecondary,
    background = AppBackground,
    surface = AppSurface,
    surfaceVariant = AppSurfaceSubtle,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun TilikTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = InvestmentDarkColorScheme,
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

