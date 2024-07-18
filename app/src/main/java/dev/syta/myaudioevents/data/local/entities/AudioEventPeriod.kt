package dev.syta.myaudioevents.data.local.entities

import java.time.Instant


data class AudioEventPeriod(
    val id: String,
    val startTime: Instant,
    val endTime: Instant,
    val type: AudioEventClass,
)