package id.tilik.app.ui.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import android.widget.FrameLayout
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

    private var rootView: FrameLayout? = null
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
    private var edgeAnimator: ValueAnimator? = null

    private val overlayPrefs by lazy {
        context.getSharedPreferences("tilik_overlay_prefs", Context.MODE_PRIVATE)
    }

    private var lastBubbleX = 0
    private var lastBubbleY = 450

    private fun getScreenDimensions(): Pair<Int, Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            Pair(bounds.width(), bounds.height())
        } else {
            val metrics = context.resources.displayMetrics
            Pair(metrics.widthPixels, metrics.heightPixels)
        }
    }

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
        if (rootView != null || composeView != null) return

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

        val (screenWidth, screenHeight) = getScreenDimensions()
        val metrics = context.resources.displayMetrics
        val defaultBubbleW = (56 * metrics.density).toInt()
        val defaultY = (screenHeight * 0.42f).toInt()
        val defaultX = screenWidth - defaultBubbleW

        isDockedOnLeft = overlayPrefs.getBoolean("pref_is_docked_left", false)
        lastBubbleX = if (isDockedOnLeft) 0 else (screenWidth - defaultBubbleW)
        lastBubbleY = overlayPrefs.getInt("pref_bubble_y", defaultY).coerceIn(
            (36 * metrics.density).toInt().coerceAtLeast(48),
            screenHeight - (56 * metrics.density).toInt() - (52 * metrics.density).toInt()
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = lastBubbleX
            y = lastBubbleY
        }
        layoutParams = params

        val container = object : FrameLayout(context) {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isBubbleDragging = false
            private var touchStartTime = 0L

            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                if (currentState != OverlayState.IDLE) {
                    if (ev.action == MotionEvent.ACTION_OUTSIDE) {
                        if (currentState == OverlayState.INPUT || currentState == OverlayState.SUCCESS) {
                            Timber.tag("TILIK_MONITOR").i("👆 [ACTION_OUTSIDE] Sentuhan di luar floating app -> Tutup sheet")
                            onCloseClicked()
                            return true
                        }
                    }
                    return super.dispatchTouchEvent(ev)
                }

                val wmParams = this@OverlayWindowManager.layoutParams ?: return super.dispatchTouchEvent(ev)
                val (screenW, screenH) = getScreenDimensions()
                val m = resources.displayMetrics
                val defaultW = (56 * m.density).toInt()
                val bubbleW = if (width in 1 until screenW) width else defaultW
                val bubbleH = if (height in 1 until screenH) height else defaultW
                val marginX = 0
                val marginTop = (36 * m.density).toInt().coerceAtLeast(48)
                val marginBottom = (52 * m.density).toInt().coerceAtLeast(64)

                when (ev.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        edgeAnimator?.cancel()
                        cancelIdleTimer()
                        untuckBubble(animate = false)
                        initialX = wmParams.x
                        initialY = wmParams.y
                        initialTouchX = ev.rawX
                        initialTouchY = ev.rawY
                        isBubbleDragging = false
                        isDragging = false
                        touchStartTime = System.currentTimeMillis()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (ev.rawX - initialTouchX).toInt()
                        val dy = (ev.rawY - initialTouchY).toInt()
                        val moveDist = kotlin.math.hypot(dx.toDouble(), dy.toDouble())

                        if (!isBubbleDragging && moveDist > 8) {
                            isBubbleDragging = true
                            isDragging = true
                            edgeAnimator?.cancel()
                            cancelIdleTimer()
                            untuckBubble(animate = false)
                        }

                        if (isBubbleDragging) {
                            val maxX = (screenW - bubbleW - marginX).coerceAtLeast(marginX)
                            val maxY = (screenH - bubbleH - marginBottom).coerceAtLeast(marginTop)
                            val newX = (initialX + dx).coerceIn(marginX, maxX)
                            val newY = (initialY + dy).coerceIn(marginTop, maxY)
                            wmParams.x = newX
                            wmParams.y = newY
                            lastBubbleX = newX
                            lastBubbleY = newY
                            val bubbleCenterX = newX + (bubbleW / 2)
                            isDockedOnLeft = bubbleCenterX < (screenW / 2)
                            try {
                                windowManager.updateViewLayout(this, wmParams)
                            } catch (_: Exception) {}
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val touchDuration = System.currentTimeMillis() - touchStartTime
                        val totalDx = ev.rawX - initialTouchX
                        val totalDy = ev.rawY - initialTouchY
                        val totalDist = kotlin.math.hypot(totalDx.toDouble(), totalDy.toDouble())

                        if (!isBubbleDragging && totalDist < 12 && touchDuration < 300) {
                            cancelIdleTimer()
                            untuckBubble(animate = false)
                            onBubbleClicked()
                        } else {
                            // Tuck/Snap ke samping: < 50% layar ke kiri, >= 50% layar ke kanan
                            val bubbleCenterX = wmParams.x + (bubbleW / 2)
                            isDockedOnLeft = bubbleCenterX < (screenW / 2)
                            val maxX = (screenW - bubbleW - marginX).coerceAtLeast(marginX)
                            val targetX = if (isDockedOnLeft) marginX else maxX

                            val containerView = this
                            edgeAnimator?.cancel()
                            edgeAnimator = ValueAnimator.ofInt(wmParams.x, targetX).apply {
                                duration = 240L
                                interpolator = DecelerateInterpolator(1.6f)
                                addUpdateListener { anim ->
                                    val curX = anim.animatedValue as Int
                                    wmParams.x = curX
                                    lastBubbleX = curX
                                    try {
                                        windowManager.updateViewLayout(containerView, wmParams)
                                    } catch (_: Exception) {}
                                }
                                addListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator) {
                                        try {
                                            overlayPrefs.edit()
                                                .putInt("pref_bubble_x", wmParams.x)
                                                .putInt("pref_bubble_y", wmParams.y)
                                                .putBoolean("pref_is_docked_left", isDockedOnLeft)
                                                .apply()
                                        } catch (_: Exception) {}
                                        resetIdleTimer()
                                    }
                                })
                                start()
                            }
                        }
                        isBubbleDragging = false
                        isDragging = false
                        return true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        val bubbleCenterX = wmParams.x + (bubbleW / 2)
                        isDockedOnLeft = bubbleCenterX < (screenW / 2)
                        val maxX = (screenW - bubbleW - marginX).coerceAtLeast(marginX)
                        val targetX = if (isDockedOnLeft) marginX else maxX
                        wmParams.x = targetX
                        lastBubbleX = targetX
                        try {
                            windowManager.updateViewLayout(this, wmParams)
                        } catch (_: Exception) {}
                        isBubbleDragging = false
                        isDragging = false
                        resetIdleTimer()
                        return true
                    }
                }
                return super.dispatchTouchEvent(ev)
            }
        }
        container.setViewTreeLifecycleOwner(this@OverlayWindowManager)
        container.setViewTreeViewModelStoreOwner(this@OverlayWindowManager)
        container.setViewTreeSavedStateRegistryOwner(this@OverlayWindowManager)
        rootView = container

        composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(this@OverlayWindowManager)
            setViewTreeViewModelStoreOwner(this@OverlayWindowManager)
            setViewTreeSavedStateRegistryOwner(this@OverlayWindowManager)

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
                if (isDragging) {
                    return@addOnLayoutChangeListener
                }
                val newWidth = right - left
                val oldWidth = oldRight - oldLeft

                if (currentState != OverlayState.INPUT && currentState != OverlayState.SUCCESS) {
                    if (newWidth > 0 && newWidth != oldWidth) {
                        val (screenW, _) = getScreenDimensions()
                        val wmParams = this@OverlayWindowManager.layoutParams ?: return@addOnLayoutChangeListener

                        if (isDockedOnLeft) {
                            wmParams.x = 0
                        } else {
                            wmParams.x = (screenW - newWidth).coerceAtLeast(0)
                        }
                        lastBubbleX = wmParams.x
                        try {
                            val root = this@OverlayWindowManager.rootView ?: v
                            windowManager.updateViewLayout(root, wmParams)
                        } catch (_: Exception) {}
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
                                            onCloseClick = { onCloseClicked() },
                                            onReturnToInput = {
                                                updateState(
                                                    state = OverlayState.INPUT,
                                                    claimText = currentClaimText,
                                                    ticker = detectedTicker
                                                )
                                            }
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

        container.addView(composeView)
        windowManager.addView(container, params)
        resetIdleTimer()
    }

    fun updateState(
        state: OverlayState,
        data: VerificationResponse? = null,
        ticker: String? = null,
        claimText: String? = null,
        error: String? = null
    ) {
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
        val view = rootView ?: composeView ?: return

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

                val (screenWidth, _) = getScreenDimensions()
                val metrics = context.resources.displayMetrics

                // Estimasi lebar untuk state aktif agar tidak overflow saat mekar di tepi kanan
                val estimatedW = when {
                    state == OverlayState.ANALYZING -> (56 * metrics.density).toInt()
                    state == OverlayState.ERROR -> (240 * metrics.density).toInt()
                    ticker != null -> (210 * metrics.density).toInt()
                    else -> (56 * metrics.density).toInt()
                }

                if (isDockedOnLeft) {
                    params.x = 0
                } else {
                    params.x = (screenWidth - estimatedW).coerceAtLeast(0)
                }
                lastBubbleX = params.x
                params.y = lastBubbleY
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
        }

        windowManager.updateViewLayout(view, params)
    }

    private fun tuckBubbleIntoEdge() {
        if (currentState != OverlayState.IDLE || isDragging) return
        isTucked = true
        val wmParams = layoutParams ?: return
        val root = rootView ?: return
        val (screenW, _) = getScreenDimensions()
        val m = context.resources.displayMetrics
        val defaultW = (56 * m.density).toInt()
        val bubbleW = if (root.width in 1 until screenW) root.width else defaultW

        if (isDockedOnLeft) {
            wmParams.x = 0
        } else {
            wmParams.x = (screenW - bubbleW).coerceAtLeast(0)
        }
        lastBubbleX = wmParams.x
        try {
            windowManager.updateViewLayout(root, wmParams)
        } catch (_: Exception) {}
    }

    private fun untuckBubble(animate: Boolean = true) {
        idleHandler.removeCallbacks(tuckRunnable)
        isTucked = false
    }

    fun hide() {
        cancelIdleTimer()
        tuckAnimator?.cancel()
        tuckAnimator = null
        try {
            context.unregisterReceiver(homeKeyReceiver)
        } catch (_: Exception) {}
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        rootView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        rootView = null
        composeView = null
        store.clear()
    }
}
