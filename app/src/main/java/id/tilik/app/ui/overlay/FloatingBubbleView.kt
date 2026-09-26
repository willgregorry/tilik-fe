package id.tilik.app.ui.overlay

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.model.OverlayState
import id.tilik.app.ui.theme.AccentBlue
import id.tilik.app.ui.theme.DarkSlateBorder
import id.tilik.app.ui.theme.DarkSlateSurface
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import id.tilik.app.ui.theme.VerdictInvalid

@Composable
fun FloatingBubbleView(
    state: OverlayState,
    detectedTicker: String? = null,
    errorMessage: String? = null,
    isTucked: Boolean = false,
    isDockedOnLeft: Boolean = false,
    onBubbleClick: () -> Unit,
    onRetryClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val alphaAnim by animateFloatAsState(
        targetValue = if (isTucked) 0.42f else 1.0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "bubbleAlpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (isTucked) 0.86f else 1.0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "bubbleScale"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            alpha = alphaAnim
            scaleX = scaleAnim
            scaleY = scaleAnim
            transformOrigin = if (isDockedOnLeft) {
                TransformOrigin(0f, 0.5f)
            } else {
                TransformOrigin(1f, 0.5f)
            }
        }
    ) {
        when (state) {
            OverlayState.IDLE -> {
                IdleBubble(
                    detectedTicker = detectedTicker,
                    isTucked = isTucked,
                    isDockedOnLeft = isDockedOnLeft,
                    onClick = onBubbleClick
                )
            }
            OverlayState.CAPTURING -> {
                CapturingBubble(onClick = onBubbleClick)
            }
            OverlayState.ANALYZING -> {
                AnalyzingCapsule()
            }
            OverlayState.ERROR -> {
                ErrorBubble(
                    message = errorMessage ?: "Gagal memproses data.",
                    onRetry = onRetryClick,
                    onClose = onCloseClick
                )
            }
            OverlayState.INPUT,
            OverlayState.SUCCESS -> {
            }
        }
    }
}

@Composable
private fun IdleBubble(
    detectedTicker: String?,
    isTucked: Boolean,
    isDockedOnLeft: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderPulse"
    )

    if (detectedTicker != null && !isTucked) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(DarkSlateSurface)
                .border(
                    width = 2.dp,
                    color = AccentBlue.copy(alpha = borderPulse.coerceIn(0.6f, 1.0f)),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(end = 12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AccentBlue.copy(alpha = 0.15f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = detectedTicker,
                        color = AccentBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(AccentBlue)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "📋 DARI CLIPBOARD",
                    color = AccentBlue,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Tap untuk Tilik",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(DarkSlateSurface)
                .border(
                    width = 1.5.dp,
                    color = AccentBlue.copy(alpha = if (isTucked) 0.75f else 1.0f),
                    shape = CircleShape
                )
        ) {
            Crossfade(
                targetState = isTucked,
                animationSpec = tween(durationMillis = 200),
                label = "tuckedCrossfade"
            ) { tucked ->
                if (tucked) {
                    // Tampilan saat ngumpet di pinggir layar (Tucked state):
                    // Elemen indikator ditaruh di sisi separuh lingkaran yang menghadap ke dalam layar,
                    // sehingga tidak ada teks yang terpotong jelek oleh bezel layar.
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = if (isDockedOnLeft) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Icon(
                            imageVector = if (isDockedOnLeft) {
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight
                            } else {
                                Icons.AutoMirrored.Rounded.KeyboardArrowLeft
                            },
                            contentDescription = "Buka Tilik",
                            tint = AccentBlue,
                            modifier = Modifier
                                .padding(
                                    start = if (!isDockedOnLeft) 6.dp else 0.dp,
                                    end = if (isDockedOnLeft) 6.dp else 0.dp
                                )
                                .size(22.dp)
                        )
                    }
                } else {
                    // Tampilan normal saat aktif: Icon Tilik di tengah lingkaran utuh
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = id.tilik.app.R.drawable.favicon),
                            contentDescription = "Tilik",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CapturingBubble(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radarScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(56.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(radarScale)
                .clip(CircleShape)
                .border(1.5.dp, AccentBlue.copy(alpha = 0.4f), CircleShape)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DarkSlateSurface)
                .border(2.dp, AccentBlue, CircleShape)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = AccentBlue,
                strokeWidth = 2.5.dp
            )
        }
    }
}

@Composable
private fun AnalyzingCapsule() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(DarkSlateSurface)
            .border(1.5.dp, AccentBlue, RoundedCornerShape(21.dp))
            .padding(horizontal = 14.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = AccentBlue,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Menilik bursa...",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorBubble(
    message: String,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSlateSurface)
            .border(1.dp, DarkSlateBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = message,
            color = VerdictInvalid,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Coba Lagi",
            color = AccentBlue,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { onRetry() }
                .padding(4.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(20.dp)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Tutup",
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
