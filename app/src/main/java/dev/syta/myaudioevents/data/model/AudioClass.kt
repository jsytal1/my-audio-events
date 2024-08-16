package dev.syta.myaudioevents.data.model

data class AudioClass(
    val id: String,
    val name: String,
    val ancestors: List<String>,
)