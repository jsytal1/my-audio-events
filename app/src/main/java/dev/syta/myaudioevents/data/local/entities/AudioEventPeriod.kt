package dev.syta.myaudioevents.data.local.entities


data class AudioEventPeriod(
    val id: Int,
    val startTime: Long,
    val endTime: Long,
    val type: AudioEventClass,
)