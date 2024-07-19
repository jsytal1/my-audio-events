package dev.syta.myaudioevents.data.local.entities


data class Event(
    val id: Int,
    val startTime: Long,
    val endTime: Long,
    val eventType: EventType,
)