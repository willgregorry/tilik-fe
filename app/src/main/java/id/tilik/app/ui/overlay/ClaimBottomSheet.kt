package id.tilik.app.ui.overlay

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.HapticFeedbackConstants
import android.view.ViewTreeObserver
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipboardManager as ComposeClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import id.tilik.app.R
import id.tilik.app.detection.StockKeywordDetector
import id.tilik.app.ui.theme.AppAccent
import id.tilik.app.ui.theme.AppBg
import id.tilik.app.ui.theme.AppBorder
import id.tilik.app.ui.theme.AppCard
import id.tilik.app.ui.theme.AppCardSubtle
import id.tilik.app.ui.theme.AppGreen
import id.tilik.app.ui.theme.AppGreenBg
import id.tilik.app.ui.theme.AppGreenBorder
import id.tilik.app.ui.theme.AppRed
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

internal data class TextToolbarMenuState(
    val rect: Rect,
    val onCopy: (() -> Unit)?,
    val onPaste: (() -> Unit)?,
    val onCut: (() -> Unit)?,
    val onSelectAll: (() -> Unit)?
)

internal class OverlayTextToolbar(
    val onShow: (
        rect: Rect,
        onCopy: (() -> Unit)?,
        onPaste: (() -> Unit)?,
        onCut: (() -> Unit)?,
        onSelectAll: (() -> Unit)?
    ) -> Unit,
    val onHide: () -> Unit
) : TextToolbar {
    private var _status = TextToolbarStatus.Hidden
    override val status: TextToolbarStatus
        get() = _status

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        _status = TextToolbarStatus.Shown
        onShow(rect, onCopyRequested, onPasteRequested, onCutRequested, onSelectAllRequested)
    }

    override fun hide() {
        _status = TextToolbarStatus.Hidden
        onHide()
    }
}

private fun getClipboardString(context: Context): String? {
    return try {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (cm?.hasPrimaryClip() == true) {
            val clip = cm.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).coerceToText(context)?.toString()?.trim()
            } else null
        } else null
    } catch (_: Exception) {
        null
    }
}

private fun copyToClipboard(context: Context, text: String) {
    try {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Tilik", text)
        cm?.setPrimaryClip(clip)
    } catch (e: Exception) {
        Timber.tag("TILIK_MONITOR").w(e, "Gagal menyalin teks ke clipboard")
    }
}

/**
 * Modal Bottom Sheet ala Grammarly yang muncul langsung di atas Threads atau WhatsApp
 * tanpa membuka full aplikasi (Activity).
 */
