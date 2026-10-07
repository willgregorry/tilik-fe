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
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import id.tilik.app.service.OverlayService
import id.tilik.app.ui.components.AppBottomNav
import id.tilik.app.ui.components.AppTab
import id.tilik.app.ui.components.AppTopBar
import id.tilik.app.ui.dashboard.DashboardScreen
import id.tilik.app.ui.history.HistoryScreen
import id.tilik.app.ui.splash.SplashScreen
import id.tilik.app.ui.theme.AppBackground
import id.tilik.app.ui.theme.TilikTheme

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

    private val prefs by lazy {
        getSharedPreferences("tilik_prefs", Context.MODE_PRIVATE)
    }

    private var isServiceExplicitlyStopped: Boolean
        get() = prefs.getBoolean("service_explicitly_stopped", false)
        set(value) = prefs.edit().putBoolean("service_explicitly_stopped", value).apply()

    private var isSplashActive by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshPermissions()

        if (hasOverlayPermission && !isServiceExplicitlyStopped) {
            startTilikService()
        }

        setContent {
            TilikTheme {
                Crossfade(
                    targetState = isSplashActive,
                    animationSpec = tween(400),
                    label = "SplashCrossfade"
                ) { showingSplash ->
                    if (showingSplash) {
                        SplashScreen(
                            onSplashFinished = {
                                isSplashActive = false
                            }
                        )
                    } else {
                        var currentTab by remember { mutableStateOf(AppTab.HOME) }
                        var isNotificationsScreenVisible by remember { mutableStateOf(false) }

                        if (isNotificationsScreenVisible) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AppBackground)
                            ) {
                                HistoryScreen(
                                    onBackClick = { isNotificationsScreenVisible = false }
                                )
                            }
                        } else {
                            Scaffold(
                                containerColor = AppBackground,
                                topBar = {
                                    AppTopBar(
                                        onProfileClick = {
                                            openAppDetailsSettings()
                                        },
                                        onNotificationClick = {
                                            isNotificationsScreenVisible = true
                                        },
                                        hasUnreadNotification = true
                                    )
                                },
                                bottomBar = {
                                    AppBottomNav(
                                        currentTab = currentTab,
                                        onTabSelected = { selected ->
                                            currentTab = selected
                                        }
                                    )
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    when (currentTab) {
                                        AppTab.HOME -> {
                                            DashboardScreen(
                                                hasOverlay = hasOverlayPermission,
                                                hasAudio = hasAudioPermission,
                                                hasProjection = hasProjectionPermission,
                                                isRunning = isServiceRunning,
                                                onRequestOverlay = { requestOverlayPermission() },
                                                onRequestAudio = { requestAudioPermission() },
                                                onRequestProjection = { requestScreenCapture() },
                                                onOpenAccessibility = { openAccessibilitySettings() },
                                                onOpenAppDetails = { openAppDetailsSettings() },
                                                onToggleService = { toggleService() },
                                                onNavigateToDetails = { currentTab = AppTab.PORTFOLIO }
                                            )
                                        }
                                        AppTab.MARKETS -> {
                                            DashboardScreen(
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
                                        AppTab.PORTFOLIO -> {
                                            HistoryScreen()
                                        }
                                        AppTab.PROFILE -> {
                                            DashboardScreen(
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
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissions()
        if (hasOverlayPermission && !isServiceExplicitlyStopped && !isServiceRunning) {
            startTilikService()
        }
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
            isServiceExplicitlyStopped = true
        } else {
            if (!hasOverlayPermission) {
                requestOverlayPermission()
                return
            }
            isServiceExplicitlyStopped = false
            startTilikService()
        }
    }
}
