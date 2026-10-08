package id.tilik.app.ui.settings

import androidx.activity.compose.BackHandler

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.tilik.app.R
import id.tilik.app.data.model.UserRole
import id.tilik.app.data.repository.AuthRepository
import id.tilik.app.data.session.SessionManager
import id.tilik.app.ui.auth.RoleChoiceCard
import id.tilik.app.ui.components.UnsavedChangesDialog
import id.tilik.app.ui.theme.AppAccent
import id.tilik.app.ui.theme.AppBackground
import id.tilik.app.ui.theme.AppCard
import id.tilik.app.ui.theme.AppGrayDark
import id.tilik.app.ui.theme.AppGreen
import id.tilik.app.ui.theme.AppRed
import id.tilik.app.ui.theme.AppRedBg
import id.tilik.app.ui.theme.TextMuted
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ProfileDetailScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val currentUser by SessionManager.currentUserState.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val displayName = currentUser?.name?.takeIf { it.isNotBlank() } ?: "User"
    val displayEmail = currentUser?.email ?: "-"
    val activeRole = currentUser?.userRole ?: SessionManager.getUserRole()
    var selectedRole by remember(activeRole) { mutableStateOf(activeRole) }
    var isSavingRole by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    val hasRoleChanges = selectedRole != activeRole

    val handleBack = {
        if (hasRoleChanges) {
            showUnsavedChangesDialog = true
        } else {
            onBackClick()
        }
    }

    BackHandler(enabled = hasRoleChanges) {
        showUnsavedChangesDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppCard)
                        .border(1.dp, Color(0x14FFFFFF), CircleShape)
                        .clickable { handleBack() },
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

                Text(
                    text = "Profil",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val hasRoleChanges = selectedRole != activeRole
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (hasRoleChanges) AppAccent else Color(0x1AFFFFFF))
                    .border(
                        1.dp,
                        if (hasRoleChanges) Color.Transparent else Color(0x14FFFFFF),
                        RoundedCornerShape(percent = 50)
                    )
                    .clickable(enabled = hasRoleChanges && !isSavingRole) {
                        scope.launch {
                            isSavingRole = true
                            AuthRepository.updateRole(selectedRole)
                            isSavingRole = false
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSavingRole) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = "Simpan",
                        color = if (hasRoleChanges) Color.White else AppGrayDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Avatar Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(AppCard)
                    .border(1.dp, Color(0x14FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!currentUser?.picture.isNullOrBlank()) {
                    AsyncImage(
                        model = currentUser?.picture,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.favicon),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = displayName,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = displayEmail,
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Role Setting
        Text(
            text = "MODE INVESTOR",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Role Option 1: PEMULA
        RoleChoiceCard(
            role = UserRole.PEMULA,
            title = "Pemula",
            description = "Bahasa santai & psikologi pasar",
            assetFileName = "PEMULA.png",
            isSelected = selectedRole == UserRole.PEMULA,
            fallbackIcon = Icons.Rounded.School,
            imageOnRight = false,
            onSelect = {
                selectedRole = UserRole.PEMULA
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Role Option 2: EXPERT
        RoleChoiceCard(
            role = UserRole.EXPERT,
            title = "Pakar",
            description = "Valuasi lengkap & broker flow",
            assetFileName = "EXPERT.png",
            isSelected = selectedRole == UserRole.EXPERT,
            fallbackIcon = Icons.Rounded.AutoGraph,
            imageOnRight = true,
            onSelect = {
                selectedRole = UserRole.EXPERT
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Account Information Card
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "AKUN GOOGLE",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = AppCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Nama Akun", color = TextMuted, fontSize = 11.sp)
                        Text(text = displayName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0x0FFFFFFF), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Email, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Email Terdaftar", color = TextMuted, fontSize = 11.sp)
                        Text(text = displayEmail, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Logout Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppRedBg)
                .border(1.dp, Color(0x33EF4444), RoundedCornerShape(14.dp))
                .clickable {
                    AuthRepository.logout()
                    onLogoutClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                    contentDescription = "Logout",
                    tint = AppRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Keluar dari Akun (Logout)",
                    color = AppRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            title = "Perubahan Belum Disimpan",
            message = "Perubahan role Anda belum disimpan. Yakin ingin keluar tanpa menyimpan?",
            discardText = "Buang",
            keepEditingText = "Lanjut Edit",
            onDiscard = {
                showUnsavedChangesDialog = false
                selectedRole = activeRole
                onBackClick()
            },
            onKeepEditing = {
                showUnsavedChangesDialog = false
            }
        )
    }
}
