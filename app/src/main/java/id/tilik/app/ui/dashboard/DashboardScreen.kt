package id.tilik.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.tilik.app.data.model.HistoryItemSummary
import id.tilik.app.data.repository.AuthRepository
import id.tilik.app.ui.dashboard.components.MarketOverviewCard
import id.tilik.app.ui.dashboard.components.RecentScansSection
import id.tilik.app.ui.dashboard.components.ServiceControlCard
import id.tilik.app.ui.dashboard.components.SystemReadinessCard
import id.tilik.app.ui.theme.AppBackground
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    isRunning: Boolean = false,
    hasOverlay: Boolean = false,
    hasProjection: Boolean = false,
    onToggleService: () -> Unit = {},
    onRequestOverlay: () -> Unit = {},
    onRequestProjection: () -> Unit = {},
    onOpenAccessibility: () -> Unit = {},
    onOpenAppDetails: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onHistoryItemClick: (String, String?) -> Unit = { _, _ -> },
    onNavigateToDetails: () -> Unit = {},
    onNavigateToStock: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var recentItems by remember { mutableStateOf<List<HistoryItemSummary>>(emptyList()) }
    var isHistoryLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Load recent history from cache / API
        val cached = AuthRepository.getCachedHistory()
        if (cached != null) {
            recentItems = cached.items
        } else {
            isHistoryLoading = true
        }
        scope.launch {
            try {
                withTimeoutOrNull(3000L) {
                    val result = AuthRepository.getHistory(limit = 5, forceRefresh = false)
                    if (result.isSuccess) {
                        recentItems = result.getOrNull()?.items ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Dashboard fetch recent history failed")
            } finally {
                isHistoryLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Status Widget & Layanan Floating Service
        ServiceControlCard(
            isRunning = isRunning,
            onToggleService = onToggleService
        )

        // 2. Riwayat Pemindaian Terakhir (Murni dari GET /api/v1/history)
        RecentScansSection(
            items = recentItems,
            isLoading = isHistoryLoading,
            onViewAllClick = onNavigateToHistory,
            onItemClick = { item ->
                onHistoryItemClick(item.id, item.ticker)
            }
        )

        // 3. Kesiapan Izin Sistem Android
        SystemReadinessCard(
            hasOverlay = hasOverlay,
            hasProjection = hasProjection,
            onRequestOverlay = onRequestOverlay,
            onRequestProjection = onRequestProjection,
            onOpenAccessibility = onOpenAccessibility,
            onOpenAppDetails = onOpenAppDetails
        )

        // 4. Integrasi Pasar BEI & Sectors Financial API
        MarketOverviewCard()

        Spacer(modifier = Modifier.height(24.dp))
    }
}
