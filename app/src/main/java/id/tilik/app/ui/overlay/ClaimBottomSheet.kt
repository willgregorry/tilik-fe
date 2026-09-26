package id.tilik.app.ui.overlay

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import id.tilik.app.detection.StockKeywordDetector
import id.tilik.app.ui.theme.AccentBlue
import id.tilik.app.ui.theme.DarkSlateBackground
import id.tilik.app.ui.theme.DarkSlateBorder
import id.tilik.app.ui.theme.DarkSlateSurface
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary
import id.tilik.app.ui.theme.VerdictValid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

// Grammarly-inspired color palette
private val GrammarlyTeal = Color(0xFF00C49F)
private val GrammarlyTealDark = Color(0xFF0A221C)
private val GrammarlySurfaceDark = Color(0xFF181A1D)
private val GrammarlyCardBg = Color(0xFF202227)
private val GrammarlyBorder = Color(0xFF2B2E35)
private val GrammarlySubtext = Color(0xFF9EACB9)

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
    var textInput by remember(initialText) { mutableStateOf(initialText) }
    var isFromClipboard by remember(initialText) { mutableStateOf(initialText.isNotBlank()) }
    var hasUserManuallyEdited by remember { mutableStateOf(false) }

    fun fetchLatestClipboard() {
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (cm?.hasPrimaryClip() == true) {
                val clip = cm.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val clipText = clip.getItemAt(0).coerceToText(context)?.toString()?.trim()
                    if (!clipText.isNullOrBlank()) {
                        // Perbarui jika teks clipboard baru berbeda dari yang sedang tampil
                        if (!hasUserManuallyEdited || textInput != clipText || textInput.isBlank()) {
                            textInput = clipText
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
            textInput = initialText
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

    // Root Container yang memenuhi layar untuk modal blocking
    Box(
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
                    .background(GrammarlySurfaceDark)
                    .border(
                        width = 1.dp,
                        color = GrammarlyBorder,
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
            // Header & Drag Handle Area (Bisa di-drag ke atas untuk memperluas / ke bawah untuk menutup modal sheet)
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
                // Drag Handle Bar (Grammarly style, tap to toggle expand)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp, bottom = 8.dp)
                        .width(if (isExpanded) 54.dp else 42.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(if (isExpanded) GrammarlyTeal.copy(alpha = 0.85f) else Color(0xFF4B4F58))
                        .clickable {
                            isExpanded = !isExpanded
                            Timber.tag("TILIK_MONITOR").d("📐 [HANDLE CLICK TOGGLE] isExpanded=$isExpanded")
                        }
                )

            // Header Section (Grammarly Top Bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Tilik Brand Emblem
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = id.tilik.app.R.drawable.favicon),
                        contentDescription = "Tilik Icon",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TILIK AI",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Sectors Stock Fact-Checker",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Right: Sparkle AI & Close Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(GrammarlyTeal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AI Sparkle",
                            tint = GrammarlyTeal,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF26282E))
                            .clickable { animateAndDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Tutup",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
            }

            // Divider Line ala Grammarly
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GrammarlyBorder)
            )

            // Content Area: Empty State ala Grammarly vs Suggestion State
            if (textInput.isBlank()) {
                // ==================== EMPTY STATE (GRAMMARLY EXACT STYLE) ====================
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
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GrammarlyEaselIllustration(
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "No pressure.",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Suggestion will appear here.",
                        color = GrammarlySubtext,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Salin teks atau klaim saham dari Threads / WhatsApp untuk menilik fakta bursa secara instan.",
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Samples
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SampleChip(
                            label = "📈 \$BBRI Akumulasi Asing",
                            modifier = Modifier.weight(1f)
                        ) {
                            textInput = "BBRI asing akumulasi masif ratusan miliar, siap all-time high!"
                            isFromClipboard = true
                        }
                        SampleChip(
                            label = "🚀 \$GOTO Rekor Profit",
                            modifier = Modifier.weight(1f)
                        ) {
                            textInput = "GOTO kuartal ini catat rekor profit dan efisiensi operasional!"
                            isFromClipboard = true
                        }
                    }
                }
            } else {
                // ==================== ACTIVE CLAIM SUGGESTION CARD ====================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GrammarlyCardBg),
                        border = BorderStroke(1.dp, GrammarlyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Card Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (detectedTicker != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GrammarlyTeal.copy(alpha = 0.15f))
                                            .border(1.dp, GrammarlyTeal.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.5.dp)
                                    ) {
                                        Text(
                                            text = "🎯 EMITEN: \$$detectedTicker",
                                            color = GrammarlyTeal,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AccentBlue.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.5.dp)
                                    ) {
                                        Text(
                                            text = "📋 KLAIM TERDETEKSI",
                                            color = AccentBlue,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isExpanded) "Perkecil ⤡" else "Perluas ⤢",
                                        color = if (isExpanded) GrammarlyTeal else Color(0xFF9EA3AE),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                isExpanded = !isExpanded
                                                Timber.tag("TILIK_MONITOR").d("📐 [TOGGLE EXPAND] Sheet di-toggle -> isExpanded=$isExpanded")
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Segarkan",
                                        color = GrammarlyTeal,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                hasUserManuallyEdited = false
                                                fetchLatestClipboard()
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Hapus",
                                        color = TextSecondary,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                textInput = ""
                                                isFromClipboard = false
                                                hasUserManuallyEdited = false
                                            }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Claim Text
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = {
                                    textInput = it
                                    hasUserManuallyEdited = true
                                    if (it.isBlank()) {
                                        isFromClipboard = false
                                        hasUserManuallyEdited = false
                                    }
                                },
                                placeholder = {
                                    Text(
                                        text = "Ketik atau salin klaim saham di sini...",
                                        color = TextSecondary.copy(alpha = 0.5f),
                                        fontSize = 12.5.sp
                                    )
                                },
                                minLines = if (isExpanded) 7 else 2,
                                maxLines = if (isExpanded) 14 else 5,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = Color(0xFF16171A),
                                    unfocusedContainerColor = Color(0xFF16171A),
                                    focusedBorderColor = GrammarlyTeal,
                                    unfocusedBorderColor = GrammarlyBorder
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary CTA Button (Grammarly pill button)
                    Button(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSubmitClaim(textInput.trim(), detectedTicker)
                            }
                        },
                        enabled = textInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrammarlyTeal,
                            contentColor = GrammarlyTealDark
                        ),
                        shape = RoundedCornerShape(24.dp),
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
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Hint dismiss
            Text(
                text = "Ketuk area gelap di atas untuk menutup / meminimize",
                color = TextSecondary.copy(alpha = 0.5f),
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { animateAndDismiss() }
            )
        }
    }
}
}

