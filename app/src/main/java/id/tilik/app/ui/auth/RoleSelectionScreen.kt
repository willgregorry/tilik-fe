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
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import id.tilik.app.ui.theme.AppGreen
import id.tilik.app.ui.theme.AppGreenBg
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
        Spacer(modifier = Modifier.height(12.dp))

        // Badge Tag
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0x1FFF5C35))
                .border(1.dp, Color(0x40FF5C35), RoundedCornerShape(percent = 50))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "PERSONAS & GAYA BAHASA",
                color = AppAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Pilih Mode Investor Anda",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tilik akan menyesuaikan gaya verifikasi, rekomendasi psikologis, dan kedalaman data pasar sesuai peran Anda.",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Card 1: PEMULA (role_beginner.png)
        RoleChoiceCard(
            role = UserRole.PEMULA,
            title = "Pemula (Beginner)",
            tag = "Panduan Sederhana & Ramah",
            description = "Gaya bahasa lugas tanpa jargon rumit. Dilengkapi refleksi psikologis (cooling-off prompt) untuk mencegah keputusan impulsif atau FOMO.",
            assetFileName = "role_beginner.png",
            isSelected = selectedRole == UserRole.PEMULA,
            fallbackIcon = Icons.Rounded.School,
            onSelect = { selectedRole = UserRole.PEMULA }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Card 2: EXPERT (role_expert.png)
        RoleChoiceCard(
            role = UserRole.EXPERT,
            title = "Pakar (Expert)",
            tag = "Metrik Lengkap & Kritis",
            description = "Akses rasio valuasi mendalam (PE, PBV, EV/EBITDA), analisis bandarmologi arus modal asing, serta tantangan analisis devil's advocate.",
            assetFileName = "role_expert.png",
            isSelected = selectedRole == UserRole.EXPERT,
            fallbackIcon = Icons.Rounded.AutoGraph,
            onSelect = { selectedRole = UserRole.EXPERT }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Catatan: Anda dapat mengubah mode ini sewaktu-waktu di Pengaturan Profil.",
            color = TextMuted,
            fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

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
            shape = RoundedCornerShape(14.dp),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Simpan & Lanjutkan",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RoleChoiceCard(
    role: UserRole,
    title: String,
    tag: String,
    description: String,
    assetFileName: String,
    isSelected: Boolean,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(assetFileName, role) {
        val candidates = when (role) {
            UserRole.PEMULA -> listOf(assetFileName, "PEMULA.jpg", "pemula.jpg", "pemula.png", "role_beginner.png", "beginner.png")
            UserRole.EXPERT -> listOf(assetFileName, "EXPERT.jpg", "expert.jpg", "expert.png", "role_expert.png")
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
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF171311) else AppCard
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Tag & Radio check
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(if (isSelected) Color(0x33FF5C35) else Color(0x14FFFFFF))
                        .border(
                            1.dp,
                            if (isSelected) AppAccent else Color(0x14FFFFFF),
                            RoundedCornerShape(percent = 50)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tag,
                        color = if (isSelected) AppAccent else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) AppAccent else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body Row: Illustration/Icon + Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Asset Image or Fallback Box
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = title,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0x2BFF5C35) else Color(0x1AFFFFFF))
                            .border(1.dp, if (isSelected) AppAccent else Color(0x14FFFFFF), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = fallbackIcon,
                            contentDescription = null,
                            tint = if (isSelected) AppAccent else TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
