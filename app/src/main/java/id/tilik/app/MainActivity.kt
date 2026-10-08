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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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

enum class MainScreenDestination {
    AUTH,
    ROLE_SELECTION,
    MAIN_APP,
    PROFILE_DETAIL,
    NOTIFICATIONS
}

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
    private var authErrorDetails by mutableStateOf<String?>(null)

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
        val resultCode = result.resultCode
        val dataIntent = result.data

        if (resultCode == RESULT_CANCELED && dataIntent == null) {
            authErrorMessage = "Login Google dibatalkan."
            authErrorDetails = "[FE] ResultCode: RESULT_CANCELED (0). Dialog pemilihan akun ditutup oleh pengguna."
            isAuthLoading = false
            return@registerForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(dataIntent)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrBlank()) {
                handleGoogleIdToken(idToken)
            } else {
                authErrorMessage = "Gagal menghubungkan ke Google. Coba lagi."
                authErrorDetails = "[FE] Akun Google terpilih (${account?.email}), namun idToken kosong (null). Pastikan GOOGLE_WEB_CLIENT_ID valid: ${BuildConfig.GOOGLE_WEB_CLIENT_ID}"
                isAuthLoading = false
            }
        } catch (e: Exception) {
            val apiEx = e as? ApiException
            val code = apiEx?.statusCode
            val explanation = when (code) {
                7 -> "NETWORK_ERROR (7): HP tidak dapat menghubungi server Google. Periksa koneksi internet."
                10 -> "DEVELOPER_ERROR (10): SHA-1 atau package name belum terdaftar di Google Cloud Console untuk Web Client ID ini.\nSHA-1 debug: A0:17:1D:9B:6F:2F:50:8D:37:E5:4C:FA:45:D0:87:9D:99:0C:B0:55\nPackage: id.tilik.app"
                12500 -> "SIGN_IN_FAILED (12500): Google Play Services gagal melakukan sign in (mungkin perlu update Play Services atau akun belum terhubung)."
                12501 -> "SIGN_IN_CANCELLED (12501): Pengguna membatalkan dialog pemilihan akun."
                12502 -> "SIGN_IN_CURRENTLY_IN_PROGRESS (12502): Proses sign-in lain sedang berlangsung."
                else -> "${e.javaClass.simpleName}: ${e.message}"
            }
            authErrorMessage = "Gagal menghubungkan ke Google. Coba lagi."
            authErrorDetails = "[FE Google Sign-In SDK Error]\nStatusCode: $code\nPenjelasan: $explanation\nRaw: ${e.localizedMessage ?: e.toString()}"
            timber.log.Timber.e(e, "Google Sign-In Launcher failed: %s", authErrorDetails)
            isAuthLoading = false
        }
    }

    private fun startGoogleSignIn() {
        authErrorMessage = null
        authErrorDetails = null
        isAuthLoading = true
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun handleGoogleIdToken(idToken: String) {
        lifecycleScope.launch {
            val res = AuthRepository.loginWithGoogle(idToken)
            isAuthLoading = false
            if (res.isSuccess) {
                authErrorMessage = null
                authErrorDetails = null
            } else {
                val err = res.exceptionOrNull()
                authErrorMessage = "Gagal menghubungkan ke server Tilik AI. Coba lagi."
                authErrorDetails = "[BE API Error - /api/v1/auth/google]\nBase URL: ${BuildConfig.BASE_URL}\nDetail: ${err?.message}\nException: ${err?.javaClass?.simpleName}"
                timber.log.Timber.e(err, "Backend Google Auth error: %s", authErrorDetails)
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
        handleClaimIntent(intent)

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

                        val currentDestination = when {
                            !isLoggedIn -> MainScreenDestination.AUTH
                            !isRoleOnboardingDone -> MainScreenDestination.ROLE_SELECTION
                            isProfileDetailVisible -> MainScreenDestination.PROFILE_DETAIL
                            isNotificationsScreenVisible -> MainScreenDestination.NOTIFICATIONS
                            else -> MainScreenDestination.MAIN_APP
                        }

                        // Android system back button & gesture handling
                        BackHandler(enabled = isProfileDetailVisible) {
                            isProfileDetailVisible = false
                        }
                        BackHandler(enabled = !isProfileDetailVisible && isNotificationsScreenVisible) {
                            isNotificationsScreenVisible = false
                        }
                        BackHandler(
                            enabled = !isProfileDetailVisible && !isNotificationsScreenVisible &&
                                    currentTab == AppTab.MARKETS && selectedHistoryId != null
                        ) {
                            selectedHistoryId = null
                        }
                        BackHandler(
                            enabled = !isProfileDetailVisible && !isNotificationsScreenVisible &&
                                    currentTab != AppTab.HOME && selectedHistoryId == null
                        ) {
                            currentTab = AppTab.HOME
                        }

                        AnimatedContent(
                            targetState = currentDestination,
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = {
                                when {
                                    // Pushing sub-screen (ProfileDetail or Notifications) over MainApp
                                    (initialState == MainScreenDestination.MAIN_APP &&
                                            (targetState == MainScreenDestination.PROFILE_DETAIL || targetState == MainScreenDestination.NOTIFICATIONS)) -> {
                                        (slideInHorizontally(
                                            animationSpec = tween(320, easing = FastOutSlowInEasing)
                                        ) { fullWidth -> fullWidth } + fadeIn(
                                            animationSpec = tween(280)
                                        )).togetherWith(
                                            slideOutHorizontally(
                                                animationSpec = tween(320, easing = FastOutSlowInEasing)
                                            ) { fullWidth -> -(fullWidth * 0.25f).toInt() } + fadeOut(
                                                animationSpec = tween(220)
                                            )
                                        )
                                    }
                                    // Popping back from sub-screen to MainApp
                                    ((initialState == MainScreenDestination.PROFILE_DETAIL || initialState == MainScreenDestination.NOTIFICATIONS) &&
                                            targetState == MainScreenDestination.MAIN_APP) -> {
                                        (slideInHorizontally(
                                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                                        ) { fullWidth -> -(fullWidth * 0.25f).toInt() } + fadeIn(
                                            animationSpec = tween(280)
                                        )).togetherWith(
                                            slideOutHorizontally(
                                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                                            ) { fullWidth -> fullWidth } + fadeOut(
                                                animationSpec = tween(220)
                                            )
                                        )
                                    }
                                    // Forward onboarding: Auth -> Role Selection
                                    (initialState == MainScreenDestination.AUTH && targetState == MainScreenDestination.ROLE_SELECTION) -> {
                                        (slideInHorizontally(
                                            animationSpec = tween(320, easing = FastOutSlowInEasing)
                                        ) { fullWidth -> (fullWidth * 0.35f).toInt() } + fadeIn(
                                            animationSpec = tween(280)
                                        )).togetherWith(
                                            slideOutHorizontally(
                                                animationSpec = tween(320, easing = FastOutSlowInEasing)
                                            ) { fullWidth -> -(fullWidth * 0.35f).toInt() } + fadeOut(
                                                animationSpec = tween(220)
                                            )
                                        )
                                    }
                                    // Completed onboarding: Role Selection -> Main App
                                    (initialState == MainScreenDestination.ROLE_SELECTION && targetState == MainScreenDestination.MAIN_APP) -> {
                                        (slideInHorizontally(
                                            animationSpec = tween(320, easing = FastOutSlowInEasing)
                                        ) { fullWidth -> (fullWidth * 0.25f).toInt() } + fadeIn(
                                            animationSpec = tween(280)
                                        )).togetherWith(
                                            slideOutHorizontally(
                                                animationSpec = tween(320, easing = FastOutSlowInEasing)
                                            ) { fullWidth -> -(fullWidth * 0.25f).toInt() } + fadeOut(
                                                animationSpec = tween(220)
                                            )
                                        )
                                    }
                                    // Fallback (e.g. Logout to Auth, etc.)
                                    else -> {
                                        fadeIn(animationSpec = tween(260)).togetherWith(
                                            fadeOut(animationSpec = tween(200))
                                        )
                                    }
                                }
                            },
                            label = "RootDestinationTransition"
                        ) { destination ->
                            when (destination) {
                                MainScreenDestination.AUTH -> {
                                    AuthScreen(
                                        isLoading = isAuthLoading,
                                        errorMessage = authErrorMessage,
                                        errorDetails = authErrorDetails,
                                        onGoogleSignInClick = { startGoogleSignIn() }
                                    )
                                }
                                MainScreenDestination.ROLE_SELECTION -> {
                                    RoleSelectionScreen(
                                        initialRole = SessionManager.getUserRole(),
                                        onRoleConfirmed = {
                                            // SessionManager.updateRole() updates isRoleOnboardingDoneState to true
                                        }
                                    )
                                }
                                MainScreenDestination.PROFILE_DETAIL -> {
                                    ProfileDetailScreen(
                                        onBackClick = { isProfileDetailVisible = false },
                                        onLogoutClick = {
                                            isProfileDetailVisible = false
                                            googleSignInClient.signOut()
                                            SessionManager.clearSession()
                                        }
                                    )
                                }
                                MainScreenDestination.NOTIFICATIONS -> {
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
                                            },
                                            onScanNowClick = {
                                                isNotificationsScreenVisible = false
                                                currentTab = AppTab.HOME
                                            }
                                        )
                                    }
                                }
                                MainScreenDestination.MAIN_APP -> {
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
                                            AnimatedContent(
                                                targetState = currentTab,
                                                modifier = Modifier.fillMaxSize(),
                                                transitionSpec = {
                                                    val isForward = targetState.ordinal > initialState.ordinal
                                                    val enterOffset = if (isForward) 0.18f else -0.18f
                                                    val exitOffset = if (isForward) -0.18f else 0.18f

                                                    (slideInHorizontally(
                                                        animationSpec = tween(260, easing = FastOutSlowInEasing)
                                                    ) { fullWidth -> (fullWidth * enterOffset).toInt() } + fadeIn(
                                                        animationSpec = tween(260)
                                                    )).togetherWith(
                                                        slideOutHorizontally(
                                                            animationSpec = tween(260, easing = FastOutSlowInEasing)
                                                        ) { fullWidth -> (fullWidth * exitOffset).toInt() } + fadeOut(
                                                            animationSpec = tween(180)
                                                        )
                                                    )
                                                },
                                                label = "TabTransition"
                                            ) { targetTab ->
                                                when (targetTab) {
                                                    AppTab.HOME -> {
                                                        DashboardScreen(
                                                            isRunning = isServiceRunning,
                                                            hasOverlay = hasOverlayPermission,
                                                            hasProjection = hasProjectionPermission,
                                                            onToggleService = { toggleService() },
                                                            onRequestOverlay = { requestOverlayPermission() },
                                                            onRequestProjection = { requestScreenCapture() },
                                                            onOpenAccessibility = { openAccessibilitySettings() },
                                                            onOpenAppDetails = { openAppDetailsSettings() },
                                                            onNavigateToHistory = {
                                                                currentTab = AppTab.HISTORY
                                                            },
                                                            onHistoryItemClick = { histId, ticker ->
                                                                selectedHistoryId = histId
                                                                if (ticker != null) selectedMarketStock = ticker
                                                                currentTab = AppTab.MARKETS
                                                            },
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
                                                            },
                                                            onScanNowClick = {
                                                                currentTab = AppTab.HOME
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleClaimIntent(intent)
    }

    private fun handleClaimIntent(intent: Intent?) {
        val claim = intent?.getStringExtra(OverlayService.EXTRA_CLAIM_TEXT)
        if (!claim.isNullOrBlank()) {
            refreshPermissions()
            if (hasOverlayPermission) {
                val serviceIntent = Intent(this, OverlayService::class.java).apply {
                    action = OverlayService.ACTION_PROCESS_TEXT_CLAIM
                    putExtra(OverlayService.EXTRA_CLAIM_TEXT, claim)
                }
                ContextCompat.startForegroundService(this, serviceIntent)
            }
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
