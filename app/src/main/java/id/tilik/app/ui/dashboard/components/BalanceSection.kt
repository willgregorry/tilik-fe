package id.tilik.app.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.ui.theme.AppGreen
import id.tilik.app.ui.theme.AppGreenBg
import id.tilik.app.ui.theme.AppGreenBorder
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary

import id.tilik.app.ui.components.SkeletonBox
import id.tilik.app.ui.components.shimmerEffect

@Composable
fun BalanceSection(
    isServiceRunning: Boolean,
    onToggleService: () -> Unit,
    isLoading: Boolean = false,
    balanceAmount: String? = null,
    balanceFraction: String? = null,
    trendPercent: String? = null,
    modifier: Modifier = Modifier
) {
    var isBalanceVisible by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        // Label with swap arrows icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Total Saldo Portofolio",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.CompareArrows,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }

        // Amount + Eye Icon on the left, Large circular action button on the right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                SkeletonBox(
                    modifier = Modifier
                        .width(180.dp)
                        .height(42.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.clickable { isBalanceVisible = !isBalanceVisible }
                ) {
                    if (isBalanceVisible) {
                        Text(
                            text = balanceAmount ?: "-",
                            color = TextPrimary,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        if (!balanceFraction.isNullOrBlank()) {
                            Text(
                                text = balanceFraction,
                                color = TextSecondary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "••••••••",
                            color = TextPrimary,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = if (isBalanceVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = "Toggle Balance",
                        tint = TextSecondary,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .size(18.dp)
                    )
                }
            }

            // High-contrast circular action button from design
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onToggleService() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isServiceRunning) Icons.Rounded.Stop else Icons.Rounded.Add,
                    contentDescription = if (isServiceRunning) "Hentikan Layanan" else "Mulai Layanan / Tambah",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status / Trend pill badge
        if (isLoading) {
            SkeletonBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(28.dp),
                shape = RoundedCornerShape(percent = 50)
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(AppGreenBg)
                    .border(1.dp, AppGreenBorder, RoundedCornerShape(percent = 50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = null,
                        tint = AppGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = trendPercent ?: "-",
                        color = AppGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isServiceRunning) "Layanan Tilik Aktif" else "Layanan Standby",
                        color = AppGreen.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
