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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import id.tilik.app.data.model.BrokerDetail
import id.tilik.app.data.model.FactCheckPoint
import id.tilik.app.data.model.VerdictLevel
import id.tilik.app.data.model.VerificationResponse
import id.tilik.app.data.util.FinancialFormatter
import id.tilik.app.ui.theme.BrandPrimary
import id.tilik.app.ui.theme.StatusDanger
import id.tilik.app.ui.theme.StatusDangerBg
import id.tilik.app.ui.theme.StatusSuccess
import id.tilik.app.ui.theme.StatusSuccessBg
import id.tilik.app.ui.theme.StatusWarning
import id.tilik.app.ui.theme.StatusWarningBg
import id.tilik.app.ui.theme.TextMuted
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Reference Dark Investment Design Tokens
private val CardBackground = Color(0xFF0A0A0A)
private val SectionCardBg = Color(0xFF141414)
private val BorderColor = Color(0x14FFFFFF)
private val TextDark = Color(0xFFFFFFFF)
private val TextBody = Color(0xFFE2E8F0)
private val TextSubtle = Color(0xFF888888)

@Composable
fun VerdictCardView(
    data: VerificationResponse,
    claimText: String? = null,
    onCloseClick: () -> Unit
) {
    val animOffsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 90.dp.toPx() }
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
    val scrollState = rememberScrollState()

    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.86f)
            .offset { IntOffset(0, currentDragOffset.roundToInt()) }
            .border(
                1.dp,
                BorderColor,
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Mencegah klik di dalam sheet tembus ke scrim
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Drag Handle Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCBD5E1))
                )
            }

            // Top Header: Stock Ticker + Company Name + Close Button
            HeaderSection(
                data = data,
                onCloseClick = onCloseClick,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            HorizontalDivider(color = BorderColor, thickness = 1.dp)

            // Scrollable Content Area with Generous Spacing
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Verdict Status Banner (Solid White Card with Neutral Border, NO glowing fill)
                VerdictHeroBanner(data = data)

                // 2. Klaim yang Dianalisis (Jika ada)
                if (!claimText.isNullOrBlank()) {
                    ClaimQuoteSection(claimText = claimText)
                }

                // 3. Poin-Poin Hasil Pemeriksaan Fakta
                if (data.points.isNotEmpty()) {
                    FactPointsSection(points = data.points)
                }

                // 4. Data Pasar & Finansial (Foreign Flow, Valuasi, Fundamental)
                MarketDataSection(data = data)

                // 5. Disclaimer Edukasi
                DisclaimerSection()

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Pinned Bottom Action Button (GoPay / OVO Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .border(1.dp, BorderColor)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onCloseClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Tutup",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Header Section: Stock Ticker + Company Name + Circular Close Button
 */
@Composable
private fun HeaderSection(
    data: VerificationResponse,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayTicker = data.ticker ?: "IDX"

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Ticker Pill (Solid Neutral Gray Container, NO colored border)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = displayTicker,
                    color = BrandPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Company Name
            if (!data.companyName.isNullOrBlank()) {
                Text(
                    text = data.companyName,
                    color = TextDark,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Circular Close Button
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1E1E))
                .clickable { onCloseClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Tutup",
                tint = TextSubtle,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Status Hero Banner: Solid White Card, Neutral Border, Clean Material Icons
 * (Zero glowing card, zero low-opacity borders)
 */
@Composable
private fun VerdictHeroBanner(data: VerificationResponse) {
    val level = data.verdictLevel

    val accentColor = when (level) {
        VerdictLevel.GREEN -> StatusSuccess
        VerdictLevel.YELLOW -> StatusWarning
        VerdictLevel.RED -> StatusDanger
    }

    val icon = when (level) {
        VerdictLevel.GREEN -> Icons.Rounded.CheckCircle
        VerdictLevel.YELLOW -> Icons.Rounded.WarningAmber
        VerdictLevel.RED -> Icons.Rounded.Warning
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = level.label,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = level.title,
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = level.label,
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Confidence Badge (Clean Neutral Gray, NO glowing border)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E1E1E))
                        .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Akurasi ${data.confidencePercentage}%",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Cooling-off prompt / Catatan Analisis
            if (data.coolingOffPrompt.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderColor, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = data.coolingOffPrompt,
                    color = TextBody,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Klaim yang Dianalisis Quote Section
 */
@Composable
private fun ClaimQuoteSection(claimText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SectionCardBg)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = "KLAIM YANG DIANALISIS",
            color = TextSubtle,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "\"$claimText\"",
            color = TextBody,
            fontSize = 13.5.sp,
            fontStyle = FontStyle.Italic,
            lineHeight = 19.sp
        )
    }
}

/**
 * Poin-Poin Hasil Pemeriksaan Fakta (Clean Fintech List Cards)
 */
@Composable
private fun FactPointsSection(points: List<FactCheckPoint>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Pemeriksaan Fakta",
            color = TextDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        points.forEach { point ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SectionCardBg)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = if (point.isFavorable) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                    contentDescription = if (point.isFavorable) "Favorable" else "Warning",
                    tint = if (point.isFavorable) StatusSuccess else StatusDanger,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 1.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = point.title,
                        color = TextDark,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = point.fact,
                        color = TextBody,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

/**
 * Data Pasar & Finansial: Foreign Flow, Valuasi, dan Kesehatan Finansial
 */
@Composable
private fun MarketDataSection(data: VerificationResponse) {
    val details = data.details
    val broker = details.brokerFlow
    val valuation = details.valuation
    val health = details.financialHealth

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Data Pasar & Finansial",
            color = TextDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        // Card 1: Aliran Dana Asing (Foreign Flow)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SectionCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Row: Title on Left, Net Amount on Right
                val isNetBuy = broker.foreignNetIdr >= 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isNetBuy) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = if (isNetBuy) StatusSuccess else StatusDanger,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Arus Modal Asing",
                            color = TextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${if (isNetBuy) "+" else ""}${FinancialFormatter.formatIdrShort(broker.foreignNetIdr)}",
                        color = if (isNetBuy) StatusSuccess else StatusDanger,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Summary Verdict Text (Full width, generous line height)
                if (broker.summaryVerdict.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = broker.summaryVerdict,
                        color = TextBody,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }

                // Top Buyers & Top Sellers List (Full Width, never cramped)
                if (broker.topBuyers.isNotEmpty() || broker.topSellers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderColor, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Top Buyers Section
                    if (broker.topBuyers.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess)
                            )
                            Text(
                                text = "Top Buyers (Akumulasi)",
                                color = StatusSuccess,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        broker.topBuyers.take(3).forEach { b ->
                            BrokerRowItem(broker = b, isBuyer = true)
                        }
                    }

                    // Top Sellers Section
                    if (broker.topSellers.isNotEmpty()) {
                        if (broker.topBuyers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StatusDanger)
                            )
                            Text(
                                text = "Top Sellers (Distribusi)",
                                color = StatusDanger,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        broker.topSellers.take(3).forEach { b ->
                            BrokerRowItem(broker = b, isBuyer = false)
                        }
                    }
                }
            }
        }

        // Card 2: Valuasi Saham vs Industri
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SectionCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Valuasi Saham",
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatBox(
                        label = "PBV Ratio",
                        value = FinancialFormatter.formatRatio(valuation.pbvRatio),
                        comparison = "Median Sektor: ${FinancialFormatter.formatRatio(valuation.industryMedianPbv)}",
                        modifier = Modifier.weight(1f)
                    )

                    MetricStatBox(
                        label = "PER Ratio",
                        value = FinancialFormatter.formatRatio(valuation.peRatio),
                        comparison = "Median Sektor: ${FinancialFormatter.formatRatio(valuation.industryMedianPe)}",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Full-width Valuation Status Banner (No cramped header box)
                if (valuation.valuationStatus.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SectionCardBg)
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = StatusWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = valuation.valuationStatus,
                                color = TextBody,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Card 3: Kesehatan Finansial & Notasi Khusus BEI
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SectionCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Kesehatan Fundamental & Notasi",
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pertumbuhan Laba Bersih",
                        color = TextBody,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${FinancialFormatter.formatPercentage(health.netProfitGrowthYoy)} YoY",
                        color = TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (health.operatingCashFlowIdr != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Arus Kas Operasional",
                            color = TextBody,
                            fontSize = 13.sp
                        )
                        Text(
                            text = FinancialFormatter.formatIdrShort(health.operatingCashFlowIdr),
                            color = TextDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                HorizontalDivider(color = BorderColor, thickness = 0.8.dp)

                // Status FCA & Papan Perdagangan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Papan Perdagangan",
                        color = TextBody,
                        fontSize = 13.sp
                    )
                    if (health.isFca) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusDangerBg)
                                .border(1.dp, StatusDanger, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = StatusDanger,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Papan Khusus (FCA)",
                                    color = StatusDanger,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusSuccessBg)
                                .border(1.dp, Color(0x3322C55E), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusSuccess,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reguler (Bebas FCA)",
                                    color = StatusSuccess,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notasi Khusus BEI",
                        color = TextBody,
                        fontSize = 13.sp
                    )
                    if (health.specialNotations.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusWarningBg)
                                .border(1.dp, StatusWarning, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = health.specialNotations.joinToString(", "),
                                color = StatusWarning,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "Bersih (Tidak Ada Notasi)",
                            color = TextSubtle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricStatBox(
    label: String,
    value: String,
    comparison: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SectionCardBg)
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text(
            text = label,
            color = TextSubtle,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = TextDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = comparison,
            color = TextSubtle,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun BrokerRowItem(
    broker: BrokerDetail,
    isBuyer: Boolean
) {
    val shortType = if (broker.brokerType.contains("Asing", ignoreCase = true)) "Asing" else "Domestik"
    val isForeign = shortType == "Asing"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = broker.brokerCode,
                    color = TextDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = shortType,
                color = if (isForeign) Color(0xFF0284C7) else TextSubtle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "${if (isBuyer) "+" else "-"}${FinancialFormatter.formatIdrShort(broker.netValueIdr)}",
            color = if (isBuyer) StatusSuccess else StatusDanger,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun DisclaimerSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = "Disclaimer",
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Data bersumber dari Sectors Financial API. Informasi bersifat edukasi pasar modal dan bukan rekomendasi jual/beli (DYOR).",
            color = TextSubtle,
            fontSize = 10.5.sp,
            lineHeight = 15.sp,
            fontStyle = FontStyle.Italic
        )
    }
}
