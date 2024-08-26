package dev.syta.myaudioevents.utilities

import android.media.MediaMetadataRetriever
import java.io.File

fun getFileMetadata(filePath: String): Pair<Long, Long> {
    val file = File(filePath)
    val fileSize = file.length()

    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(filePath)
    val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    val duration = durationString?.toLong() ?: 0L

    retriever.release()

    return Pair(fileSize, duration)
}