@Composable
fun ClaimBottomSheet(
    initialText: String = "",
    onDismiss: () -> Unit,
    onSubmitClaim: (claimText: String, ticker: String?) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    var textFieldValue by remember(initialText) {
        mutableStateOf(TextFieldValue(initialText, TextRange(initialText.length)))
    }
    val textInput = textFieldValue.text
    var isFromClipboard by remember(initialText) { mutableStateOf(initialText.isNotBlank()) }
    var hasUserManuallyEdited by remember { mutableStateOf(false) }

    var menuState by remember { mutableStateOf<TextToolbarMenuState?>(null) }
    var toolbarSize by remember { mutableStateOf(IntSize.Zero) }

    val overlayTextToolbar = remember {
        OverlayTextToolbar(
            onShow = { rect, onCopy, onPaste, onCut, onSelectAll ->
                menuState = TextToolbarMenuState(
                    rect = rect,
                    onCopy = onCopy,
                    onPaste = onPaste,
                    onCut = onCut,
                    onSelectAll = onSelectAll
                )
            },
            onHide = {
                menuState = null
            }
        )
    }

    val customClipboardManager = remember(context) {
        object : ComposeClipboardManager {
            override fun getText(): AnnotatedString? {
                val str = getClipboardString(context)
                return if (!str.isNullOrEmpty()) AnnotatedString(str) else null
            }

            override fun setText(annotatedString: AnnotatedString) {
                copyToClipboard(context, annotatedString.text)
            }

            override fun hasText(): Boolean {
                val str = getClipboardString(context)
                return !str.isNullOrEmpty()
            }
        }
    }

    fun fetchLatestClipboard() {
        // Jangan timpa jika initialText telah diisi secara eksplisit (misal dari ACTION_PROCESS_TEXT)
        if (initialText.isNotBlank() && !hasUserManuallyEdited) {
            return
        }
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (cm?.hasPrimaryClip() == true) {
                val clip = cm.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val clipText = clip.getItemAt(0).coerceToText(context)?.toString()?.trim()
                    if (!clipText.isNullOrBlank()) {
                        // Perbarui jika teks clipboard baru berbeda dari yang sedang tampil
                        if (!hasUserManuallyEdited || textFieldValue.text != clipText || textFieldValue.text.isBlank()) {
                            textFieldValue = TextFieldValue(clipText, TextRange(clipText.length))
                            isFromClipboard = true
                            Timber.tag("TILIK_MONITOR").i("📋 [SHEET AUTO CLIPBOARD] Berhasil menarik teks clipboard terbaru: ${clipText.take(50)}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.tag("TILIK_MONITOR").w(e, "Gagal membaca clipboard di Bottom Sheet")
        }
    }

    // Live clipboard listener saat Bottom Sheet sedang aktif di layar
    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
            Timber.tag("TILIK_MONITOR").i("📋 [LIVE CLIPBOARD EVENT] Deteksi klip baru di Bottom Sheet")
            fetchLatestClipboard()
        }
        cm?.addPrimaryClipChangedListener(clipListener)
        onDispose {
            cm?.removePrimaryClipChangedListener(clipListener)
        }
    }

    // Auto-fetch polling sequence saat Sheet pertama kali muncul untuk menangkap asynchronous clipboard write dari Threads/WhatsApp
    LaunchedEffect(Unit) {
        val delays = listOf(0L, 80L, 200L, 450L, 800L, 1400L)
        for (d in delays) {
            if (d > 0) delay(d)
            fetchLatestClipboard()
        }
    }

    // Auto-fetch setiap kali window overlay memperoleh window focus dari OS Android
    DisposableEffect(view) {
        view.post { view.requestFocus() }
        val listener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) {
                fetchLatestClipboard()
            }
        }
        view.viewTreeObserver.addOnWindowFocusChangeListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
        }
    }

    LaunchedEffect(initialText) {
        if (initialText.isNotBlank()) {
            textFieldValue = TextFieldValue(initialText, TextRange(initialText.length))
            isFromClipboard = true
            hasUserManuallyEdited = false
        } else {
            fetchLatestClipboard()
        }
    }

    val detectedTicker = remember(textInput) {
        StockKeywordDetector.extractPotentialTicker(textInput)
    }

    var isSheetVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // State untuk mode perluas (taller height) saat ada teks panjang atau di-drag ke atas
    var isExpanded by remember { mutableStateOf(false) }

    // Auto-expand jika teks yang dimasukkan panjang (>= 4 baris atau >= 200 karakter)
    LaunchedEffect(textInput) {
        if (textInput.lines().size >= 4 || textInput.length >= 200) {
            if (!isExpanded) {
                Timber.tag("TILIK_MONITOR").d("📜 [AUTO EXPAND] Teks panjang terdeteksi (${textInput.length} karakter, ${textInput.lines().size} baris) -> Memperluas sheet lebih tinggi")
                isExpanded = true
            }
        }
    }

    // Memicu animasi slide-up ease in-out begitu Composable tampil
    LaunchedEffect(Unit) {
        isSheetVisible = true
    }

    val animOffsetY = remember { Animatable(0f) }
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 90.dp.toPx() }
    val expandThresholdPx = with(density) { 45.dp.toPx() }
    val collapseThresholdPx = with(density) { 60.dp.toPx() }
    var lastDragVelocity by remember { mutableStateOf(0f) }
    var lastDragTime by remember { mutableStateOf(0L) }

    fun onDragDelta(dragAmount: Float) {
        val now = System.currentTimeMillis()
        val dt = (now - lastDragTime).coerceAtLeast(1)
        lastDragVelocity = (dragAmount / dt) * 1000f
        lastDragTime = now
        coroutineScope.launch {
            // Izinkan drag ke atas (negatif) agar pengguna merasakan feedback tarikan ke atas
            val minOffset = if (isExpanded) -50f else -130f
            animOffsetY.snapTo((animOffsetY.value + dragAmount).coerceIn(minOffset, 1500f))
        }
    }

    fun onDragFinished() {
        val offset = animOffsetY.value
        val vel = lastDragVelocity
        if (!isExpanded) {
            // Mode Normal: Drag UP -> Perluas sheet (Expanded)
            if (offset < -expandThresholdPx || vel < -500f) {
                Timber.tag("TILIK_MONITOR").i("🔼 [DRAG UP EXPAND] Sheet ditarik ke atas (offset=$offset, vel=$vel) -> Mode Lebih Tinggi (Expanded)")
                isExpanded = true
                coroutineScope.launch {
                    animOffsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
            } else if (offset > dismissThresholdPx || vel > 700f) {
                // Drag DOWN -> Tutup sheet (Dismiss)
                coroutineScope.launch {
                    Timber.tag("TILIK_MONITOR").i("👇 [DRAG DOWN DISMISS] Sheet ditarik ke bawah (offset=$offset, vel=$vel) -> Menutup sheet")
                    animOffsetY.animateTo(
                        targetValue = 2000f,
                        animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                    )
                    onDismiss()
                }
            } else {
                coroutineScope.launch {
                    animOffsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
            }
        } else {
            // Mode Expanded:
            // Tarik ke bawah sangat jauh atau flick keras ke bawah -> Dismiss langsung
            if (offset > dismissThresholdPx * 1.8f || vel > 1200f) {
                coroutineScope.launch {
                    Timber.tag("TILIK_MONITOR").i("👇 [DRAG DOWN DISMISS] Sheet expanded ditarik jauh ke bawah (offset=$offset, vel=$vel) -> Menutup sheet")
                    animOffsetY.animateTo(
                        targetValue = 2000f,
                        animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                    )
                    onDismiss()
                }
            } else if (offset > collapseThresholdPx || vel > 500f) {
                // Tarik ke bawah sedang -> Kembalikan ke mode standar
                Timber.tag("TILIK_MONITOR").i("🔽 [DRAG DOWN COLLAPSE] Sheet ditarik ke bawah (offset=$offset, vel=$vel) -> Kembali ke ukuran standar")
                isExpanded = false
                coroutineScope.launch {
                    animOffsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
            } else {
                coroutineScope.launch {
                    animOffsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
            }
        }
    }

    fun onDragCanceled() {
        coroutineScope.launch {
            animOffsetY.animateTo(0f)
        }
    }

    fun animateAndDismiss() {
        if (!isSheetVisible) return
        isSheetVisible = false
        coroutineScope.launch {
            delay(280L)
            onDismiss()
        }
    }

    val scrimAlpha by animateFloatAsState(
        targetValue = if (isSheetVisible) 0.55f else 0.0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "scrimFade"
    )

    // Offset visual saat ditarik: izinkan sedikit peregangan ke atas (-60px)
    val currentDragOffset = animOffsetY.value.coerceAtLeast(-60f)
    val dynamicScrimAlpha = (scrimAlpha * (1f - (currentDragOffset.coerceAtLeast(0f) / 600f).coerceIn(0f, 1f)))

    // Root Container yang memenuhi layar untuk modal blocking dengan Custom TextToolbar & Clipboard
    CompositionLocalProvider(
        LocalTextToolbar provides overlayTextToolbar,
        LocalClipboardManager provides customClipboardManager
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
        // Scrim semi-transparan (Tap outside to minimize/dismiss dengan animasi halus)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dynamicScrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    Timber.tag("TILIK_MONITOR").d("👆 [TAP OUTSIDE SCRIM] Me-minimize sheet ke bubble dengan animasi")
                    animateAndDismiss()
                }
        )

        // Bottom Sheet Surface ala Grammarly dengan animasi Ease In-Out dari bawah ke atas
        AnimatedVisibility(
            visible = isSheetVisible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    durationMillis = 360,
                    easing = CubicBezierEasing(0.18f, 0.9f, 0.2f, 1.0f)
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 280,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    durationMillis = 260,
                    easing = FastOutLinearInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearEasing
                )
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    .offset { IntOffset(0, currentDragOffset.roundToInt()) }
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(AppBg)
                    .border(
                        width = 1.dp,
                        color = AppBorder,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Mencegah klik di dalam sheet tembus ke scrim
                    }
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                // Header & Drag Handle Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(isExpanded) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    lastDragVelocity = 0f
                                    lastDragTime = System.currentTimeMillis()
                                },
                                onDragEnd = { onDragFinished() },
                                onDragCancel = { onDragCanceled() },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    onDragDelta(dragAmount)
                                }
                            )
                        }
                ) {
                    // Drag Handle Bar (Clean Neutral, tap to toggle expand)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp, bottom = 8.dp)
                            .width(if (isExpanded) 54.dp else 42.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(if (isExpanded) AppAccent else Color(0x33FFFFFF))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                isExpanded = !isExpanded
                                Timber.tag("TILIK_MONITOR").d("📐 [HANDLE CLICK TOGGLE] isExpanded=$isExpanded")
                            }
                    )

                    // Header Section: Logo + Close Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Tilik Logo + Name Asset (Klik untuk membuka aplikasi utama Tilik)
                        Image(
                            painter = painterResource(id = R.drawable.logo_name),
                            contentDescription = "Buka Aplikasi Tilik",
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    try {
                                        val intent = Intent(context, id.tilik.app.MainActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        }
                                        context.startActivity(intent)
                                        animateAndDismiss()
                                    } catch (e: Exception) {
                                        Timber.tag("TILIK_MONITOR").e(e, "Gagal membuka aplikasi Tilik dari logo bottom sheet")
                                    }
                                },
                            contentScale = ContentScale.Fit
                        )

                        // Right: Close Icon
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(AppCardSubtle)
                                .border(1.dp, AppBorder, CircleShape)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    animateAndDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Tutup",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Divider Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppBorder)
                )

                // Content Area: Active Claim Editor Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppCard),
                        border = BorderStroke(1.dp, AppBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header Row: Klaim Diskusi badge on left, Action icon buttons on right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: Pill Badge for Claim / Detected Ticker
                                if (detectedTicker != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(percent = 50))
                                            .background(AppGreenBg)
                                            .border(1.dp, AppGreenBorder, RoundedCornerShape(percent = 50))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                                contentDescription = null,
                                                tint = AppGreen,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "$$detectedTicker",
                                                color = AppGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(percent = 50))
                                            .background(AppCardSubtle)
                                            .border(1.dp, AppBorder, RoundedCornerShape(percent = 50))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Forum,
                                                contentDescription = null,
                                                tint = AppAccent,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "Klaim Diskusi",
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                // Right: Modern Icon Actions (Segarkan, Perluas/Perkecil, Hapus)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 1. Segarkan (Refresh Clipboard)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppCardSubtle)
                                            .border(1.dp, AppBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                hasUserManuallyEdited = false
                                                fetchLatestClipboard()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = "Segarkan",
                                            tint = if (isFromClipboard) AppAccent else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // 2. Perluas / Perkecil (Toggle Sheet Height)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isExpanded) AppAccent.copy(alpha = 0.15f) else AppCardSubtle)
                                            .border(
                                                1.dp,
                                                if (isExpanded) AppAccent.copy(alpha = 0.4f) else AppBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                isExpanded = !isExpanded
                                                Timber.tag("TILIK_MONITOR").d("📐 [TOGGLE EXPAND] Sheet di-toggle -> isExpanded=$isExpanded")
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Rounded.CloseFullscreen else Icons.Rounded.OpenInFull,
                                            contentDescription = if (isExpanded) "Perkecil" else "Perluas",
                                            tint = if (isExpanded) AppAccent else TextSecondary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    // 3. Hapus (Clear text input)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppCardSubtle)
                                            .border(1.dp, AppBorder, RoundedCornerShape(8.dp))
                                            .clickable(enabled = textInput.isNotEmpty()) {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                textFieldValue = TextFieldValue("", TextRange.Zero)
                                                isFromClipboard = false
                                                hasUserManuallyEdited = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Hapus",
                                            tint = if (textInput.isNotEmpty()) AppRed.copy(alpha = 0.85f) else TextSecondary.copy(alpha = 0.35f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Claim Text Field
                            OutlinedTextField(
                                value = textFieldValue,
                                onValueChange = { newValue ->
                                    textFieldValue = newValue
                                    hasUserManuallyEdited = true
                                    if (newValue.text.isBlank()) {
                                        isFromClipboard = false
                                        hasUserManuallyEdited = false
                                    }
                                },
                                placeholder = {
                                    Text(
                                        text = "Ketik atau salin klaim saham dari Threads/X di sini...",
                                        color = TextSecondary.copy(alpha = 0.85f),
                                        fontSize = 13.sp
                                    )
                                },
                                minLines = if (isExpanded) 7 else 3,
                                maxLines = if (isExpanded) 14 else 6,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = AppCardSubtle,
                                    unfocusedContainerColor = AppCardSubtle,
                                    focusedBorderColor = AppAccent,
                                    unfocusedBorderColor = AppBorder,
                                    cursorColor = AppAccent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary CTA Button
                    Button(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                onSubmitClaim(textInput.trim(), detectedTicker)
                            }
                        },
                        enabled = textInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppAccent,
                            contentColor = Color.White,
                            disabledContainerColor = AppCardSubtle,
                            disabledContentColor = Color(0xFF555555)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Check",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Periksa Fakta di Sectors API",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hint dismiss
                    Text(
                        text = "Ketuk area di luar untuk menutup",
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { animateAndDismiss() }
                    )
                }
            }
        }

        // Floating Text Selection Toolbar ala native Android (muncul saat user tahan/seleksi teks)
        if (menuState != null) {
                val currentMenu = menuState!!
                // Dismiss backdrop jika tap di luar floating toolbar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            overlayTextToolbar.hide()
                        }
                )

                val density = LocalDensity.current
                val screenWidthPx = with(density) { maxWidth.toPx() }
                val screenHeightPx = with(density) { maxHeight.toPx() }

                val actualW = if (toolbarSize.width > 0) toolbarSize.width.toFloat() else with(density) { 240.dp.toPx() }
                val actualH = if (toolbarSize.height > 0) toolbarSize.height.toFloat() else with(density) { 46.dp.toPx() }

                val rect = currentMenu.rect
                val spaceAbove = rect.top
                val targetY = if (spaceAbove > actualH + 16f) {
                    (rect.top - actualH - 12f).coerceAtLeast(16f)
                } else {
                    (rect.bottom + 12f).coerceAtMost(screenHeightPx - actualH - 16f)
                }

                val centerX = if (rect.width > 0) (rect.left + rect.right) / 2f else rect.left
                val targetX = (centerX - actualW / 2f).coerceIn(16f, (screenWidthPx - actualW - 16f).coerceAtLeast(16f))

                Box(
                    modifier = Modifier
                        .offset { IntOffset(targetX.roundToInt(), targetY.roundToInt()) }
                        .onGloballyPositioned { coordinates ->
                            toolbarSize = coordinates.size
                        }
                ) {
                    TextSelectionFloatingToolbar(
                        menuState = currentMenu,
                        textFieldValue = textFieldValue,
                        hasClipboard = !getClipboardString(context).isNullOrEmpty(),
                        onSelectAllClick = {
                            currentMenu.onSelectAll?.invoke()
                            textFieldValue = textFieldValue.copy(selection = TextRange(0, textFieldValue.text.length))
                        },
                        onCutClick = {
                            val sel = textFieldValue.selection
                            if (currentMenu.onCut != null) {
                                currentMenu.onCut.invoke()
                            } else if (sel.length > 0) {
                                val textToCut = textFieldValue.text.substring(sel.min, sel.max)
                                copyToClipboard(context, textToCut)
                                val newText = textFieldValue.text.removeRange(sel.min, sel.max)
                                textFieldValue = TextFieldValue(newText, selection = TextRange(sel.min))
                                hasUserManuallyEdited = true
                            }
                            overlayTextToolbar.hide()
                        },
                        onCopyClick = {
                            val sel = textFieldValue.selection
                            if (currentMenu.onCopy != null) {
                                currentMenu.onCopy.invoke()
                            } else if (sel.length > 0) {
                                val textToCopy = textFieldValue.text.substring(sel.min, sel.max)
                                copyToClipboard(context, textToCopy)
                            }
                            overlayTextToolbar.hide()
                        },
                        onPasteClick = {
                            val clipString = getClipboardString(context)
                            if (currentMenu.onPaste != null) {
                                currentMenu.onPaste.invoke()
                            } else if (!clipString.isNullOrEmpty()) {
                                val sel = textFieldValue.selection
                                val start = sel.min.coerceIn(0, textFieldValue.text.length)
                                val end = sel.max.coerceIn(0, textFieldValue.text.length)
                                val newText = textFieldValue.text.replaceRange(start, end, clipString)
                                val newCursor = start + clipString.length
                                textFieldValue = TextFieldValue(newText, selection = TextRange(newCursor))
                                hasUserManuallyEdited = true
                            }
                            overlayTextToolbar.hide()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TextSelectionFloatingToolbar(
    menuState: TextToolbarMenuState,
    textFieldValue: TextFieldValue,
    hasClipboard: Boolean,
    onSelectAllClick: () -> Unit,
    onCutClick: () -> Unit,
    onCopyClick: () -> Unit,
    onPasteClick: () -> Unit
) {
    val canSelectAll = menuState.onSelectAll != null ||
            (textFieldValue.text.isNotEmpty() && textFieldValue.selection.length < textFieldValue.text.length)
    val canCut = menuState.onCut != null || textFieldValue.selection.length > 0
    val canCopy = menuState.onCopy != null || textFieldValue.selection.length > 0
    val canPaste = menuState.onPaste != null || hasClipboard

    if (!canSelectAll && !canCut && !canCopy && !canPaste) return

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF222222),
        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var needDivider = false

            if (canCut) {
                ToolbarActionItem(
                    icon = Icons.Rounded.ContentCut,
                    label = "Potong",
                    onClick = onCutClick
                )
                needDivider = true
            }

            if (canCopy) {
                if (needDivider) {
                    ToolbarDivider()
                }
                ToolbarActionItem(
                    icon = Icons.Rounded.ContentCopy,
                    label = "Salin",
                    onClick = onCopyClick
                )
                needDivider = true
            }

            if (canPaste) {
                if (needDivider) {
                    ToolbarDivider()
                }
                ToolbarActionItem(
                    icon = Icons.Rounded.ContentPaste,
                    label = "Tempel",
                    onClick = onPasteClick
                )
                needDivider = true
            }

            if (canSelectAll) {
                if (needDivider) {
                    ToolbarDivider()
                }
                ToolbarActionItem(
                    icon = Icons.Rounded.SelectAll,
                    label = "Pilih Semua",
                    onClick = onSelectAllClick
                )
            }
        }
    }
}

@Composable
private fun ToolbarDivider() {
    Box(
        modifier = Modifier
            .height(18.dp)
            .width(1.dp)
            .background(Color(0x22FFFFFF))
    )
}

@Composable
private fun ToolbarActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

