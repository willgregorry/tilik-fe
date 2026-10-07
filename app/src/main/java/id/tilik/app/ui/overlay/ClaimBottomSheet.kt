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
import id.tilik.app.ui.theme.BrandPrimary
import id.tilik.app.ui.theme.BrandSecondary
import id.tilik.app.ui.theme.AppCard
import id.tilik.app.ui.theme.DarkSlateBackground
import id.tilik.app.ui.theme.DarkSlateBorder
import id.tilik.app.ui.theme.DarkSlateSurface
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary
import id.tilik.app.ui.theme.VerdictValid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

// Theme Colors matching Dark Investment Design System
private val SheetBackground = Color(0xFF0A0A0A)
private val SheetCardBg = Color(0xFF141414)
private val SheetBorder = Color(0x14FFFFFF)
private val BrandIndigo = BrandPrimary
private val BrandViolet = BrandSecondary
private val BrandVioletLight = Color(0xFF1E1E1E)

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
                    .background(SheetBackground)
                    .border(
                        width = 1.dp,
                        color = SheetBorder,
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
                        .background(if (isExpanded) BrandViolet else Color(0xFFCBD5E1))
                        .clickable {
                            isExpanded = !isExpanded
                            Timber.tag("TILIK_MONITOR").d("📐 [HANDLE CLICK TOGGLE] isExpanded=$isExpanded")
                        }
                )

            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Tilik Logo + Name Asset
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = id.tilik.app.R.drawable.logo_name),
                    contentDescription = "Tilik",
                    modifier = Modifier.height(36.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )

                // Right: Close Icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E1E))
                        .clickable { animateAndDismiss() },
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
                    .background(SheetBorder)
            )

            // Content Area: Empty State vs Suggestion State
            if (textInput.isBlank()) {
                // ==================== EMPTY STATE ====================
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
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E1E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tilik Fakta Saham",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Salin teks klaim atau ketik di sini untuk memverifikasi data fundamental dan transaksi pasar.",
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quick Samples
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SampleChip(
                            label = "BBRI Akumulasi Asing",
                            modifier = Modifier.weight(1f)
                        ) {
                            textInput = "BBRI asing akumulasi masif ratusan miliar, valuasi murah siap all-time high!"
                            isFromClipboard = true
                        }
                        SampleChip(
                            label = "GOTO Rekor Laba",
                            modifier = Modifier.weight(1f)
                        ) {
                            textInput = "Si ijo mulai diserok bandar YP, valuasi salah harga to the moon!"
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
                        colors = CardDefaults.cardColors(containerColor = SheetCardBg),
                        border = BorderStroke(1.dp, SheetBorder),
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
                                            .background(Color(0xFF1E1E1E))
                                            .border(1.dp, SheetBorder, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "EMITEN: $$detectedTicker",
                                            color = BrandPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF1E1E1E))
                                            .border(1.dp, SheetBorder, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "KLAIM DISKUSI",
                                            color = TextSecondary,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isExpanded) "Perkecil" else "Perluas",
                                        color = BrandPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                isExpanded = !isExpanded
                                                Timber.tag("TILIK_MONITOR").d("📐 [TOGGLE EXPAND] Sheet di-toggle -> isExpanded=$isExpanded")
                                            }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Segarkan",
                                        color = BrandPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                hasUserManuallyEdited = false
                                                fetchLatestClipboard()
                                            }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Hapus",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                textInput = ""
                                                isFromClipboard = false
                                                hasUserManuallyEdited = false
                                            }
                                            .padding(horizontal = 4.dp, vertical = 3.dp)
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
                                        color = TextSecondary.copy(alpha = 0.6f),
                                        fontSize = 13.sp
                                    )
                                },
                                minLines = if (isExpanded) 7 else 3,
                                maxLines = if (isExpanded) 14 else 6,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = AppCard,
                                    unfocusedContainerColor = AppCard,
                                    focusedBorderColor = BrandPrimary,
                                    unfocusedBorderColor = Color(0x1AFFFFFF),
                                    cursorColor = BrandPrimary
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
                                onSubmitClaim(textInput.trim(), detectedTicker)
                            }
                        },
                        enabled = textInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF1E1E1E),
                            disabledContentColor = Color(0xFF737373)
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
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hint dismiss
            Text(
                text = "Ketuk area di luar untuk menutup",
                color = TextSecondary.copy(alpha = 0.7f),
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { animateAndDismiss() }
            )
        }
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
            .background(Color(0xFFF1F5F9))
            .border(1.dp, SheetBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
