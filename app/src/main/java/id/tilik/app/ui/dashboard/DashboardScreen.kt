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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.tilik.app.ui.dashboard.components.BalanceSection
import id.tilik.app.ui.dashboard.components.MarketOverviewCard
import id.tilik.app.ui.dashboard.components.PortfolioCarousel
import id.tilik.app.ui.dashboard.components.ServiceControlCard
import id.tilik.app.ui.dashboard.components.SystemReadinessCard
import id.tilik.app.ui.dashboard.components.WatchlistSection
import id.tilik.app.ui.theme.AppBackground

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    hasOverlay: Boolean,
    hasAudio: Boolean,
    hasProjection: Boolean,
    isRunning: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestAudio: () -> Unit,
    onRequestProjection: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenAppDetails: () -> Unit,
    onToggleService: () -> Unit,
    onNavigateToDetails: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Balance Section (Hero with Total Balance, Amount, Action Button, and Trend Badge)
        BalanceSection(
            isServiceRunning = isRunning,
            onToggleService = onToggleService
        )

        // 2. Portfolio Carousel (Horizontal scrolling stock cards with live sparkline curves)
        PortfolioCarousel(
            onViewDetailsClick = onNavigateToDetails
        )

        // 3. Watchlist Section (BEI Stocks with squircle icons and price change)
        WatchlistSection()

        // 4. Service Control Card (Floating widget fact-checker controls)
        ServiceControlCard(
            isRunning = isRunning,
            onToggleService = onToggleService
        )

        // 5. System Readiness (Permissions checklist)
        SystemReadinessCard(
            hasOverlay = hasOverlay,
            hasProjection = hasProjection,
            onRequestOverlay = onRequestOverlay,
            onRequestProjection = onRequestProjection,
            onOpenAccessibility = onOpenAccessibility,
            onOpenAppDetails = onOpenAppDetails
        )

        // 6. Market Overview (BEI integration and Sectors API guarantee)
        MarketOverviewCard()

        Spacer(modifier = Modifier.height(24.dp))
    }
}
