package id.tilik.app.ui.dashboard.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.ui.theme.AppCard
import id.tilik.app.ui.theme.AppGreen
import id.tilik.app.ui.theme.AppRed
import id.tilik.app.ui.theme.TextMuted
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary

data class PortfolioStock(
    val ticker: String,
    val name: String,
    val statusText: String,
    val isPositive: Boolean,
    val points: List<Float> = listOf(0.7f, 0.5f, 0.8f, 0.4f, 0.3f, 0.2f),
    val historyId: String? = null
)

@Composable
fun PortfolioCarousel(
    stocks: List<PortfolioStock> = emptyList(),
    onViewDetailsClick: () -> Unit = {},
    onStockClick: (String, String?) -> Unit = { _, _ -> }
) {
    if (stocks.isEmpty()) return

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
                text = "Portofolio Saham",
                color = TextMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Lihat Detail >",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.clickable { onViewDetailsClick() }
            )
        }

        // Horizontal scrolling cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(stocks) { item ->
                PortfolioCard(
                    stock = item,
                    onClick = { onStockClick(item.ticker, item.historyId) }
                )
            }
        }
    }
}

@Composable
private fun PortfolioCard(
    stock: PortfolioStock,
    onClick: () -> Unit
) {
    val trendColor = if (stock.isPositive) AppGreen else AppRed

    Box(
        modifier = Modifier
            .width(172.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(AppCard)
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        // Sparkline Canvas at bottom
        SparklineChart(
            points = stock.points,
            lineColor = trendColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
        )

        // Card Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Ticker + Name + Round badge icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = stock.ticker,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stock.name,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (stock.isPositive) Color(0xFF1E1E1E) else Color.White)
                        .border(1.dp, Color(0x1AFFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stock.ticker.take(1),
                        color = if (stock.isPositive) Color.White else Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom figures: Verified status from API
            Column {
                Text(
                    text = stock.statusText,
                    color = trendColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (stock.isPositive) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (stock.isPositive) "Valid" else "Waspada",
                        color = trendColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SparklineChart(
    points: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1)

        val strokePath = Path()
        val fillPath = Path()

        fillPath.moveTo(0f, height)

        points.forEachIndexed { i, normY ->
            val x = i * stepX
            val y = normY * height
            if (i == 0) {
                strokePath.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                strokePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw translucent gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.25f),
                    lineColor.copy(alpha = 0.0f)
                )
            )
        )

        // Draw line
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
