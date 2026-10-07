package id.tilik.app.ui.markets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.data.model.HistoryDetailResponse
import id.tilik.app.data.model.VerdictLevel
import id.tilik.app.data.model.VerificationResponse
import id.tilik.app.data.repository.AuthRepository
import id.tilik.app.ui.components.SkeletonBox
import id.tilik.app.ui.theme.AppAccent
import id.tilik.app.ui.theme.AppBackground
import id.tilik.app.ui.theme.AppCard
import id.tilik.app.ui.theme.AppCardSubtle
import id.tilik.app.ui.theme.AppGreen
import id.tilik.app.ui.theme.AppGreenBg
import id.tilik.app.ui.theme.AppGreenBorder
import id.tilik.app.ui.theme.AppRed
import id.tilik.app.ui.theme.AppRedBg
import id.tilik.app.ui.theme.AppRedBorder
import id.tilik.app.ui.theme.TextMuted
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary

@Composable
fun MarketsScreen(
    historyId: String? = null,
    initialTicker: String? = null,
    onBackClick: (() -> Unit)? = null
) {
    var isLoading by remember { mutableStateOf(false) }
    var detailResponse by remember { mutableStateOf<HistoryDetailResponse?>(null) }
    var selectedTicker by remember { mutableStateOf(initialTicker ?: "") }
    val scrollState = rememberScrollState()

    LaunchedEffect(historyId) {
        if (!historyId.isNullOrBlank()) {
            isLoading = true
            val result = AuthRepository.getHistoryDetail(historyId)
            if (result.isSuccess) {
                detailResponse = result.getOrNull()
                detailResponse?.verification?.ticker?.let {
                    selectedTicker = it
                }
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBackClick != null) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AppCard)
                            .border(1.dp, Color(0x14FFFFFF), CircleShape)
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column {
                    Text(
                        text = "Detail Forensik Bursa",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Data resmi Sectors API v2 & Algoritma Tilik AI",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppAccent)
            }
        } else if (detailResponse == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppCard)
                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "-",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Belum Ada Forensik Dipilih",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih item dari Riwayat Verifikasi untuk menelaah forensik bursa & valuasi Sectors API.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            val v = detailResponse?.verification

            // Emiten Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = AppCard),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = v?.ticker ?: selectedTicker.ifBlank { "-" },
                                    color = TextPrimary,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x1AFFFFFF))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "BEI / IDX",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = v?.companyName ?: "-",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        val verdictLevel = v?.verdictLevel ?: VerdictLevel.SESUAI_FAKTA
                        val confidence = v?.confidencePercentage

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(verdictLevel.containerColor)
                                .border(1.dp, verdictLevel.borderColor, RoundedCornerShape(percent = 50))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (confidence != null) "${verdictLevel.label} • $confidence%" else verdictLevel.label,
                                color = verdictLevel.color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Klaim yang Diverifikasi
            val tweetText = detailResponse?.tweetText ?: "-"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = AppCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "TEKS KLAIM YANG DITELUSURI",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tweetText,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Cooling-off Prompt & Catatan Panduan
            val prompt = v?.coolingOffPrompt ?: "-"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x14FF5C35))
                    .border(1.dp, Color(0x33FF5C35), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = AppAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Refleksi Investor",
                            color = AppAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = prompt,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Valuasi Fundamental (Sectors API)
            val valDetail = v?.details?.valuation
            val peRatio = valDetail?.peRatio?.let { String.format(java.util.Locale.US, "%.1fx", it) } ?: "-"
            val peMedian = valDetail?.industryMedianPe?.let { String.format(java.util.Locale.US, "%.1fx", it) } ?: "-"
            val pbvRatio = valDetail?.pbvRatio?.let { String.format(java.util.Locale.US, "%.1fx", it) } ?: "-"
            val pbvMedian = valDetail?.industryMedianPbv?.let { String.format(java.util.Locale.US, "%.1fx", it) } ?: "-"

            Text(
                text = "VALUASI FUNDAMENTAL SECTORS",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ValuationCardItem(
                    label = "P/E Ratio",
                    value = peRatio,
                    note = "Median Industri: $peMedian",
                    modifier = Modifier.weight(1f)
                )
                ValuationCardItem(
                    label = "PBV Ratio",
                    value = pbvRatio,
                    note = "Median Industri: $pbvMedian",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Arus Modal Asing (Broker Summary)
            val brokerFlow = v?.details?.brokerFlow
            val netFlowIdr = brokerFlow?.foreignNetIdr
            val isFlowPositive = (netFlowIdr ?: 0.0) >= 0
            val formattedFlow = when {
                netFlowIdr == null -> "-"
                netFlowIdr >= 0 -> "+${formatFlowCurrency(netFlowIdr)}"
                else -> "-${formatFlowCurrency(kotlin.math.abs(netFlowIdr))}"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = AppCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Arus Modal Asing (Broker Flow)",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formattedFlow,
                            color = if (netFlowIdr == null) TextMuted else if (isFlowPositive) AppGreen else AppRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0x0FFFFFFF), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Top Foreign Buyers",
                        color = AppGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (brokerFlow?.topBuyers?.isNotEmpty() == true) {
                        brokerFlow.topBuyers.forEach { b ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${b.brokerCode} (${b.brokerType})", color = TextSecondary, fontSize = 12.sp)
                                Text(text = formatFlowCurrency(b.netValueIdr), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Text(text = "-", color = TextMuted, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Top Sellers",
                        color = AppRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (brokerFlow?.topSellers?.isNotEmpty() == true) {
                        brokerFlow.topSellers.forEach { b ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${b.brokerCode} (${b.brokerType})", color = TextSecondary, fontSize = 12.sp)
                                Text(text = formatFlowCurrency(b.netValueIdr), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Text(text = "-", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

private fun formatFlowCurrency(amount: Double): String {
    val absAmount = kotlin.math.abs(amount)
    return when {
        absAmount >= 1_000_000_000_000.0 -> String.format(java.util.Locale.US, "Rp %.1f Triliun", absAmount / 1_000_000_000_000.0)
        absAmount >= 1_000_000_000.0 -> String.format(java.util.Locale.US, "Rp %.1f Miliar", absAmount / 1_000_000_000.0)
        absAmount >= 1_000_000.0 -> String.format(java.util.Locale.US, "Rp %.1f Juta", absAmount / 1_000_000.0)
        absAmount > 0 -> String.format(java.util.Locale.US, "Rp %,.0f", absAmount)
        else -> "Rp 0"
    }
}

@Composable
private fun ValuationCardItem(
    label: String,
    value: String,
    note: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, color = TextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = note, color = TextSecondary, fontSize = 10.sp)
        }
    }
}
