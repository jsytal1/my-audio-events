package dev.syta.myaudioevents.data.model

data class Label(
    val id: Int = 0,
    val name: String,
) {
    constructor(name: String) : this(0, name)
}