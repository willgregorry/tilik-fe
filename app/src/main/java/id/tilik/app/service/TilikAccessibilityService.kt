package id.tilik.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import id.tilik.app.detection.StockKeywordDetector
import timber.log.Timber

class TilikAccessibilityService : AccessibilityService() {

    private var lastDetectedTicker: String? = null
    private var lastDetectionTimestamp: Long = 0L
    private val debounceIntervalMs = 3000L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val textList = mutableListOf<String>()
            event.text?.forEach { textList.add(it.toString()) }
            val desc = event.contentDescription?.toString()
            if (!desc.isNullOrBlank()) textList.add(desc)

            val fullClickedText = textList.joinToString(" ")
            val isCopyAction = fullClickedText.contains("Salin", ignoreCase = true) ||
                    fullClickedText.contains("Copy", ignoreCase = true)

            if (isCopyAction) {
                Timber.tag("TILIK_MONITOR").i("📋 [ACCESSIBILITY COPY DETECTED] Tombol salin ditekan: \"$fullClickedText\"")
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, OverlayService::class.java).apply {
                        action = OverlayService.ACTION_TRIGGER_VERIFY
                    }
                    try {
                        startService(intent)
                    } catch (e: Exception) {
                        Timber.tag("TILIK_MONITOR").w(e, "Gagal memulai service verifikasi")
                    }
                }, 350L)
            }
        }

        val rootNode = rootInActiveWindow ?: return
        val extractedTexts = mutableListOf<String>()
        collectNodeTexts(rootNode, extractedTexts)

        if (extractedTexts.isEmpty()) return

        val fullText = extractedTexts.joinToString(" ")
        val pkg = event?.packageName ?: "app"
        Timber.tag("TILIK_MONITOR").d("📱 [SCROLLING $pkg] Nodes: ${extractedTexts.size} | Preview: ${fullText.take(120)}")

        if (StockKeywordDetector.shouldTriggerAnalysis(fullText)) {
            val ticker = StockKeywordDetector.extractPotentialTicker(fullText)
            val claim = StockKeywordDetector.extractStockClaim(fullText)
            val now = System.currentTimeMillis()
            if (ticker != null && (ticker != lastDetectedTicker || now - lastDetectionTimestamp > debounceIntervalMs)) {
                lastDetectedTicker = ticker
                lastDetectionTimestamp = now
                Timber.tag("TILIK_MONITOR").i("🎯 [STOCK DETECTED] Emiten: $ticker | Klaim: $claim")
                notifyOverlayService(ticker, claim)
            }
        }
    }

    private fun collectNodeTexts(node: AccessibilityNodeInfo, textList: MutableList<String>) {
        val text = node.text?.toString()
        if (!text.isNullOrBlank()) {
            textList.add(text)
        }
        val description = node.contentDescription?.toString()
        if (!description.isNullOrBlank()) {
            textList.add(description)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                collectNodeTexts(child, textList)
            }
        }
    }

    private fun notifyOverlayService(ticker: String, claim: String?) {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_STOCK_DETECTED
            putExtra(OverlayService.EXTRA_TICKER, ticker)
            if (!claim.isNullOrBlank()) {
                putExtra(OverlayService.EXTRA_CLAIM_TEXT, claim)
            }
        }
        startService(intent)
    }

    override fun onInterrupt() {
    }
}
