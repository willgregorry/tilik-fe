package id.tilik.app.capture

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioBufferRecorder(
    private val bufferDurationSeconds: Int = 5,
    private val sampleRate: Int = 16000
) {
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bytesPerSample = 2
    private val totalBufferSizeBytes = sampleRate * bytesPerSample * bufferDurationSeconds

    private val ringBuffer = ByteArray(totalBufferSizeBytes)
    private var writeHead = 0
    private var isBufferFull = false
    private val bufferLock = Any()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("MissingPermission")
    fun start() {
        if (recordingJob?.isActive == true) return

        val minHardwareBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            channelConfig,
            audioFormat
        )

        val bufferSize = maxOf(minHardwareBufferSize, 4096)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        audioRecord?.startRecording()

        recordingJob = scope.launch {
            val chunk = ByteArray(2048)
            while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val readBytes = audioRecord?.read(chunk, 0, chunk.size) ?: 0
                if (readBytes > 0) {
                    appendChunk(chunk, readBytes)
                }
            }
        }
    }

    private fun appendChunk(chunk: ByteArray, length: Int) {
        synchronized(bufferLock) {
            var bytesCopied = 0
            while (bytesCopied < length) {
                val spaceUntilEnd = totalBufferSizeBytes - writeHead
                val bytesToCopyNow = minOf(length - bytesCopied, spaceUntilEnd)
                System.arraycopy(chunk, bytesCopied, ringBuffer, writeHead, bytesToCopyNow)
                writeHead += bytesToCopyNow
                bytesCopied += bytesToCopyNow
                if (writeHead >= totalBufferSizeBytes) {
                    writeHead = 0
                    isBufferFull = true
                }
            }
        }
    }

    fun getLatestWavBytes(): ByteArray {
        val pcmData: ByteArray
        synchronized(bufferLock) {
            if (!isBufferFull) {
                pcmData = ringBuffer.copyOfRange(0, writeHead)
            } else {
                pcmData = ByteArray(totalBufferSizeBytes)
                val firstPartLength = totalBufferSizeBytes - writeHead
                System.arraycopy(ringBuffer, writeHead, pcmData, 0, firstPartLength)
                System.arraycopy(ringBuffer, 0, pcmData, firstPartLength, writeHead)
            }
        }
        return wrapPcmWithWavHeader(pcmData, sampleRate, 1, 16)
    }

    fun stop() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {
        }
        audioRecord = null
    }

    private fun wrapPcmWithWavHeader(
        pcmBytes: ByteArray,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ): ByteArray {
        val totalAudioLen = pcmBytes.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * (bitsPerSample / 8)

        val header = ByteArray(44)
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put('R'.code.toByte())
        buffer.put('I'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.putInt(totalDataLen)
        buffer.put('W'.code.toByte())
        buffer.put('A'.code.toByte())
        buffer.put('V'.code.toByte())
        buffer.put('E'.code.toByte())
        buffer.put('f'.code.toByte())
        buffer.put('m'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put(' '.code.toByte())
        buffer.putInt(16)
        buffer.putShort(1.toShort())
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort((channels * (bitsPerSample / 8)).toShort())
        buffer.putShort(bitsPerSample.toShort())
        buffer.put('d'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.putInt(totalAudioLen)

        val out = ByteArrayOutputStream(header.size + pcmBytes.size)
        out.write(header)
        out.write(pcmBytes)
        return out.toByteArray()
    }
}
