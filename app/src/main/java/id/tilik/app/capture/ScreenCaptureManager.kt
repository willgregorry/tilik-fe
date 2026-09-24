package id.tilik.app.capture

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import java.io.ByteArrayOutputStream

class ScreenCaptureManager {

    fun captureFrame(
        mediaProjection: MediaProjection,
        displayWidth: Int,
        displayHeight: Int,
        densityDpi: Int,
        onCaptured: (ByteArray?) -> Unit
    ) {
        val handler = Handler(Looper.getMainLooper())
        var virtualDisplay: VirtualDisplay? = null
        var imageReader: ImageReader? = null

        val projectionCallback = object : MediaProjection.Callback() {
            override fun onStop() {
                try {
                    virtualDisplay?.release()
                    imageReader?.close()
                } catch (_: Exception) {
                }
            }
        }

        try {
            mediaProjection.registerCallback(projectionCallback, handler)

            val reader = ImageReader.newInstance(
                displayWidth,
                displayHeight,
                PixelFormat.RGBA_8888,
                2
            )
            imageReader = reader

            reader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
                override fun onImageAvailable(r: ImageReader) {
                    r.setOnImageAvailableListener(null, null)
                    var image: Image? = null
                    var bitmap: Bitmap? = null
                    var scaledBitmap: Bitmap? = null
                    try {
                        image = r.acquireLatestImage()
                        if (image != null) {
                            val planes = image.planes
                            val buffer = planes[0].buffer
                            val pixelStride = planes[0].pixelStride
                            val rowStride = planes[0].rowStride
                            val rowPadding = rowStride - pixelStride * displayWidth

                            bitmap = Bitmap.createBitmap(
                                displayWidth + rowPadding / pixelStride,
                                displayHeight,
                                Bitmap.Config.ARGB_8888
                            )
                            bitmap.copyPixelsFromBuffer(buffer)

                            val finalWidth = 720
                            val finalHeight = (displayHeight * (720.0 / displayWidth)).toInt()
                            scaledBitmap = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)

                            val outputStream = ByteArrayOutputStream()
                            scaledBitmap.compress(Bitmap.CompressFormat.WEBP, 80, outputStream)
                            val webpBytes = outputStream.toByteArray()

                            onCaptured(webpBytes)
                        } else {
                            onCaptured(null)
                        }
                    } catch (_: Exception) {
                        onCaptured(null)
                    } finally {
                        try {
                            image?.close()
                            bitmap?.recycle()
                            scaledBitmap?.recycle()
                            virtualDisplay?.release()
                            reader.close()
                            mediaProjection.unregisterCallback(projectionCallback)
                        } catch (_: Exception) {
                        }
                    }
                }
            }, handler)

            virtualDisplay = mediaProjection.createVirtualDisplay(
                "TilikScreenCapture",
                displayWidth,
                displayHeight,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.surface,
                null,
                handler
            )
        } catch (_: Exception) {
            try {
                virtualDisplay?.release()
                imageReader?.close()
                mediaProjection.unregisterCallback(projectionCallback)
            } catch (_: Exception) {
            }
            onCaptured(null)
        }
    }
}
