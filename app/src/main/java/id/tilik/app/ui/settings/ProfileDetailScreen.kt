package id.tilik.app.ui.settings

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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import id.tilik.app.ui.theme.AppAccent
import id.tilik.app.ui.theme.AppBackground
import id.tilik.app.ui.theme.AppCard
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
    val userRole = currentUser?.userRole ?: UserRole.PEMULA

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
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Profil & Pengaturan Akun",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
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
                    .border(2.dp, AppAccent, CircleShape),
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

        // 1. Role / Mode Persona Setting Card (PUT /api/v1/user/settings)
        Text(
            text = "MODE PERSONA ANALISIS (ROLE)",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = AppCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Role Option: PEMULA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (userRole == UserRole.PEMULA) Color(0x1FFF5C35) else Color.Transparent)
                        .border(
                            1.dp,
                            if (userRole == UserRole.PEMULA) AppAccent else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            if (userRole != UserRole.PEMULA) {
                                scope.launch {
                                    AuthRepository.updateRole(UserRole.PEMULA)
                                }
                            }
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x1AFFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.School, contentDescription = null, tint = AppAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Pemula (Beginner)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Bahasa ramah & refleksi psikologis", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Icon(
                        imageVector = if (userRole == UserRole.PEMULA) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (userRole == UserRole.PEMULA) AppAccent else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Role Option: EXPERT
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (userRole == UserRole.EXPERT) Color(0x1FFF5C35) else Color.Transparent)
                        .border(
                            1.dp,
                            if (userRole == UserRole.EXPERT) AppAccent else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            if (userRole != UserRole.EXPERT) {
                                scope.launch {
                                    AuthRepository.updateRole(UserRole.EXPERT)
                                }
                            }
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x1AFFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = AppAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Pakar (Expert)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Valuasi penuh & devil's advocate", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Icon(
                        imageVector = if (userRole == UserRole.EXPERT) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (userRole == UserRole.EXPERT) AppAccent else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Account Information Card
        Text(
            text = "INFORMASI AKUN GOOGLE",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

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
}
