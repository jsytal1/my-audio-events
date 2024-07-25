package dev.syta.myaudioevents.navigation

sealed interface MaeDestinations {
    val route: String
}

data object Home : MaeDestinations {
    override val route = "home"
}

data object Watch : MaeDestinations {
    override val route = "watch"
}