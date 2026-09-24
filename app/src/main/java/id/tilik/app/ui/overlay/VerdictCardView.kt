package id.tilik.app.ui.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.model.VerdictType
import id.tilik.app.model.VerifyData
import id.tilik.app.ui.theme.DarkSlateBackground
import id.tilik.app.ui.theme.DarkSlateBorder
import id.tilik.app.ui.theme.DarkSlateSurface
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary
import id.tilik.app.ui.theme.VerdictInvalid
import id.tilik.app.ui.theme.VerdictMisleading
import id.tilik.app.ui.theme.VerdictValid
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun VerdictCardView(
    data: VerifyData,
    onCloseClick: () -> Unit
) {
    val animOffsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 80.dp.toPx() }
    var lastDragVelocity by remember { mutableStateOf(0f) }
    var lastDragTime by remember { mutableStateOf(0L) }

    fun onDragDelta(dragAmount: Float) {
        val now = System.currentTimeMillis()
        val dt = (now - lastDragTime).coerceAtLeast(1)
        lastDragVelocity = (dragAmount / dt) * 1000f
        lastDragTime = now
        coroutineScope.launch {
            animOffsetY.snapTo((animOffsetY.value + dragAmount).coerceAtLeast(0f))
        }
    }

    fun onDragFinished() {
        if (animOffsetY.value > dismissThresholdPx || lastDragVelocity > 700f) {
            coroutineScope.launch {
                animOffsetY.animateTo(
                    targetValue = 1800f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                )
                onCloseClick()
            }
        } else {
            coroutineScope.launch {
                animOffsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    val currentDragOffset = animOffsetY.value.coerceAtLeast(0f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, currentDragOffset.roundToInt()) }
            .padding(16.dp)
            .border(1.dp, DarkSlateBorder, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        lastDragVelocity = 0f
                        lastDragTime = System.currentTimeMillis()
                    },
                    onDragEnd = { onDragFinished() },
                    onDragCancel = {
                        coroutineScope.launch { animOffsetY.animateTo(0f) }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        onDragDelta(dragAmount)
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Drag Handle Bar
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF4B4F58))
            )
            HeaderSection(data = data, onCloseClick = onCloseClick)
            Spacer(modifier = Modifier.height(12.dp))
            ClaimSection(claim = data.influencerClaim)
            Spacer(modifier = Modifier.height(12.dp))
            GroundingGridSection(data = data)
            Spacer(modifier = Modifier.height(12.dp))
            SynthesisSection(summary = data.aiSummary)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DarkSlateBorder, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))
            DisclaimerSection(disclaimer = data.disclaimer)
        }
    }
}

@Composable
private fun HeaderSection(
    data: VerifyData,
    onCloseClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSlateBackground)
                    .border(1.dp, DarkSlateBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = data.ticker,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            VerdictBadge(verdict = data.verdict)
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onCloseClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Tutup",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
    if (data.companyName.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = data.companyName,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun VerdictBadge(verdict: String) {
    val verdictType = VerdictType.fromString(verdict)
    val (badgeColor, label) = when (verdictType) {
        VerdictType.VALID -> Pair(VerdictValid, "FAKTA / VALID")
        VerdictType.MISLEADING -> Pair(VerdictMisleading, "MENYESATKAN")
        VerdictType.UNSUBSTANTIATED -> Pair(VerdictInvalid, "TIDAK SESUAI DATA")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = badgeColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ClaimSection(claim: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSlateBackground)
            .padding(10.dp)
    ) {
        Text(
            text = "KLAIM KONTEN",
            color = TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "\"$claim\"",
            color = TextPrimary,
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun GroundingGridSection(data: VerifyData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricBox(
            title = "FOREIGN FLOW",
            primaryValue = data.marketEvidence.foreignFlow.valueFormatted,
            secondaryValue = "${data.marketEvidence.foreignFlow.status} (${data.marketEvidence.foreignFlow.period})",
            modifier = Modifier.weight(1f)
        )
        MetricBox(
            title = "VALUASI & KINERJA",
            primaryValue = "PER ${data.marketEvidence.valuation.peRatio ?: "-"}x | PBV ${data.marketEvidence.valuation.pbvRatio ?: "-"}x",
            secondaryValue = "Laba YoY: ${data.marketEvidence.valuation.yoyProfitGrowth ?: "-"}",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricBox(
    title: String,
    primaryValue: String,
    secondaryValue: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSlateBackground)
            .border(0.8.dp, DarkSlateBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = primaryValue,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = secondaryValue,
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun SynthesisSection(summary: String) {
    Text(
        text = summary,
        color = TextPrimary,
        fontSize = 12.sp,
        lineHeight = 17.sp
    )
}

@Composable
private fun DisclaimerSection(disclaimer: String) {
    Text(
        text = disclaimer,
        color = TextSecondary,
        fontSize = 9.sp,
        fontStyle = FontStyle.Italic,
        lineHeight = 12.sp
    )
}
