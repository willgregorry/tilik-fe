package id.tilik.app.ui.settings

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.ScreenShare
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Api
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import id.tilik.app.data.repository.AuthRepository
import id.tilik.app.data.session.SessionManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.R
import id.tilik.app.ui.theme.AppAccent
import id.tilik.app.ui.theme.AppBackground
import id.tilik.app.ui.theme.AppBorder
import id.tilik.app.ui.theme.AppCard
import id.tilik.app.ui.theme.AppGreen
import id.tilik.app.ui.theme.AppGreenBg
import id.tilik.app.ui.theme.AppRed
import id.tilik.app.ui.theme.AppRedBg
import id.tilik.app.ui.theme.TextMuted
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    hasOverlay: Boolean,
    hasAudio: Boolean,
    hasProjection: Boolean,
    isRunning: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestAudio: () -> Unit,
    onRequestProjection: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenAppDetails: () -> Unit,
    onToggleService: () -> Unit,
    onOpenProfileDetail: () -> Unit = {},
    onOpenAuth: () -> Unit = {}
) {
    var autoClipboardEnabled by remember { mutableStateOf(true) }
    var hapticFeedbackEnabled by remember { mutableStateOf(true) }
    var amoledThemeEnabled by remember { mutableStateOf(true) }

    val currentUser by SessionManager.currentUserState.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val profileName = currentUser?.name?.takeIf { it.isNotBlank() } ?: "User"
    val profileEmail = currentUser?.email ?: "-"
    val profileRole = currentUser?.userRole?.displayName ?: "-"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Page Title
        Text(
            text = "Pengaturan",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 1. Profile Top Card (navigates to ProfileDetailScreen)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
                .clickable { onOpenProfileDetail() },
            colors = CardDefaults.cardColors(containerColor = AppCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AFFFFFF))
                            .border(1.5.dp, AppAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!currentUser?.picture.isNullOrBlank()) {
                            AsyncImage(
                                model = currentUser?.picture,
                                contentDescription = "Profile Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.favicon),
                                contentDescription = "Profile Avatar",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profileName,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = profileEmail,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(Color(0x1FFF5C35))
                                .border(1.dp, Color(0x40FF5C35), RoundedCornerShape(percent = 50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = profileRole.uppercase(),
                                color = AppAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "Detail Profil",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Settings Category: Floating Fact-Checker & Sistem
        SettingsCategoryHeader(title = "LAYANAN FLOATING FACT-CHECKER")
        SettingsCardGroup {
            // Master Toggle Switch
            SettingsSwitchRow(
                icon = Icons.Rounded.Layers,
                iconTint = AppAccent,
                title = "Widget Melayang Tilik",
                subtitle = if (isRunning) "Aktif melayang di atas aplikasi lain" else "Layanan nonaktif",
                checked = isRunning,
                onCheckedChange = { onToggleService() }
            )

            SettingsDivider()

            // Overlay Permission
            SettingsActionRow(
                icon = Icons.Rounded.Visibility,
                iconTint = Color(0xFF60A5FA),
                title = "Izin Tampil di Atas Aplikasi",
                subtitle = "Menampilkan floating bubble di layar",
                statusBadgeText = if (hasOverlay) "Aktif" else "Perlu Izin",
                isPositive = hasOverlay,
                onClick = onRequestOverlay
            )

            SettingsDivider()

            // Screen Capture Permission
            SettingsActionRow(
                icon = Icons.AutoMirrored.Rounded.ScreenShare,
                iconTint = Color(0xFFA78BFA),
                title = "Izin Rekam Layar",
                subtitle = "Analisis klaim dan grafik di layar",
                statusBadgeText = if (hasProjection) "Aktif" else "Minta Izin",
                isPositive = hasProjection,
                onClick = onRequestProjection
            )

            SettingsDivider()

            // Accessibility Service
            SettingsActionRow(
                icon = Icons.Rounded.AccessibilityNew,
                iconTint = Color(0xFF34D399),
                title = "Layanan Aksesibilitas Tilik",
                subtitle = "Deteksi teks layar dan pergantian aplikasi",
                statusBadgeText = "Buka Pengaturan",
                isPositive = false,
                onClick = onOpenAccessibility
            )

            SettingsDivider()

            // Internal Audio
            SettingsActionRow(
                icon = Icons.Rounded.Mic,
                iconTint = Color(0xFFFBBF24),
                title = "Izin Rekam Audio Internal",
                subtitle = "Deteksi audio streaming bursa saham",
                statusBadgeText = if (hasAudio) "Aktif" else "Minta Izin",
                isPositive = hasAudio,
                onClick = onRequestAudio
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Settings Category: Preferensi Deteksi
        SettingsCategoryHeader(title = "PREFERENSI DETEKSI")
        SettingsCardGroup {
            SettingsSwitchRow(
                icon = Icons.Rounded.ContentPaste,
                iconTint = Color(0xFF38BDF8),
                title = "Deteksi Otomatis Clipboard",
                subtitle = "Sinkronisasi otomatis saat salin klaim saham",
                checked = autoClipboardEnabled,
                onCheckedChange = { autoClipboardEnabled = it }
            )

            SettingsDivider()

            SettingsSwitchRow(
                icon = Icons.Rounded.Vibration,
                iconTint = Color(0xFFFB923C),
                title = "Haptic Feedback (Getar)",
                subtitle = "Getar lembut saat saham BEI teridentifikasi",
                checked = hapticFeedbackEnabled,
                onCheckedChange = { hapticFeedbackEnabled = it }
            )

            SettingsDivider()

            SettingsSwitchRow(
                icon = Icons.Rounded.Tune,
                iconTint = Color(0xFF4ADE80),
                title = "Mode Gelap AMOLED Murni",
                subtitle = "Maksimal hemat baterai dan kontras tinggi",
                checked = amoledThemeEnabled,
                onCheckedChange = { amoledThemeEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Settings Category: Data & Sectors API
        SettingsCategoryHeader(title = "DATA & INTEGRASI SECTORS")
        SettingsCardGroup {
            SettingsActionRow(
                icon = Icons.Rounded.Api,
                iconTint = AppAccent,
                title = "Sectors Financial API",
                subtitle = "Koneksi data fundamental & broker bursa",
                statusBadgeText = "Terhubung",
                isPositive = true,
                onClick = {}
            )

            SettingsDivider()

            SettingsActionRow(
                icon = Icons.Rounded.DeleteOutline,
                iconTint = AppRed,
                title = "Bersihkan Cache & Riwayat",
                subtitle = "Hapus riwayat akun di backend & lokal",
                statusBadgeText = "Bersihkan",
                isPositive = false,
                onClick = {
                    scope.launch {
                        AuthRepository.clearHistory()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Settings Category: Akun & Keamanan
        SettingsCategoryHeader(title = "AKUN & AUTENTIKASI")
        SettingsCardGroup {
            SettingsActionRow(
                icon = Icons.Rounded.Lock,
                iconTint = Color(0xFF818CF8),
                title = "Masuk / Ganti Akun",
                subtitle = "Buka halaman autentikasi akun Tilik",
                statusBadgeText = "Buka",
                isPositive = true,
                onClick = onOpenAuth
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Settings Category: Tentang Aplikasi
        SettingsCategoryHeader(title = "TENTANG")
        SettingsCardGroup {
            SettingsActionRow(
                icon = Icons.Rounded.Info,
                iconTint = TextSecondary,
                title = "Versi Aplikasi",
                subtitle = "Tilik Android v1.0.0 (Production)",
                statusBadgeText = "Terbaru",
                isPositive = true,
                onClick = onOpenAppDetails
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
    )
}

@Composable
private fun SettingsCardGroup(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = Color(0x0FFFFFFF),
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppAccent,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Color(0x1FFFFFFF)
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    statusBadgeText: String,
    isPositive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(if (isPositive) AppGreenBg else AppRedBg)
                .border(
                    1.dp,
                    if (isPositive) Color(0x3322C55E) else Color(0x33EF4444),
                    RoundedCornerShape(percent = 50)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = statusBadgeText,
                color = if (isPositive) AppGreen else AppRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
