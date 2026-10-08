package id.tilik.app.ui.history

import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.data.model.HistoryItemSummary
import id.tilik.app.data.model.UserRole
import id.tilik.app.data.model.VerdictLevel
import id.tilik.app.data.repository.AuthRepository
import id.tilik.app.data.session.SessionManager
import id.tilik.app.ui.components.SkeletonBox
import id.tilik.app.ui.components.shimmerEffect
import id.tilik.app.ui.theme.AppAccent
import id.tilik.app.ui.theme.AppBackground
import id.tilik.app.ui.theme.AppCard
import id.tilik.app.ui.theme.TextMuted
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onItemClick: ((historyId: String, ticker: String?) -> Unit)? = null,
    onScanNowClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val currentUser by SessionManager.currentUserState.collectAsState()
    val userRole = currentUser?.userRole ?: SessionManager.getUserRole()
    val roleBitmap = remember(userRole) {
        val candidates = when (userRole) {
            UserRole.EXPERT -> listOf("EXPERT.png", "EXPERT.jpg", "expert.png", "expert.jpg")
            UserRole.PEMULA -> listOf("PEMULA.png", "PEMULA.jpg", "pemula.png", "pemula.jpg")
        }
        var loaded: android.graphics.Bitmap? = null
        for (f in candidates) {
            try {
                context.assets.open(f).use { input ->
                    loaded = BitmapFactory.decodeStream(input)
                }
                if (loaded != null) break
            } catch (_: Exception) {
            }
        }
        loaded
    }

    val cachedInitial = remember { AuthRepository.getCachedHistory() }
    var itemsList by remember { mutableStateOf(cachedInitial?.items ?: emptyList()) }
    var isLoading by remember { mutableStateOf(cachedInitial == null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Sesuai Fakta", "Waspada", "Hoax")
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "RefreshRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    fun loadHistory(forceRefresh: Boolean = false) {
        if (forceRefresh) {
            isRefreshing = true
        } else if (itemsList.isEmpty()) {
            isLoading = true
        }
        scope.launch {
            try {
                withTimeoutOrNull(4000L) {
                    val result = AuthRepository.getHistory(limit = 30, forceRefresh = forceRefresh)
                    if (result.isSuccess) {
                        itemsList = result.getOrNull()?.items ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Fetch history failed")
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadHistory(forceRefresh = false)
    }

    val filteredList = remember(selectedFilter, itemsList) {
        when (selectedFilter) {
            "Sesuai Fakta" -> itemsList.filter { it.verdict == VerdictLevel.SESUAI_FAKTA || it.verdict == VerdictLevel.GREEN }
            "Waspada" -> itemsList.filter { it.verdict == VerdictLevel.WASPADA || it.verdict == VerdictLevel.YELLOW }
            "Hoax" -> itemsList.filter { it.verdict == VerdictLevel.HOAX_BAHAYA || it.verdict == VerdictLevel.RED }
            else -> itemsList
        }
    }

    val insetsModifier = if (onBackClick != null) {
        Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 12.dp)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .then(insetsModifier)
            .padding(horizontal = 20.dp)
    ) {
        if (onBackClick == null) {
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBackClick != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppCard)
                            .border(1.dp, Color(0x14FFFFFF), CircleShape)
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }

                Column {
                    Text(
                        text = "Riwayat Verifikasi",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "${filteredList.size} catatan fact-check akun Google",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppCard)
                    .border(1.dp, Color(0x14FFFFFF), CircleShape)
                    .clickable { loadHistory(forceRefresh = true) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Muat Ulang",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(if (isRefreshing) rotationAngle else 0f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filters) { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(if (isSelected) Color.White else AppCard)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.White else Color(0x14FFFFFF),
                            shape = RoundedCornerShape(percent = 50)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) Color.Black else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // History Items Content
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                repeat(4) {
                    HistoryCardSkeleton()
                }
            }
        } else if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    if (roleBitmap != null) {
                        Image(
                            bitmap = roleBitmap.asImageBitmap(),
                            contentDescription = if (userRole == UserRole.EXPERT) "Pakar" else "Pemula",
                            modifier = Modifier.size(185.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Belum Ada Riwayat",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Masih belum ada hasil scan sebelumnya. Scan cuitan bursa atau klaim saham Anda sekarang.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onScanNowClick?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Scan sekarang",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    HistoryItemCardView(
                        item = item,
                        onClick = {
                            onItemClick?.invoke(item.id, item.ticker)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCardView(
    item: HistoryItemSummary,
    onClick: () -> Unit
) {
    val verdictLevel = item.verdict
    val formattedScore = (item.confidenceScore * 100).toInt().coerceIn(0, 100)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = AppCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Ticker & Verdict Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val ticker = item.ticker ?: "-"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1AFFFFFF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = ticker,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item.sourcePlatform?.uppercase() ?: "X",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(verdictLevel.containerColor)
                        .border(1.dp, verdictLevel.borderColor, RoundedCornerShape(percent = 50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${verdictLevel.label} • $formattedScore%",
                        color = verdictLevel.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tweet / Claim Preview
            Text(
                text = "\"${item.tweetPreview.trim()}\"",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 18.sp,
                maxLines = 3
            )

            if (!item.companyName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.companyName,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Row: Date & Action Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.createdAt.take(10),
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detail Forensik",
                        color = AppAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = null,
                        tint = AppAccent,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCardSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppCard)
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBox(modifier = Modifier.size(width = 70.dp, height = 22.dp))
                SkeletonBox(modifier = Modifier.size(width = 110.dp, height = 22.dp), shape = RoundedCornerShape(percent = 50))
            }
            SkeletonBox(modifier = Modifier.size(width = 240.dp, height = 16.dp))
            SkeletonBox(modifier = Modifier.size(width = 180.dp, height = 14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBox(modifier = Modifier.size(width = 80.dp, height = 12.dp))
                SkeletonBox(modifier = Modifier.size(width = 90.dp, height = 12.dp))
            }
        }
    }
}
