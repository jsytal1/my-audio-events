package dev.syta.myaudioevents.utilities

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDuration(millis: Int): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0 || hours > 0) append("${minutes}m ")
        append("${seconds}s")
    }.trim()
}

fun formatMillisToReadableDate(millis: Long, format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val date = Date(millis)
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(date)
}
