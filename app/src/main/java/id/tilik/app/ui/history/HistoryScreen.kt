package id.tilik.app.ui.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.ui.components.BadgeType
import id.tilik.app.ui.theme.AppBackground
import id.tilik.app.ui.theme.AppBorder
import id.tilik.app.ui.theme.AppSurface
import id.tilik.app.ui.theme.AppSurfaceSubtle
import id.tilik.app.ui.theme.BrandPrimary
import id.tilik.app.ui.theme.TextMuted
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Valid", "Meragukan", "Tidak Valid")

    val sampleHistory = remember {
        listOf(
            HistoryItem(
                id = "1",
                ticker = "BBRI",
                companyName = "Bank Rakyat Indonesia",
                claim = "BBRI cetak laba bersih kuartal ini tembus rekor dan asing akumulasi masif.",
                verdictTitle = "Valid",
                verdictType = BadgeType.SUCCESS,
                timestamp = "10 menit yang lalu",
                foreignFlow = "Net Foreign: +Rp 340,5 Miliar",
                valuationNote = "PE: 11.2x | PBV: 2.1x"
            ),
            HistoryItem(
                id = "2",
                ticker = "GOTO",
                companyName = "GoTo Gojek Tokopedia",
                claim = "GOTO targetkan dividen jumbo tahun ini setelah efisiensi beban.",
                verdictTitle = "Tidak Valid",
                verdictType = BadgeType.DANGER,
                timestamp = "2 jam yang lalu",
                foreignFlow = "Net Foreign: -Rp 42,1 Miliar",
                valuationNote = "Belum membagikan dividen tunai historis"
            ),
            HistoryItem(
                id = "3",
                ticker = "BBCA",
                companyName = "Bank Central Asia",
                claim = "Asing terus buang barang di saham BBCA karena valuasi sudah kemahalan.",
                verdictTitle = "Meragukan",
                verdictType = BadgeType.WARNING,
                timestamp = "Kemarin",
                foreignFlow = "Net Foreign: Netral (-Rp 12 Miliar)",
                valuationNote = "PBV 4.8x konsisten dengan rata-rata 5 tahun"
            ),
            HistoryItem(
                id = "4",
                ticker = "AMMN",
                companyName = "Amman Mineral Internasional",
                claim = "Kenaikan laba AMMN didorong lonjakan produksi konsentrat tembaga.",
                verdictTitle = "Valid",
                verdictType = BadgeType.SUCCESS,
                timestamp = "2 hari yang lalu",
                foreignFlow = "Net Foreign: +Rp 88,2 Miliar",
                valuationNote = "Sesuai laporan keuangan kuartalan BEI"
            )
        )
    }

    val filteredList = remember(selectedFilter) {
        if (selectedFilter == "Semua") sampleHistory
        else sampleHistory.filter { it.verdictTitle.equals(selectedFilter, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title & Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Riwayat Pemeriksaan",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                )
                Text(
                    text = "${filteredList.size} verifikasi tersimpan dari bursa",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) BrandPrimary else AppSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) BrandPrimary else AppBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) androidx.compose.ui.graphics.Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History Items List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum ada riwayat untuk filter ini",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    HistoryItemCard(item = item)
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
