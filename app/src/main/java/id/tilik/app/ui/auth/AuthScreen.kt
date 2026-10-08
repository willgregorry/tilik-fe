package id.tilik.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tilik.app.R
import id.tilik.app.ui.theme.AppAccent
import id.tilik.app.ui.theme.AppBackground
import id.tilik.app.ui.theme.AppCard
import id.tilik.app.ui.theme.AppRed
import id.tilik.app.ui.theme.AppRedBg
import id.tilik.app.ui.theme.TextMuted
import id.tilik.app.ui.theme.TextPrimary
import id.tilik.app.ui.theme.TextSecondary

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

@Composable
fun AuthScreen(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    errorDetails: String? = null,
    onGoogleSignInClick: () -> Unit,
    onSkip: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Optional Top Back Button (if provided)
        if (onBackClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AppCard)
                    .border(1.dp, Color(0x14FFFFFF), CircleShape)
                    .clickable { onBackClick.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Center Content: Pure Tilik Icon, Catchy Trendy Indonesian Tagline, & Centered Google Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pure Tilik Icon with solid black container & white logo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF000000))
                    .border(1.5.dp, Color(0x26FFFFFF), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.favicon),
                    contentDescription = "Logo Tilik",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand Title
            Text(
                text = "TILIK",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Trendy & Catchy Indonesian Tagline
            Text(
                text = "Bongkar Rumor Saham BEI dalam Sekejap",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Keputusan investasi lebih terukur, bebas FOMO.",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(44.dp))

            // Centered Google Sign-In Button with google.png icon
            Button(
                onClick = {
                    try {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (_: Exception) {}
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onGoogleSignInClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "Google",
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = "Lanjutkan dengan Google",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Error Message & Diagnostic Log (if any)
            AnimatedVisibility(
                visible = !errorMessage.isNullOrBlank() || !errorDetails.isNullOrBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!errorMessage.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppRedBg)
                                .border(1.dp, Color(0x33EF4444), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = AppRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Debug Error Details Box (Dibawah pesan error untuk diagnosa FE vs BE)
                    if (!errorDetails.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF141414))
                                .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "DIAGNOSTIC ERROR LOG (DEBUG):",
                                    color = Color(0xFFFF8C70),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = errorDetails,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.5.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
