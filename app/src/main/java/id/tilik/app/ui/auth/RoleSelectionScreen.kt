package id.tilik.app.ui.auth

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.data.model.UserRole
import id.tilik.app.data.repository.AuthRepository
import id.tilik.app.data.session.SessionManager
import id.tilik.app.ui.theme.AppAccent
import id.tilik.app.ui.theme.AppBackground
import id.tilik.app.ui.theme.AppCard
import id.tilik.app.ui.theme.TextMuted
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun RoleSelectionScreen(
    initialRole: UserRole = UserRole.PEMULA,
    onRoleConfirmed: (UserRole) -> Unit
) {
    var selectedRole by remember { mutableStateOf(initialRole) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Pilih Peran Anda",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.6).sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sesuaikan gaya analisis dengan kebutuhan Anda.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Card 1: Pemula (Image Left)
        RoleChoiceCard(
            role = UserRole.PEMULA,
            title = "Pemula",
            description = "Bahasa santai & psikologi pasar",
            assetFileName = "PEMULA.png",
            isSelected = selectedRole == UserRole.PEMULA,
            fallbackIcon = Icons.Rounded.School,
            imageOnRight = false,
            onSelect = { selectedRole = UserRole.PEMULA }
        )

        // Center "atau" Divider (matching reference UI)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0x14FFFFFF),
                thickness = 1.dp
            )
            Text(
                text = "atau",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0x14FFFFFF),
                thickness = 1.dp
            )
        }

        // Card 2: Pakar (Image Right)
        RoleChoiceCard(
            role = UserRole.EXPERT,
            title = "Pakar",
            description = "Valuasi lengkap & broker flow",
            assetFileName = "EXPERT.png",
            isSelected = selectedRole == UserRole.EXPERT,
            fallbackIcon = Icons.Rounded.AutoGraph,
            imageOnRight = true,
            onSelect = { selectedRole = UserRole.EXPERT }
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Dapat diubah kapan saja di Profil.",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Confirm Button
        Button(
            onClick = {
                if (isSubmitting) return@Button
                isSubmitting = true
                scope.launch {
                    AuthRepository.updateRole(selectedRole)
                    SessionManager.setRoleOnboardingDone(true)
                    isSubmitting = false
                    onRoleConfirmed(selectedRole)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Lanjutkan",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RoleChoiceCard(
    role: UserRole,
    title: String,
    description: String,
    assetFileName: String,
    isSelected: Boolean,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector,
    imageOnRight: Boolean = false,
    onSelect: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(assetFileName, role) {
        val candidates = when (role) {
            UserRole.PEMULA -> listOf("PEMULA.png", assetFileName, "PEMULA.jpg", "pemula.png", "pemula.jpg")
            UserRole.EXPERT -> listOf("EXPERT.png", assetFileName, "EXPERT.jpg", "expert.png", "expert.jpg")
        }
        var loaded: android.graphics.Bitmap? = null
        for (file in candidates) {
            try {
                context.assets.open(file).use { input ->
                    loaded = BitmapFactory.decodeStream(input)
                }
                if (loaded != null) break
            } catch (_: Exception) {
            }
        }
        loaded
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) AppAccent else Color(0x14FFFFFF),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1B1513) else AppCard
        ),
        shape = RoundedCornerShape(26.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!imageOnRight) {
                // Image on Left
                RoleIllustration(bitmap = bitmap, title = title, fallbackIcon = fallbackIcon, isSelected = isSelected)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) AppAccent else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // Text on Left, Image on Right
                Icon(
                    imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) AppAccent else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                RoleIllustration(bitmap = bitmap, title = title, fallbackIcon = fallbackIcon, isSelected = isSelected)
            }
        }
    }
}

@Composable
private fun RoleIllustration(
    bitmap: android.graphics.Bitmap?,
    title: String,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean
) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = title,
            modifier = Modifier
                .size(105.dp),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) Color(0x2BFF5C35) else Color(0x1AFFFFFF))
                .border(1.dp, if (isSelected) AppAccent else Color(0x14FFFFFF), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = null,
                tint = if (isSelected) AppAccent else TextSecondary,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
