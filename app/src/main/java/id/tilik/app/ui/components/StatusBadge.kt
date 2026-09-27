package id.tilik.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.ui.theme.AppSurfaceSubtle
import id.tilik.app.ui.theme.StatusDanger
import id.tilik.app.ui.theme.StatusNeutral
import id.tilik.app.ui.theme.StatusSuccess
import id.tilik.app.ui.theme.StatusWarning

enum class BadgeType {
    SUCCESS,
    DANGER,
    WARNING,
    NEUTRAL
}

@Composable
fun StatusBadge(
    text: String,
    type: BadgeType = BadgeType.NEUTRAL,
    showDot: Boolean = true
) {
    val (dotColor, textColor) = when (type) {
        BadgeType.SUCCESS -> Pair(StatusSuccess, StatusSuccess)
        BadgeType.DANGER -> Pair(StatusDanger, StatusDanger)
        BadgeType.WARNING -> Pair(StatusWarning, StatusWarning)
        BadgeType.NEUTRAL -> Pair(StatusNeutral, StatusNeutral)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AppSurfaceSubtle)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            )
        }
    }
}
