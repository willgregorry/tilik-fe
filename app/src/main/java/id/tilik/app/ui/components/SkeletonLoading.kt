package id.tilik.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable Shimmer Effect Modifier for Fintech Dark Mode.
 * Runs hardware-accelerated linear gradient animation across views.
 */
fun Modifier.shimmerEffect(
    shape: Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFF141414),
    highlightColor: Color = Color(0xFF262626)
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim = transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor
        ),
        start = Offset(translateAnim.value - 300f, translateAnim.value - 300f),
        end = Offset(translateAnim.value, translateAnim.value)
    )

    this
        .clip(shape)
        .background(brush)
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Box(modifier = modifier.shimmerEffect(shape = shape))
}

@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 14.dp,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    val baseModifier = if (width != null) modifier.width(width) else modifier
    Box(
        modifier = baseModifier
            .height(height)
            .shimmerEffect(shape = shape)
    )
}

/**
 * Skeleton representation of Verdict Bottom Sheet / Card during analysis.
 */
@Composable
fun VerdictCardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFF0A0A0A))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF262626))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Header: Ticker Pill + Close
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .size(width = 68.dp, height = 28.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                SkeletonBox(
                    modifier = Modifier
                        .size(width = 140.dp, height = 18.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
            SkeletonBox(
                modifier = Modifier.size(32.dp),
                shape = CircleShape
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hero Status Banner Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .shimmerEffect(shape = RoundedCornerShape(14.dp))
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Fact Check Point Skeletons
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .shimmerEffect(shape = RoundedCornerShape(12.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Financial Health & Valuation Grid Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .shimmerEffect(shape = RoundedCornerShape(12.dp))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .shimmerEffect(shape = RoundedCornerShape(12.dp))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Close / Action Button Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shimmerEffect(shape = RoundedCornerShape(24.dp))
        )
    }
}
