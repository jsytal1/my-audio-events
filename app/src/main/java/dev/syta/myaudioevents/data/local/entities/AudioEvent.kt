package dev.syta.myaudioevents.data.local.entities


data class AudioEvent(
    val id: Int?,
    val startTime: Long,
    val endTime: Long,
    val eventType: EventType,
    val score: Float,
)