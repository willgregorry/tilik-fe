package id.tilik.app.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.data.model.HistoryItemSummary
import id.tilik.app.ui.components.SkeletonBox
import id.tilik.app.ui.theme.AppAccent
import id.tilik.app.ui.theme.AppCard
import id.tilik.app.ui.theme.TextMuted
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary

@Composable
fun RecentScansSection(
    items: List<HistoryItemSummary>,
    isLoading: Boolean = false,
    onViewAllClick: () -> Unit = {},
    onItemClick: (HistoryItemSummary) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RIWAYAT PEMINDAIAN TERBARU",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            if (items.isNotEmpty()) {
                Text(
                    text = "Lihat Semua >",
                    color = AppAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isLoading && items.isEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(2) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        } else if (items.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = AppCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum Ada Riwayat Pemindaian",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aktifkan widget Tilik di atas media sosial untuk mulai memindai klaim saham bursa secara instan.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.take(3).forEach { item ->
                    RecentScanCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentScanCard(
    item: HistoryItemSummary,
    onClick: () -> Unit
) {
    val verdictLevel = item.verdict
    val formattedScore = (item.confidenceScore * 100).toInt().coerceIn(0, 100)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Ticker Pill + Platform Tag + Verdict Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val ticker = item.ticker ?: "-"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E1E))
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = ticker,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item.sourcePlatform?.uppercase() ?: "X",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(verdictLevel.containerColor)
                        .border(1.dp, verdictLevel.borderColor, RoundedCornerShape(percent = 50))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${verdictLevel.label} • $formattedScore%",
                        color = verdictLevel.color,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tweet / Claim Preview
            Text(
                text = "\"${item.tweetPreview.trim()}\"",
                color = TextPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 17.sp,
                maxLines = 2
            )

            if (!item.companyName.isNullOrBlank() && item.companyName != "-") {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.companyName,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
