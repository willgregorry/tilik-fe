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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Stop
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
import id.tilik.app.ui.theme.AppGreen
import id.tilik.app.ui.theme.AppGreenBg
import id.tilik.app.ui.theme.AppGreenBorder
import id.tilik.app.ui.theme.AppRed
import id.tilik.app.ui.theme.AppRedBg
import id.tilik.app.ui.theme.AppRedBorder
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary

@Composable
fun BalanceSection(
    isServiceRunning: Boolean,
    onToggleService: () -> Unit,
    verifiedCount: Int = 0,
    validCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        // Label Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Klaim Saham Ditilik",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(15.dp)
            )
        }

        // Hero Metric + Large Circular Action Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$verifiedCount",
                    color = TextPrimary,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = " Klaim",
                    color = TextSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            // High-contrast circular action button (50dp White circle)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onToggleService() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isServiceRunning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    contentDescription = if (isServiceRunning) "Hentikan Layanan Tilik" else "Mulai Layanan Tilik",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Service & Accuracy Pill Badge
        val badgeBg = if (isServiceRunning) AppGreenBg else Color(0x14FFFFFF)
        val badgeBorder = if (isServiceRunning) AppGreenBorder else Color(0x22FFFFFF)
        val badgeColor = if (isServiceRunning) AppGreen else TextSecondary

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(badgeBg)
                .border(1.dp, badgeBorder, RoundedCornerShape(percent = 50))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isServiceRunning) Icons.Rounded.Shield else Icons.Rounded.PowerSettingsNew,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isServiceRunning) "Layanan Tilik Aktif" else "Layanan Standby",
                    color = badgeColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (verifiedCount > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $validCount Valid",
                        color = badgeColor.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
