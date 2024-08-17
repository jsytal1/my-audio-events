package dev.syta.myaudioevents.data.model

data class UserAudioClass(
    val id: String,
    val name: String,
    val ancestors: List<String>,
    val isFollowed: Boolean,
) {
    constructor(audioClass: AudioClass, userData: UserData) : this(
        id = audioClass.id,
        name = audioClass.name,
        ancestors = audioClass.ancestors,
        isFollowed = audioClass.id in userData.followedAudioClasses,
    )
}

fun List<AudioClass>.mapToUserAudioClasses(userData: UserData): List<UserAudioClass> =
    map { UserAudioClass(it, userData) }