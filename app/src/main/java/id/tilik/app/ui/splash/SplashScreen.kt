package id.tilik.app.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.R
import id.tilik.app.ui.theme.AppBackground
import id.tilik.app.ui.theme.BrandDark
import id.tilik.app.ui.theme.BrandPrimary
import id.tilik.app.ui.theme.BrandSecondary
import id.tilik.app.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit
) {
    // Animasi logo: scale-up halus & fade-in (tanpa lebay)
    val logoScale = remember { Animatable(0.82f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(350)
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = LinearEasing)
            )
        }
        // Total durasi splash ~2.2 detik lalu transisi ke dashboard
        delay(2200)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Gambar sesuai screenshot (logo.png)
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Tilik Logo",
                modifier = Modifier
                    .size(130.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Tulisan "tilik" dengan animasi bouncing balls yang smooth
            BouncingTilikText(
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}

/**
 * Karakter "t", "i", "l", "i", "k" yang masing-masing melompat secara berurutan (wave bounce),
 * dengan titik pada huruf 'i' sebagai bouncing ball yang ceria dan smooth.
 */
@Composable
private fun BouncingTilikText(modifier: Modifier = Modifier) {
    val word = listOf("t", "i", "l", "i", "k")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        word.forEachIndexed { index, letter ->
            val bounceAnim = remember { Animatable(0f) }

            LaunchedEffect(index) {
                // Staggered delay agar gelombang membal berjalan dari kiri ke kanan (bouncing wave)
                delay((index * 110L))
                bounceAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 1100
                            0.0f at 0 using FastOutSlowInEasing
                            -10.0f at 220 using FastOutSlowInEasing // Titik puncak pantulan
                            0.0f at 480 using FastOutSlowInEasing  // Kembali mendarat
                            -2.5f at 600 using FastOutSlowInEasing  // Micro bounce kedua
                            0.0f at 700 using FastOutSlowInEasing  // Mendarat stabil
                            0.0f at 1100                           // Rest sejenak sebelum pantulan berikutnya
                        },
                        repeatMode = RepeatMode.Restart
                    )
                )
            }

            Box(
                modifier = Modifier.offset(y = bounceAnim.value.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    color = TextPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp
                )
            }
        }
    }
}
