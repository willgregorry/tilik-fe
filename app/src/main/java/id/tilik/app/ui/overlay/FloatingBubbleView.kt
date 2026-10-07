package id.tilik.app.ui.overlay

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.model.OverlayState
import id.tilik.app.ui.theme.AppCard
import id.tilik.app.ui.theme.BrandPrimary
import id.tilik.app.ui.theme.BrandSecondary
import id.tilik.app.ui.theme.StatusDanger
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary

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
        targetValue = if (isTucked) 0.38f else 1.0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "bubbleAlpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (isTucked) 0.72f else 1.0f,
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
                AnalyzingBubble()
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
    if (detectedTicker != null && !isTucked) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(AppCard)
                .border(
                    width = 1.5.dp,
                    color = BrandPrimary,
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(end = 14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E1E))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = detectedTicker,
                        color = BrandPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(BrandPrimary)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "DARI CLIPBOARD",
                    color = BrandSecondary,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Tap untuk Tilik",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(AppCard)
                .border(
                    width = 1.5.dp,
                    color = BrandPrimary,
                    shape = CircleShape
                )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = id.tilik.app.R.drawable.favicon),
                    contentDescription = "Tilik",
                    modifier = Modifier
                        .size(if (isTucked) 32.dp else 40.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

@Composable
private fun CapturingBubble(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AppCard)
                .border(2.dp, BrandPrimary, CircleShape)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = BrandPrimary,
                strokeWidth = 2.5.dp
            )
        }
    }
}

@Composable
private fun AnalyzingBubble() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(56.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AppCard)
                .border(2.dp, BrandPrimary, CircleShape)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = BrandPrimary,
                strokeWidth = 2.5.dp
            )
        }
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
            .background(AppCard)
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = message,
            color = StatusDanger,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Coba Lagi",
            color = BrandPrimary,
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

