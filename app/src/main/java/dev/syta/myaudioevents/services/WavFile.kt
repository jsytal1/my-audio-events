package dev.syta.myaudioevents.services

import java.io.Closeable
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val HEADER_SIZE: Int = 44
private const val BITS_PER_SAMPLE: Short = 16

class WavFile(
    baseDir: String,
    fileName: String,
    private val channelCount: Short,
    private val sampleRate: Int,
) : Closeable {
    val file: File = File(baseDir, fileName)
    private val outputStream = file.outputStream()

    init {
        outputStream.channel.position(HEADER_SIZE.toLong())
    }

    override fun close() {
        writeWavHeader()
        outputStream.flush()
        outputStream.close()
    }

    var durationMillis: Int = 0
        private set
    var sizeBytes: Int = HEADER_SIZE
        private set

    fun write(floatSamples: FloatArray, len: Int) {
        val byteBuffer = ByteBuffer.allocate(2 * len).order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until len) {
            val floatSample = floatSamples[i]
            val shortSample = (floatSample * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            byteBuffer.putShort(shortSample)
        }

        byteBuffer.flip()

        outputStream.channel.write(byteBuffer)
    }

    private fun writeWavHeader() {
        val fileSize = outputStream.channel.position().toInt()

        val riffHeaderSize = 8 // len("RIFF") + # of bytes in the file size field

        // size of the entire file minus 8 bytes for the RIFF header
        val fileSizeMinus8 = fileSize - riffHeaderSize

        // size of the audio data block
        val byteRate = sampleRate * channelCount * (BITS_PER_SAMPLE / 8)
        val blockAlign = channelCount * (BITS_PER_SAMPLE / 8)

        val header = ByteBuffer.allocate(HEADER_SIZE).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(fileSizeMinus8)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16) // size of the 'fmt' data block
            putShort(1) // Audio format (1 for PCM)
            putShort(channelCount)
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign.toShort())
            putShort(BITS_PER_SAMPLE)
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(fileSize - HEADER_SIZE)

        }
        header.flip()
        outputStream.channel.write(header, 0)
    }
}
