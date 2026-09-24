package id.tilik.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import id.tilik.app.service.OverlayService
import id.tilik.app.ui.theme.AccentBlue
import id.tilik.app.ui.theme.DarkSlateBackground
import id.tilik.app.ui.theme.DarkSlateBorder
import id.tilik.app.ui.theme.DarkSlateSurface
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary
import id.tilik.app.ui.theme.TilikTheme
import id.tilik.app.ui.theme.VerdictInvalid
import id.tilik.app.ui.theme.VerdictValid

class MainActivity : ComponentActivity() {

    private var hasOverlayPermission by mutableStateOf(false)
    private var hasAudioPermission by mutableStateOf(false)
    private var hasProjectionPermission by mutableStateOf(false)
    private var isServiceRunning by mutableStateOf(false)

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
    }

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            hasProjectionPermission = true
            sendProjectionTokenToService(result.resultCode, result.data!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshPermissions()

        // Otomatis mulai layanan dan minta izin tangkapan layar jika overlay sudah diizinkan
        if (hasOverlayPermission) {
            startTilikService()
            if (!hasProjectionPermission) {
                requestScreenCapture()
            }
        }

        setContent {
            TilikTheme {
                Scaffold(
                    containerColor = DarkSlateBackground
                ) { innerPadding ->
                    MainDashboard(
                        modifier = Modifier.padding(innerPadding),
                        hasOverlay = hasOverlayPermission,
                        hasAudio = hasAudioPermission,
                        hasProjection = hasProjectionPermission,
                        isRunning = isServiceRunning,
                        onRequestOverlay = { requestOverlayPermission() },
                        onRequestAudio = { requestAudioPermission() },
                        onRequestProjection = { requestScreenCapture() },
                        onOpenAccessibility = { openAccessibilitySettings() },
                        onOpenAppDetails = { openAppDetailsSettings() },
                        onToggleService = { toggleService() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissions()
    }

    private fun refreshPermissions() {
        hasOverlayPermission = Settings.canDrawOverlays(this)
        hasAudioPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        isServiceRunning = OverlayService.isServiceRunning
        hasProjectionPermission = OverlayService.hasProjectionToken
    }

    private fun startTilikService() {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_START
        }
        try {
            ContextCompat.startForegroundService(this, intent)
            isServiceRunning = true
        } catch (_: Exception) {
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun requestAudioPermission() {
        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun requestScreenCapture() {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        try {
            screenCaptureLauncher.launch(manager.createScreenCaptureIntent())
        } catch (_: Exception) {
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun openAppDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun sendProjectionTokenToService(resultCode: Int, data: Intent) {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_SET_MEDIA_PROJECTION_TOKEN
            putExtra(OverlayService.EXTRA_RESULT_CODE, resultCode)
            putExtra(OverlayService.EXTRA_DATA, data)
        }
        ContextCompat.startForegroundService(this, intent)
        isServiceRunning = true
    }

    private fun toggleService() {
        if (isServiceRunning) {
            val intent = Intent(this, OverlayService::class.java).apply {
                action = OverlayService.ACTION_STOP
            }
            stopService(intent)
            isServiceRunning = false
            hasProjectionPermission = false
        } else {
            if (!hasOverlayPermission) {
                requestOverlayPermission()
                return
            }
            startTilikService()
            requestScreenCapture()
        }
    }
}

@Composable
fun MainDashboard(
    modifier: Modifier = Modifier,
    hasOverlay: Boolean,
    hasAudio: Boolean,
    hasProjection: Boolean,
    isRunning: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestAudio: () -> Unit,
    onRequestProjection: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenAppDetails: () -> Unit,
    onToggleService: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "TILIK",
                color = AccentBlue,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "AI-Powered Floating Stock Fact-Checker",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Verifikasi klaim saham finfluencer langsung di atas Threads & WhatsApp",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkSlateBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STATUS PERIZINAN & SHARE SCREEN",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    PermissionItem(
                        title = "Floating Overlay (Draw over apps)",
                        granted = hasOverlay,
                        onFix = onRequestOverlay
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PermissionItem(
                        title = "Share Screen (MediaProjection)",
                        granted = hasProjection,
                        actionLabel = "Bagikan",
                        onFix = onRequestProjection
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PermissionItem(
                        title = "Aksesibilitas (Auto-Detect Salin)",
                        granted = true,
                        actionLabel = "Atur",
                        onFix = onOpenAccessibility
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PermissionItem(
                        title = "Setelan Dibatasi (Tunggu 10 Detik)",
                        granted = true,
                        actionLabel = "Buka Info",
                        onFix = onOpenAppDetails
                    )
                }
            }
        }

        Column {
            Button(
                onClick = onToggleService,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) VerdictInvalid else AccentBlue
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (isRunning) "Hentikan Layanan Tilik" else "Mulai Layanan Tilik",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Didukung data pasar resmi Indonesia Stock Exchange (IDX) via Sectors API.",
                color = TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    granted: Boolean,
    actionLabel: String = "Izinkan",
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (granted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(VerdictValid.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "AKTIF",
                    color = VerdictValid,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            OutlinedButton(
                onClick = onFix,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
            ) {
                Text(text = actionLabel, fontSize = 11.sp)
            }
        }
    }
}
