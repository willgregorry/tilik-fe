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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import id.tilik.app.data.repository.AuthRepository
import id.tilik.app.data.session.SessionManager
import id.tilik.app.service.OverlayService
import id.tilik.app.ui.auth.AuthScreen
import id.tilik.app.ui.auth.RoleSelectionScreen
import id.tilik.app.ui.components.AppBottomNav
import id.tilik.app.ui.components.AppTab
import id.tilik.app.ui.components.AppTopBar
import id.tilik.app.ui.dashboard.DashboardScreen
import id.tilik.app.ui.history.HistoryScreen
import id.tilik.app.ui.markets.MarketsScreen
import id.tilik.app.ui.settings.ProfileDetailScreen
import id.tilik.app.ui.settings.SettingsScreen
import id.tilik.app.ui.splash.SplashScreen
import id.tilik.app.ui.theme.AppBackground
import id.tilik.app.ui.theme.TilikTheme
import kotlinx.coroutines.launch

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

    private var isAuthLoading by mutableStateOf(false)
    private var authErrorMessage by mutableStateOf<String?>(null)

    private val googleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso)
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrBlank()) {
                handleGoogleIdToken(idToken)
            } else {
                authErrorMessage = "Gagal menghubungkan ke Google. Coba lagi."
                isAuthLoading = false
            }
        } catch (_: Exception) {
            authErrorMessage = "Gagal menghubungkan ke Google. Coba lagi."
            isAuthLoading = false
        }
    }

    private fun startGoogleSignIn() {
        authErrorMessage = null
        isAuthLoading = true
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun handleGoogleIdToken(idToken: String) {
        lifecycleScope.launch {
            val res = AuthRepository.loginWithGoogle(idToken)
            isAuthLoading = false
            if (res.isSuccess) {
                authErrorMessage = null
            } else {
                authErrorMessage = "Gagal menghubungkan ke server Tilik AI. Coba lagi."
            }
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
                        val isLoggedIn by SessionManager.isLoggedInState.collectAsState()
                        val isRoleOnboardingDone by SessionManager.isRoleOnboardingDoneState.collectAsState()
                        val currentUser by SessionManager.currentUserState.collectAsState()

                        var currentTab by remember { mutableStateOf(AppTab.HOME) }
                        var isNotificationsScreenVisible by remember { mutableStateOf(false) }
                        var isProfileDetailVisible by remember { mutableStateOf(false) }
                        var selectedMarketStock by remember { mutableStateOf<String?>(null) }
                        var selectedHistoryId by remember { mutableStateOf<String?>(null) }

                        if (!isLoggedIn) {
                            AuthScreen(
                                isLoading = isAuthLoading,
                                errorMessage = authErrorMessage,
                                onGoogleSignInClick = { startGoogleSignIn() }
                            )
                        } else if (!isRoleOnboardingDone) {
                            RoleSelectionScreen(
                                initialRole = SessionManager.getUserRole(),
                                onRoleConfirmed = {
                                    // SessionManager.updateRole() updates isRoleOnboardingDoneState to true
                                }
                            )
                        } else if (isProfileDetailVisible) {
                            ProfileDetailScreen(
                                onBackClick = { isProfileDetailVisible = false },
                                onLogoutClick = {
                                    isProfileDetailVisible = false
                                    googleSignInClient.signOut()
                                    SessionManager.clearSession()
                                }
                            )
                        } else if (isNotificationsScreenVisible) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AppBackground)
                            ) {
                                HistoryScreen(
                                    onBackClick = { isNotificationsScreenVisible = false },
                                    onItemClick = { histId, ticker ->
                                        selectedHistoryId = histId
                                        if (ticker != null) selectedMarketStock = ticker
                                        isNotificationsScreenVisible = false
                                        currentTab = AppTab.MARKETS
                                    }
                                )
                            }
                        } else {
                            Scaffold(
                                containerColor = AppBackground,
                                topBar = {
                                    AppTopBar(
                                        onProfileClick = {
                                            isProfileDetailVisible = true
                                        },
                                        onNotificationClick = {
                                            isNotificationsScreenVisible = true
                                        },
                                        hasUnreadNotification = false,
                                        userName = currentUser?.name?.takeIf { it.isNotBlank() } ?: "User",
                                        userAvatarUrl = currentUser?.picture
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
                                                isRunning = isServiceRunning,
                                                onToggleService = { toggleService() },
                                                onNavigateToDetails = {
                                                    selectedMarketStock = null
                                                    selectedHistoryId = null
                                                    currentTab = AppTab.MARKETS
                                                },
                                                onNavigateToStock = { ticker ->
                                                    selectedMarketStock = ticker
                                                    selectedHistoryId = null
                                                    currentTab = AppTab.MARKETS
                                                }
                                            )
                                        }
                                        AppTab.MARKETS -> {
                                            MarketsScreen(
                                                historyId = selectedHistoryId,
                                                initialTicker = selectedMarketStock,
                                                onBackClick = {
                                                    selectedHistoryId = null
                                                }
                                            )
                                        }
                                        AppTab.HISTORY -> {
                                            HistoryScreen(
                                                onItemClick = { histId, ticker ->
                                                    selectedHistoryId = histId
                                                    if (ticker != null) selectedMarketStock = ticker
                                                    currentTab = AppTab.MARKETS
                                                }
                                            )
                                        }
                                        AppTab.SETTINGS -> {
                                            SettingsScreen(
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
                                                onOpenProfileDetail = { isProfileDetailVisible = true },
                                                onOpenAuth = {
                                                    googleSignInClient.signOut()
                                                    SessionManager.clearSession()
                                                }
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
