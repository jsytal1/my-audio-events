package dev.syta.myaudioevents.navigation

sealed interface MaeDestinations {
    val route: String
}

data object Recorder : MaeDestinations {
    override val route = "recorder"
}

data object Categories : MaeDestinations {
    override val route = "categories"
}