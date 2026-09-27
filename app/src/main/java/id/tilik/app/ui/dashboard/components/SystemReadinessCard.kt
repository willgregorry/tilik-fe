package id.tilik.app.ui.dashboard.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.ScreenShare
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.ui.theme.AppBorder
import id.tilik.app.ui.theme.AppBorderLight
import id.tilik.app.ui.theme.AppSurface
import id.tilik.app.ui.theme.BrandPrimary
import id.tilik.app.ui.theme.StatusSuccess
import id.tilik.app.ui.theme.TextMuted
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary

@Composable
fun SystemReadinessCard(
    hasOverlay: Boolean,
    hasProjection: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestProjection: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenAppDetails: () -> Unit
) {
    val activeCount = listOf(hasOverlay, hasProjection, true, true).count { it }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Label (Grouped iOS / Settings Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "KESIAPAN SISTEM",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "$activeCount dari 4 aktif",
                color = if (activeCount == 4) StatusSuccess else TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Grouped Card Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                SettingRow(
                    icon = Icons.Rounded.Layers,
                    title = "Floating Overlay",
                    subtitle = "Izin melayang di atas aplikasi lain",
                    isGranted = hasOverlay,
                    actionLabel = "Izinkan",
                    onAction = onRequestOverlay
                )

                HorizontalDivider(
                    color = AppBorderLight,
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(start = 32.dp)
                )

                SettingRow(
                    icon = Icons.Rounded.ScreenShare,
                    title = "Tangkapan Layar",
                    subtitle = "MediaProjection pembaca klaim",
                    isGranted = hasProjection,
                    actionLabel = "Bagikan",
                    onAction = onRequestProjection
                )

                HorizontalDivider(
                    color = AppBorderLight,
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(start = 32.dp)
                )

                SettingRow(
                    icon = Icons.Rounded.ContentCopy,
                    title = "Deteksi Clipboard",
                    subtitle = "Aksesibilitas sinkronisasi otomatis",
                    isGranted = true,
                    actionLabel = "Atur",
                    onAction = onOpenAccessibility
                )

                HorizontalDivider(
                    color = AppBorderLight,
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(start = 32.dp)
                )

                SettingRow(
                    icon = Icons.Rounded.Security,
                    title = "Setelan Latar Belakang",
                    subtitle = "Perlindungan memori OriginOS/HyperOS",
                    isGranted = true,
                    actionLabel = "Buka Info",
                    onAction = onOpenAppDetails
                )
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Clean direct leading icon without artificial squircle container
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isGranted) TextPrimary else TextMuted,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Text Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Trailing Status or Clean Pill Button
        if (isGranted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Aktif",
                    tint = StatusSuccess,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Aktif",
                    color = StatusSuccess,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary,
                    contentColor = Color.White
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
