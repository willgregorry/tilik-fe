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
import id.tilik.app.ui.dashboard.components.TrendingSection
import id.tilik.app.ui.dashboard.components.WatchlistSection
import id.tilik.app.ui.theme.AppBackground

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    isRunning: Boolean = false,
    onToggleService: () -> Unit = {},
    onNavigateToDetails: () -> Unit = {},
    onNavigateToStock: (String) -> Unit = {}
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

        // 3. Trending Section (Saham & Rumor Sedang Tren di BEI & Medsos)
        TrendingSection(
            onSelectStock = { ticker ->
                onNavigateToStock(ticker)
            }
        )

        // 4. Watchlist Section (BEI Stocks with squircle icons and price change)
        WatchlistSection()

        // 5. Market Overview (BEI integration and Sectors API guarantee)
        MarketOverviewCard()

        Spacer(modifier = Modifier.height(24.dp))
    }
}