/**
 * Pixel-perfect vector canvas & easel illustration matching Grammarly's empty state.
 */
@Composable
private fun GrammarlyEaselIllustration(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(width = 130.dp, height = 115.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val easelWood = Color(0xFFECA199)
            val easelWoodDark = Color(0xFFD47C70)

            // Back leg of easel
            drawLine(
                color = easelWoodDark,
                start = Offset(w * 0.5f, h * 0.05f),
                end = Offset(w * 0.5f, h * 0.95f),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Left leg
            drawLine(
                color = easelWood,
                start = Offset(w * 0.48f, h * 0.08f),
                end = Offset(w * 0.22f, h * 0.98f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Right leg
            drawLine(
                color = easelWood,
                start = Offset(w * 0.52f, h * 0.08f),
                end = Offset(w * 0.78f, h * 0.98f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Canvas Board (white rectangle with rounded corners)
            val canvasLeft = w * 0.22f
            val canvasTop = h * 0.15f
            val canvasWidth = w * 0.56f
            val canvasHeight = h * 0.50f

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(canvasLeft, canvasTop),
                size = Size(canvasWidth, canvasHeight),
                cornerRadius = CornerRadius(6.dp.toPx())
            )

            // Subtle border on canvas
            drawRoundRect(
                color = Color(0xFF333333),
                topLeft = Offset(canvasLeft, canvasTop),
                size = Size(canvasWidth, canvasHeight),
                cornerRadius = CornerRadius(6.dp.toPx()),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Soft blue sky shape inside canvas
            drawRoundRect(
                color = Color(0xFFE0F2FE),
                topLeft = Offset(canvasLeft + 3.dp.toPx(), canvasTop + 3.dp.toPx()),
                size = Size(canvasWidth - 6.dp.toPx(), canvasHeight * 0.62f),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // Paintbrush on canvas
            // Blue handle
            drawLine(
                color = Color(0xFF38BDF8),
                start = Offset(canvasLeft + canvasWidth * 0.76f, canvasTop + canvasHeight * 0.14f),
                end = Offset(canvasLeft + canvasWidth * 0.48f, canvasTop + canvasHeight * 0.54f),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Silver ferrule
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(canvasLeft + canvasWidth * 0.48f, canvasTop + canvasHeight * 0.54f),
                end = Offset(canvasLeft + canvasWidth * 0.42f, canvasTop + canvasHeight * 0.63f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Green/Teal dipped brush tip
            drawLine(
                color = Color(0xFF10B981),
                start = Offset(canvasLeft + canvasWidth * 0.42f, canvasTop + canvasHeight * 0.63f),
                end = Offset(canvasLeft + canvasWidth * 0.34f, canvasTop + canvasHeight * 0.75f),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Horizontal easel shelf
            val shelfY = canvasTop + canvasHeight
            drawLine(
                color = easelWood,
                start = Offset(w * 0.16f, shelfY),
                end = Offset(w * 0.84f, shelfY),
                strokeWidth = 7.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Lower horizontal brace
            drawLine(
                color = easelWoodDark,
                start = Offset(w * 0.28f, h * 0.84f),
                end = Offset(w * 0.72f, h * 0.84f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun SampleChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF22242A))
            .border(0.8.dp, GrammarlyBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
