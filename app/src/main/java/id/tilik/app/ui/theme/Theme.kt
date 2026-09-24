package id.tilik.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkSlateBackground = Color(0xFF090D16)
val DarkSlateSurface = Color(0xFF18181B)
val DarkSlateBorder = Color(0xFF27272A)
val AccentBlue = Color(0xFF3B82F6)
val VerdictValid = Color(0xFF10B981)
val VerdictMisleading = Color(0xFFF59E0B)
val VerdictInvalid = Color(0xFFEF4444)
val TextPrimary = Color(0xFFF4F4F5)
val TextSecondary = Color(0xFFA1A1AA)

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    background = DarkSlateBackground,
    surface = DarkSlateSurface,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun TilikTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
