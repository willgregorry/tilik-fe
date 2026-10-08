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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.ui.theme.AppCard
import id.tilik.app.ui.theme.AppGreen
import id.tilik.app.ui.theme.AppRed
import id.tilik.app.ui.theme.TextMuted
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary

data class WatchlistItem(
    val ticker: String,
    val name: String,
    val statusText: String,
    val isPositive: Boolean,
    val historyId: String? = null
)

@Composable
fun WatchlistSection(
    items: List<WatchlistItem> = emptyList(),
    onAddWatchlistClick: () -> Unit = {},
    onItemClick: (String, String?) -> Unit = { _, _ -> }
) {
    if (items.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "My Watchlist",
                color = TextMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onAddWatchlistClick() }
            ) {
                Text(
                    text = "Add Watchlist",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // List Items
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(top = 6.dp)
        ) {
            items.forEach { item ->
                WatchlistRow(
                    item = item,
                    onClick = { onItemClick(item.ticker, item.historyId) }
                )
            }
        }
    }
}

@Composable
private fun WatchlistRow(
    item: WatchlistItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Squircle Icon + Ticker + Company Name
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppCard)
                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.ticker.take(2),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = item.ticker,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.name,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }
        }

        // Right: Status badge from API
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.statusText,
                color = if (item.isPositive) AppGreen else AppRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (item.isPositive) "Valid" else "Waspada",
                color = if (item.isPositive) AppGreen else AppRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
