package dev.syta.myaudioevents.data.local.entities

data class Timeline(
    val id: Int,
    val startTime: Long,
    val endTime: Long,
    val events: List<Event>,
)
