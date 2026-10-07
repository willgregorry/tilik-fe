package id.tilik.app.ui.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import timber.log.Timber
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import id.tilik.app.data.model.VerificationResponse
import id.tilik.app.model.OverlayState
import id.tilik.app.ui.theme.TilikTheme
import kotlin.math.abs

class OverlayWindowManager(
    private val context: Context,
    private val onBubbleClicked: () -> Unit,
    private val onClaimSubmitted: (claimText: String, ticker: String?) -> Unit,
    private val onRetryClicked: () -> Unit,
    private val onCloseClicked: () -> Unit,
    private val onStopServiceClicked: () -> Unit = {}
) : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle = lifecycleRegistry
    override val viewModelStore: ViewModelStore = store
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

    private var composeView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var currentState by mutableStateOf(OverlayState.IDLE)
    private var currentData by mutableStateOf<VerificationResponse?>(null)
    private var detectedTicker by mutableStateOf<String?>(null)
    private var currentClaimText by mutableStateOf<String?>(null)
    private var errorMessage by mutableStateOf<String?>(null)
    private var inputSessionId by mutableStateOf(0L)

    private var isTucked by mutableStateOf(false)
    private var isDockedOnLeft by mutableStateOf(false)
    private var isDragging = false
    private val idleHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val IDLE_TIMEOUT_MS = 3000L
    private var tuckAnimator: ValueAnimator? = null

    private val overlayPrefs by lazy {
        context.getSharedPreferences("tilik_overlay_prefs", Context.MODE_PRIVATE)
    }

    private var lastBubbleX = 0
    private var lastBubbleY = 450

    private val tuckRunnable = Runnable {
        Timber.tag("TILIK_MONITOR").i("⏳ [TUCK TIMER FIRED] Inactivity 3 detik terdeteksi. currentState=$currentState, isTucked=$isTucked, isDragging=$isDragging")
        if (currentState == OverlayState.IDLE && !isTucked && !isDragging) {
            if (detectedTicker != null) {
                detectedTicker = null
            }
            tuckBubbleIntoEdge()
        }
    }

    private fun resetIdleTimer() {
        idleHandler.removeCallbacks(tuckRunnable)
        Timber.tag("TILIK_MONITOR").i("⏱️ [RESET IDLE TIMER] currentState=$currentState, detectedTicker=$detectedTicker, isDragging=$isDragging")
        if (currentState == OverlayState.IDLE && !isDragging) {
            idleHandler.postDelayed(tuckRunnable, IDLE_TIMEOUT_MS)
        }
    }

    private fun cancelIdleTimer() {
        Timber.tag("TILIK_MONITOR").i("🛑 [CANCEL IDLE TIMER]")
        idleHandler.removeCallbacks(tuckRunnable)
    }

    private val homeKeyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra("reason")
                Timber.tag("TILIK_MONITOR").i("🏠 [HOME/SYSTEM GESTURE: $reason] User swipe up / Home -> Menutup modal sheet")
                if (currentState == OverlayState.INPUT || currentState == OverlayState.SUCCESS) {
                    onCloseClicked()
                }
            }
        }
    }

    init {
        savedStateRegistryController.performRestore(Bundle())
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun show() {
        if (composeView != null) return

        try {
            val filter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(homeKeyReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(homeKeyReceiver, filter)
            }
        } catch (e: Exception) {
            Timber.tag("TILIK_MONITOR").w(e, "Gagal mendaftarkan homeKeyReceiver")
        }

        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val defaultBubbleW = (56 * metrics.density).toInt()
        val marginPx = (8 * metrics.density).toInt().coerceAtLeast(16)
        val defaultY = (screenHeight * 0.42f).toInt()

        isDockedOnLeft = overlayPrefs.getBoolean("pref_is_docked_left", false)
        lastBubbleY = overlayPrefs.getInt("pref_bubble_y", defaultY).coerceIn(
            (60 * metrics.density).toInt(),
            screenHeight - (100 * metrics.density).toInt()
        )
        lastBubbleX = if (isDockedOnLeft) {
            marginPx
        } else {
            screenWidth - defaultBubbleW - marginPx
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = lastBubbleX
            y = lastBubbleY
        }
        layoutParams = params

        composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(this@OverlayWindowManager)
            setViewTreeViewModelStoreOwner(this@OverlayWindowManager)
            setViewTreeSavedStateRegistryOwner(this@OverlayWindowManager)
            setupTouchListener(this, params, metrics.widthPixels, metrics.heightPixels)

            isFocusable = true
            isFocusableInTouchMode = true
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    if (currentState == OverlayState.INPUT || currentState == OverlayState.SUCCESS) {
                        Timber.tag("TILIK_MONITOR").i("🔙 [BACK KEY/GESTURE] Menutup modal sheet")
                        onCloseClicked()
                        true
                    } else false
                } else false
            }

            viewTreeObserver.addOnWindowFocusChangeListener { hasFocus ->
                if (!hasFocus && (currentState == OverlayState.INPUT || currentState == OverlayState.SUCCESS)) {
                    postDelayed({
                        if (!hasWindowFocus() && (currentState == OverlayState.INPUT || currentState == OverlayState.SUCCESS)) {
                            Timber.tag("TILIK_MONITOR").i("🏠 [WINDOW FOCUS LOST] User beralih app / swipe up Home -> Menutup modal sheet")
                            onCloseClicked()
                        }
                    }, 220L)
                }
            }

            addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (isDragging || snapAnimator?.isRunning == true || tuckAnimator?.isRunning == true) {
                    return@addOnLayoutChangeListener
                }
                val newWidth = right - left
                val oldWidth = oldRight - oldLeft

                if (currentState != OverlayState.INPUT && currentState != OverlayState.SUCCESS && !isTucked) {
                    if (newWidth > 0 && newWidth != oldWidth) {
                        val screenW = metrics.widthPixels
                        val marginPx = (8 * metrics.density).toInt().coerceAtLeast(16)
                        val wmParams = this@OverlayWindowManager.layoutParams ?: return@addOnLayoutChangeListener

                        // Cegah overflow ke kanan layar: pastikan x + newWidth <= screenW - marginPx
                        val maxX = screenW - newWidth - marginPx
                        if (!isDockedOnLeft) {
                            wmParams.x = maxX.coerceAtLeast(marginPx)
                            lastBubbleX = wmParams.x
                            try {
                                windowManager.updateViewLayout(v, wmParams)
                            } catch (_: Exception) {}
                        } else {
                            if (wmParams.x > maxX) {
                                wmParams.x = maxX.coerceAtLeast(marginPx)
                                lastBubbleX = wmParams.x
                                try {
                                    windowManager.updateViewLayout(v, wmParams)
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }
            }

            setContent {
                TilikTheme {
                    when (currentState) {
                        OverlayState.INPUT -> {
                            androidx.compose.runtime.key(inputSessionId) {
                                ClaimBottomSheet(
                                    initialText = currentClaimText ?: "",
                                    onDismiss = { onCloseClicked() },
                                    onSubmitClaim = { claim, ticker ->
                                        onClaimSubmitted(claim, ticker)
                                    }
                                )
                            }
                        }
                        OverlayState.SUCCESS -> {
                            if (currentData != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .navigationBarsPadding()
                                ) {
                                    // Scrim luar: ketuk untuk menutup/me-minimize hasil vonis
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.55f))
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                onCloseClicked()
                                            }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                    ) {
                                        VerdictCardView(
                                            data = currentData!!,
                                            claimText = currentClaimText,
                                            onCloseClick = { onCloseClicked() }
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            FloatingBubbleView(
                                state = currentState,
                                detectedTicker = detectedTicker,
                                errorMessage = errorMessage,
                                isTucked = isTucked,
                                isDockedOnLeft = isDockedOnLeft,
                                onBubbleClick = {
                                    untuckBubble(animate = false)
                                    onBubbleClicked()
                                },
                                onRetryClick = { onRetryClicked() },
                                onCloseClick = { onCloseClicked() }
                            )
                        }
                    }
                }
            }
        }

        windowManager.addView(composeView, params)
        resetIdleTimer()
    }

    fun updateState(
        state: OverlayState,
        data: VerificationResponse? = null,
        ticker: String? = null,
        claimText: String? = null,
        error: String? = null
    ) {
        snapAnimator?.cancel()
        tuckAnimator?.cancel()

        currentState = state
        currentData = data
        detectedTicker = ticker
        if (state == OverlayState.IDLE) {
            currentClaimText = null
        } else if (state == OverlayState.INPUT) {
            currentClaimText = claimText
            inputSessionId = System.currentTimeMillis()
        } else if (claimText != null) {
            currentClaimText = claimText
        }
        errorMessage = error

        if (state == OverlayState.IDLE) {
            untuckBubble(animate = false)
            resetIdleTimer()
        } else {
            cancelIdleTimer()
            untuckBubble(animate = false)
        }

        val params = layoutParams ?: return
        val view = composeView ?: return

        when (state) {
            OverlayState.INPUT, OverlayState.SUCCESS -> {
                // Tampilan Modal Bottom Sheet: Full-screen agar Scrim menutupi layar & memblokir scroll di belakangnya
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.height = WindowManager.LayoutParams.MATCH_PARENT
                params.gravity = Gravity.FILL
                params.x = 0
                params.y = 0
                params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                @Suppress("DEPRECATION")
                params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
            }
            else -> {
                // Mode Floating Bubble biasa saat idle atau proses berlangsung (ANALYZING, ERROR, dsb)
                params.width = WindowManager.LayoutParams.WRAP_CONTENT
                params.height = WindowManager.LayoutParams.WRAP_CONTENT
                params.gravity = Gravity.TOP or Gravity.START

                val metrics = context.resources.displayMetrics
                val screenWidth = metrics.widthPixels
                val marginPx = (8 * metrics.density).toInt().coerceAtLeast(16)

                // Estimasi lebar untuk state aktif agar tidak overflow saat mekar di tepi kanan
                val estimatedW = when {
                    state == OverlayState.ANALYZING -> (175 * metrics.density).toInt()
                    state == OverlayState.ERROR -> (240 * metrics.density).toInt()
                    ticker != null -> (210 * metrics.density).toInt()
                    else -> (56 * metrics.density).toInt()
                }

                if (!isDockedOnLeft) {
                    val maxX = screenWidth - estimatedW - marginPx
                    params.x = maxX.coerceAtLeast(marginPx)
                    lastBubbleX = params.x
                } else {
                    params.x = marginPx
                    lastBubbleX = params.x
                }

                params.y = lastBubbleY
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            }
        }

        windowManager.updateViewLayout(view, params)
    }

    private fun tuckBubbleIntoEdge() {
        val view = composeView ?: return
        val params = layoutParams ?: return
        if (currentState != OverlayState.IDLE || isTucked || isDragging) return

        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val defaultBubbleW = (56 * metrics.density).toInt()
        val viewW = if (view.width in 1 until screenWidth) view.width else defaultBubbleW
        val tuckDistance = (viewW * 0.45f).toInt()

        val targetX = if (isDockedOnLeft) {
            -tuckDistance
        } else {
            screenWidth - viewW + tuckDistance
        }

        isTucked = true
        tuckAnimator?.cancel()
        tuckAnimator = ValueAnimator.ofInt(params.x, targetX).apply {
            duration = 300L
            interpolator = DecelerateInterpolator(1.5f)
            addUpdateListener { animation ->
                val x = animation.animatedValue as Int
                params.x = x
                try {
                    if (composeView != null && currentState == OverlayState.IDLE && !isDragging) {
                        windowManager.updateViewLayout(view, params)
                    }
                } catch (_: Exception) {}
            }
            start()
        }
        Timber.tag("TILIK_MONITOR").i("💤 [BUBBLE TUCKED] Masuk ke pinggir layar (x=$targetX, isDockedOnLeft=$isDockedOnLeft)")
    }

    private fun untuckBubble(animate: Boolean = true) {
        idleHandler.removeCallbacks(tuckRunnable)
        val wasTucked = isTucked
        isTucked = false

        val view = composeView ?: return
        val params = layoutParams ?: return
        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val defaultBubbleW = (56 * metrics.density).toInt()
        val viewW = if (view.width in 1 until screenWidth) view.width else defaultBubbleW
        val marginPx = (8 * metrics.density).toInt().coerceAtLeast(16)

        val normalX = if (isDockedOnLeft) {
            marginPx
        } else {
            screenWidth - viewW - marginPx
        }

        tuckAnimator?.cancel()
        if (wasTucked && animate) {
            tuckAnimator = ValueAnimator.ofInt(params.x, normalX).apply {
                duration = 200L
                interpolator = DecelerateInterpolator(1.5f)
                addUpdateListener { animation ->
                    val x = animation.animatedValue as Int
                    params.x = x
                    lastBubbleX = x
                    try {
                        if (composeView != null && currentState == OverlayState.IDLE && !isDragging) {
                            windowManager.updateViewLayout(view, params)
                        }
                    } catch (_: Exception) {}
                }
                start()
            }
        } else {
            params.x = normalX
            lastBubbleX = normalX
            try {
                windowManager.updateViewLayout(view, params)
            } catch (_: Exception) {}
        }
        Timber.tag("TILIK_MONITOR").i("☀️ [BUBBLE UNTUCKED] Bangun ke posisi normal (x=$normalX, wasTucked=$wasTucked, animate=$animate)")
    }

    private var snapAnimator: ValueAnimator? = null

    private fun snapBubbleToEdge(view: View, params: WindowManager.LayoutParams) {
        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val defaultBubbleW = (56 * metrics.density).toInt()
        val viewW = if (view.width in 1 until screenWidth) view.width else defaultBubbleW
        val marginPx = (8 * metrics.density).toInt().coerceAtLeast(16)

        // Titik tengah horizontal bubble saat ini
        val bubbleCenterX = params.x + (viewW / 2)
        isDockedOnLeft = (bubbleCenterX < screenWidth / 2)
        val targetX = if (isDockedOnLeft) {
            marginPx
        } else {
            screenWidth - viewW - marginPx
        }

        // Simpan posisi terakhir pengguna ke SharedPreferences agar selalu diingat
        try {
            overlayPrefs.edit()
                .putInt("pref_bubble_y", params.y)
                .putBoolean("pref_is_docked_left", isDockedOnLeft)
                .apply()
        } catch (_: Exception) {}

        isTucked = false
        snapAnimator?.cancel()
        snapAnimator = ValueAnimator.ofInt(params.x, targetX).apply {
            duration = 240L
            interpolator = DecelerateInterpolator(1.6f)
            addUpdateListener { animation ->
                val newX = animation.animatedValue as Int
                params.x = newX
                lastBubbleX = newX
                try {
                    if (composeView != null && currentState == OverlayState.IDLE && !isDragging) {
                        windowManager.updateViewLayout(view, params)
                    }
                } catch (_: Exception) {}
            }
            start()
        }
        resetIdleTimer()
        Timber.tag("TILIK_MONITOR").i("🧲 [SNAP TO EDGE] TargetX=$targetX, isDockedOnLeft=$isDockedOnLeft -> Memulai timer idle 3 detik")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener(
        view: View,
        params: WindowManager.LayoutParams,
        screenWidth: Int,
        screenHeight: Int
    ) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var touchStartTime = 0L

        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                if (currentState == OverlayState.INPUT || currentState == OverlayState.SUCCESS) {
                    Timber.tag("TILIK_MONITOR").i("👆 [ACTION_OUTSIDE] Sentuhan di luar floating app -> Tutup sheet")
                    onCloseClicked()
                    return@setOnTouchListener true
                }
            }

            if (currentState != OverlayState.IDLE) {
                return@setOnTouchListener false
            }

            val metrics = context.resources.displayMetrics
            val screenW = metrics.widthPixels
            val screenH = metrics.heightPixels
            val defaultBubbleW = (56 * metrics.density).toInt()
            val viewW = if (view.width in 1 until screenW) view.width else defaultBubbleW
            val marginPx = (8 * metrics.density).toInt().coerceAtLeast(16)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    cancelIdleTimer()
                    snapAnimator?.cancel()
                    tuckAnimator?.cancel()
                    view.parent?.requestDisallowInterceptTouchEvent(true)

                    // Jika sedang tucked, kembalikan seketika ke pinggir layar agar koordinat x valid sebelum di-drag
                    if (isTucked) {
                        isTucked = false
                        val normalX = if (isDockedOnLeft) marginPx else screenW - viewW - marginPx
                        params.x = normalX
                        lastBubbleX = normalX
                        try {
                            windowManager.updateViewLayout(view, params)
                        } catch (_: Exception) {}
                    }

                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    touchStartTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    val moveDistance = kotlin.math.hypot(deltaX.toDouble(), deltaY.toDouble())

                    // Threshold 8px agar responsif seketika begitu jari mulai bergeser
                    if (!isDragging && moveDistance > 8) {
                        isDragging = true
                        cancelIdleTimer()
                        if (isTucked) {
                            isTucked = false
                        }
                    }

                    if (isDragging) {
                        val safeMarginX = (4 * metrics.density).toInt().coerceAtLeast(8)
                        val safeMarginTop = (40 * metrics.density).toInt().coerceAtLeast(60)
                        val safeMarginBottom = (40 * metrics.density).toInt().coerceAtLeast(60)

                        params.x = (initialX + deltaX).coerceIn(safeMarginX, screenW - viewW - safeMarginX)
                        params.y = (initialY + deltaY).coerceIn(safeMarginTop, screenH - viewW - safeMarginBottom)
                        lastBubbleX = params.x
                        lastBubbleY = params.y
                        try {
                            windowManager.updateViewLayout(view, params)
                        } catch (_: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val touchDuration = System.currentTimeMillis() - touchStartTime
                    val totalDeltaX = event.rawX - initialTouchX
                    val totalDeltaY = event.rawY - initialTouchY
                    val totalDistance = kotlin.math.hypot(totalDeltaX.toDouble(), totalDeltaY.toDouble())

                    // Hanya anggap sebagai tap/klik jika jari tidak bergeser (>14px) dan durasi wajar (<350ms)
                    if (!isDragging && totalDistance < 14 && touchDuration < 350) {
                        cancelIdleTimer()
                        untuckBubble(animate = false)
                        onBubbleClicked()
                    } else if (isDragging) {
                        // Selesai menggeser: Mepet (snap) otomatis ke tepi terdekat (kiri atau kanan)
                        snapBubbleToEdge(view, params)
                    } else {
                        // Disentuh sebentar tanpa digeser lalu dilepas
                        if (isTucked) {
                            untuckBubble(animate = true)
                        }
                        resetIdleTimer()
                    }
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        snapBubbleToEdge(view, params)
                    } else {
                        if (isTucked) {
                            untuckBubble(animate = true)
                        }
                        resetIdleTimer()
                    }
                    isDragging = false
                    true
                }
                else -> false
            }
        }
    }

    fun hide() {
        cancelIdleTimer()
        tuckAnimator?.cancel()
        tuckAnimator = null
        try {
            context.unregisterReceiver(homeKeyReceiver)
        } catch (_: Exception) {}
        snapAnimator?.cancel()
        snapAnimator = null
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        composeView?.let {
            windowManager.removeView(it)
        }
        composeView = null
        store.clear()
    }
}
