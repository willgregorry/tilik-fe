package id.tilik.app.service

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import id.tilik.app.MainActivity
import timber.log.Timber

/**
 * Activity yang merespons ACTION_PROCESS_TEXT dari sistem Android.
 * Memunculkan opsi "Tilik AI" di toolbar seleksi teks sistem di aplikasi mana pun
 * (WhatsApp, Twitter/X, Chrome, Telegram, Threads, dsb).
 *
 * Begitu diketuk, Activity ini langsung meneruskan teks yang diseleksi ke OverlayService
 * untuk memunculkan Modal Bottom Sheet verifikasi klaim secara otomatis dari bawah.
 */
class ProcessTextActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleProcessTextIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleProcessTextIntent(intent)
    }

    private fun handleProcessTextIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_PROCESS_TEXT) {
            finishWithoutAnimation()
            return
        }

        val rawText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
            ?: intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT_READONLY)
        val selectedText = rawText?.toString()?.trim()

        if (selectedText.isNullOrBlank()) {
            finishWithoutAnimation()
            return
        }

        val hasOverlayPermission = Settings.canDrawOverlays(this)

        if (hasOverlayPermission) {
            val serviceIntent = Intent(this, OverlayService::class.java).apply {
                action = OverlayService.ACTION_PROCESS_TEXT_CLAIM
                putExtra(OverlayService.EXTRA_CLAIM_TEXT, selectedText)
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(this, serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                Timber.tag("TILIK_PROCESS_TEXT").i("🚀 [PROCESS_TEXT] Mengirim teks klaim (${selectedText.length} karakter) ke OverlayService")
            } catch (e: Exception) {
                Timber.tag("TILIK_PROCESS_TEXT").e(e, "Gagal meluncurkan OverlayService dari ProcessTextActivity")
            }

            finishWithoutAnimation()
        } else {
            Toast.makeText(this, "Aktifkan izin Tampilkan di Atas Aplikasi Lain untuk menggunakan Tilik AI", Toast.LENGTH_LONG).show()
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(OverlayService.EXTRA_CLAIM_TEXT, selectedText)
            }
            startActivity(mainIntent)
            finish()
        }
    }

    private fun finishWithoutAnimation() {
        finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}
