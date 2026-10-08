package id.tilik.app.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import id.tilik.app.capture.AudioBufferRecorder
import id.tilik.app.capture.ScreenCaptureManager
import id.tilik.app.detection.StockKeywordDetector
import id.tilik.app.data.model.BrokerDetail
import id.tilik.app.data.model.BrokerFlowDetail
import id.tilik.app.data.model.ExpandedDetails
import id.tilik.app.data.model.FactCheckPoint
import id.tilik.app.data.model.FinancialHealthDetail
import id.tilik.app.data.model.ValuationPeerDetail
import id.tilik.app.data.model.VerificationResponse
import id.tilik.app.data.repository.VerificationRepository
import id.tilik.app.data.repository.VerificationRepositoryImpl
import id.tilik.app.model.OverlayState
import id.tilik.app.ui.overlay.OverlayWindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.io.ByteArrayOutputStream

class OverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var overlayWindowManager: OverlayWindowManager? = null
    private var audioRecorder: AudioBufferRecorder? = null
    private val screenCaptureManager = ScreenCaptureManager()
    private val repository: VerificationRepository = VerificationRepositoryImpl()

    private var clipboardManager: ClipboardManager? = null
    private var clipListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    private var mediaProjection: MediaProjection? = null
    private var activeDetectedTicker: String? = null
    private var activeClaimText: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        createNotificationChannel()
        startAsForeground()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            try {
                audioRecorder = AudioBufferRecorder(bufferDurationSeconds = 5)
                audioRecorder?.start()
            } catch (e: Exception) {
                Timber.tag("TILIK_MONITOR").w(e, "Tidak dapat memulai audio recorder")
            }
        }

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipListener = ClipboardManager.OnPrimaryClipChangedListener {
            checkAndProcessClipboard()
        }
        clipboardManager?.addPrimaryClipChangedListener(clipListener)

        overlayWindowManager = OverlayWindowManager(
            context = this,
            onBubbleClicked = { openClaimInput() },
            onClaimSubmitted = { claim, ticker -> processClaimVerification(claim, ticker) },
            onRetryClicked = { openClaimInput() },
            onCloseClicked = { resetToIdle() },
            onStopServiceClicked = {
                getSharedPreferences("tilik_prefs", Context.MODE_PRIVATE)
                    .edit().putBoolean("service_explicitly_stopped", true).apply()
                stopSelf()
            }
        )
        overlayWindowManager?.show()
        Timber.tag("TILIK_MONITOR").i("🟢 [SERVICE STARTED] Floating overlay aktif & siap menilik")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SET_MEDIA_PROJECTION_TOKEN -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_DATA)
                }
                if (resultCode != 0 && data != null) {
                    val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    mediaProjection = projectionManager.getMediaProjection(resultCode, data)
                    hasProjectionToken = true
                    startAsForeground()
                    Timber.tag("TILIK_MONITOR").i("🔑 [TOKEN GRANTED] Izin MediaProjection aktif")
                }
            }
            ACTION_STOCK_DETECTED -> {
                val ticker = intent.getStringExtra(EXTRA_TICKER)
                val claim = intent.getStringExtra(EXTRA_CLAIM_TEXT)
                if (!ticker.isNullOrBlank()) {
                    activeDetectedTicker = ticker
                    activeClaimText = claim
                    overlayWindowManager?.updateState(
                        state = OverlayState.IDLE,
                        ticker = ticker,
                        claimText = claim
                    )
                    Timber.tag("TILIK_MONITOR").i("💡 [BUBBLE UPDATED] Menampilkan badge emiten: $ticker")
                }
            }
            ACTION_TRIGGER_VERIFY -> {
                openClaimInput()
            }
            ACTION_STOP -> {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Timber.tag("TILIK_MONITOR").i("🧹 [TASK REMOVED] Aplikasi diswipe dari Recent Apps -> Menghentikan service & menutup floating bubble")
        overlayWindowManager?.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    fun getLatestClipboardText(): String? {
        return try {
            val cm = clipboardManager ?: (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
            if (cm?.hasPrimaryClip() == true) {
                val clip = cm.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    clip.getItemAt(0).coerceToText(this)?.toString()?.trim()
                } else null
            } else null
        } catch (e: Exception) {
            Timber.tag("TILIK_MONITOR").d("Clipboard access pending user interaction: ${e.message}")
            null
        }
    }

    private fun checkAndProcessClipboard() {
        val text = getLatestClipboardText()
        if (!text.isNullOrBlank() && text != activeClaimText) {
            val ticker = StockKeywordDetector.extractPotentialTicker(text)
            val hasStockContext = StockKeywordDetector.shouldTriggerAnalysis(text)
            
            // Hanya auto-trigger Bottom Sheet jika teks relevan dengan saham/emiten
            if (ticker != null || hasStockContext) {
                activeClaimText = text
                activeDetectedTicker = ticker
                Timber.tag("TILIK_MONITOR").i("🚀 [AUTO SHEET TRIGGER] Teks saham terdeteksi: \"${text.take(60)}...\" | Emiten: $ticker -> Meluncurkan Modal Bottom Sheet")
                
                // Langsung memunculkan Modal Bottom Sheet di atas Threads ala Grammarly
                overlayWindowManager?.updateState(
                    state = OverlayState.INPUT,
                    ticker = ticker,
                    claimText = text
                )
            } else {
                Timber.tag("TILIK_MONITOR").d("📋 [CLIPBOARD IGNORED] Teks umum non-saham: \"${text.take(40)}\"")
            }
        }
    }

    private fun openClaimInput() {
        val clipText = getLatestClipboardText()
        val textToUse = if (!clipText.isNullOrBlank()) clipText else activeClaimText
        val tickerToUse = textToUse?.let { StockKeywordDetector.extractPotentialTicker(it) } ?: activeDetectedTicker

        overlayWindowManager?.updateState(
            state = OverlayState.INPUT,
            ticker = tickerToUse,
            claimText = textToUse
        )
        Timber.tag("TILIK_MONITOR").i("📝 [BOTTOM SHEET OPENED] Membuka modal bottom sheet dengan teks: \"${textToUse?.take(50)}\"")
    }

    private fun processClaimVerification(claim: String, ticker: String?) {
        val resolvedTicker = ticker ?: StockKeywordDetector.extractPotentialTicker(claim) ?: "IDX"
        activeDetectedTicker = resolvedTicker
        activeClaimText = claim

        Timber.tag("TILIK_TERMINAL").i("==================================================")
        Timber.tag("TILIK_TERMINAL").i("📋 KLAIM DI-PASTE DARI HP USER:")
        Timber.tag("TILIK_TERMINAL").i("👉 \"$claim\"")
        Timber.tag("TILIK_TERMINAL").i("🎯 Emiten Terdeteksi : $resolvedTicker")
        Timber.tag("TILIK_TERMINAL").i("==================================================")

        overlayWindowManager?.updateState(
            state = OverlayState.ANALYZING,
            ticker = resolvedTicker
        )

        serviceScope.launch {
            var verifiedData: VerificationResponse? = null

            try {
                val result = withContext(Dispatchers.IO) {
                    repository.verify(
                        text = claim,
                        sourcePlatform = "x",
                        detectedTicker = resolvedTicker
                    )
                }
                result.onSuccess { response ->
                    verifiedData = response
                }
            } catch (e: Exception) {
                Timber.tag("TILIK_TERMINAL").e(e, "Gagal melakukan verifikasi API: ${e.message}")
            }

            val finalData = verifiedData ?: createFailedData(resolvedTicker, claim)

            delay(250)

            overlayWindowManager?.updateState(
                state = OverlayState.SUCCESS,
                data = finalData,
                claimText = claim
            )

            Timber.tag("TILIK_TERMINAL").i("✅ [VERDICT KELUAR] Emiten: ${finalData.ticker} | Vonis: ${finalData.verdict} | Keyakinan: ${finalData.confidencePercentage}%")
        }
    }

    private suspend fun captureFrameSynchronously(projection: MediaProjection): ByteArray? {
        val metrics = resources.displayMetrics
        return kotlin.coroutines.suspendCoroutine { cont ->
            try {
                screenCaptureManager.captureFrame(
                    mediaProjection = projection,
                    displayWidth = metrics.widthPixels,
                    displayHeight = metrics.heightPixels,
                    densityDpi = metrics.densityDpi
                ) { bytes ->
                    cont.resumeWith(Result.success(bytes))
                }
            } catch (_: Exception) {
                cont.resumeWith(Result.success(null))
            }
        }
    }

    private fun createFallbackImageBytes(): ByteArray {
        val bitmap = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.WEBP, 80, stream)
        bitmap.recycle()
        return stream.toByteArray()
    }

    private fun createFailedData(ticker: String, claim: String): VerificationResponse {
        val cleanTicker = ticker.uppercase().takeIf { it.isNotBlank() && it != "IDX" } ?: "-"
        return VerificationResponse(
            status = "failed",
            ticker = cleanTicker,
            companyName = "-",
            verdict = "-",
            confidenceScore = 0.0,
            points = emptyList(),
            coolingOffPrompt = "-",
            details = ExpandedDetails(
                valuation = ValuationPeerDetail(
                    peRatio = null,
                    pbvRatio = null,
                    industryMedianPe = null,
                    industryMedianPbv = null,
                    valuationStatus = "-"
                ),
                brokerFlow = BrokerFlowDetail(
                    foreignNetIdr = 0.0,
                    topBuyers = emptyList(),
                    topSellers = emptyList(),
                    summaryVerdict = "-"
                ),
                financialHealth = FinancialHealthDetail(
                    netProfitGrowthYoy = null,
                    operatingCashFlowIdr = null,
                    isFca = false,
                    specialNotations = emptyList()
                )
            ),
            isCached = false
        )
    }

    private fun resetToIdle() {
        activeClaimText = null
        activeDetectedTicker = null
        overlayWindowManager?.updateState(
            state = OverlayState.IDLE,
            ticker = null,
            claimText = null
        )
        Timber.tag("TILIK_MONITOR").i("🔄 [MINIMIZE TO BUBBLE] Modal Bottom Sheet diminimize ke floating bubble")
    }

    private fun startAsForeground() {
        val notification = buildForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            if (hasProjectionToken) {
                fgsType = fgsType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                fgsType = fgsType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            startForeground(NOTIFICATION_ID, notification, fgsType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tilik Siap Menilik")
            .setContentText("Ketuk bubble untuk memeriksa klaim saham")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tilik Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi status pemantau pasar saham Tilik"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        hasProjectionToken = false
        clipListener?.let {
            try {
                clipboardManager?.removePrimaryClipChangedListener(it)
            } catch (_: Exception) {
            }
        }
        serviceScope.cancel()
        audioRecorder?.stop()
        overlayWindowManager?.hide()
        try {
            mediaProjection?.stop()
        } catch (_: Exception) {
        }
        mediaProjection = null
        Timber.tag("TILIK_MONITOR").i("🛑 [SERVICE DESTROYED] Layanan Tilik dihentikan")
    }

    companion object {
        var isServiceRunning: Boolean = false
        var hasProjectionToken: Boolean = false

        const val CHANNEL_ID = "tilik_overlay_channel"
        const val NOTIFICATION_ID = 2026

        const val ACTION_START = "id.tilik.app.ACTION_START"
        const val ACTION_STOP = "id.tilik.app.ACTION_STOP"
        const val ACTION_SET_MEDIA_PROJECTION_TOKEN = "id.tilik.app.ACTION_SET_MEDIA_PROJECTION_TOKEN"
        const val ACTION_STOCK_DETECTED = "id.tilik.app.ACTION_STOCK_DETECTED"
        const val ACTION_TRIGGER_VERIFY = "id.tilik.app.ACTION_TRIGGER_VERIFY"

        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_DATA = "extra_data"
        const val EXTRA_TICKER = "extra_ticker"
        const val EXTRA_CLAIM_TEXT = "extra_claim_text"
    }
}
