package dev.syta.myaudioevents.data.local.entities

import java.time.Instant

data class MonitoringPeriod(
    val id: String,
    val startTime: Instant,
    val endTime: Instant,
)
