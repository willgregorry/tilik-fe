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
import id.tilik.app.ui.dashboard.components.MarketOverviewCard
import id.tilik.app.ui.dashboard.components.ServiceControlCard
import id.tilik.app.ui.dashboard.components.SystemReadinessCard
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
    onToggleService: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ServiceControlCard(
            isRunning = isRunning,
            onToggleService = onToggleService
        )

        SystemReadinessCard(
            hasOverlay = hasOverlay,
            hasProjection = hasProjection,
            onRequestOverlay = onRequestOverlay,
            onRequestProjection = onRequestProjection,
            onOpenAccessibility = onOpenAccessibility,
            onOpenAppDetails = onOpenAppDetails
        )

        MarketOverviewCard()

        Spacer(modifier = Modifier.height(16.dp))
    }
}
