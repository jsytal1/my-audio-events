package dev.syta.myaudioevents.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import dev.syta.myaudioevents.R
import dev.syta.myaudioevents.designsystem.icon.MaeIcons

sealed interface MaeDestinations {
    val route: String
}

sealed interface MaeTopLevelDestination : MaeDestinations {
    val icon: ImageVector
    val labelId: Int
}

data object Recorder : MaeTopLevelDestination {
    override val icon = MaeIcons.Mic
    override val labelId = R.string.recorder_title
    override val route = "recorder"
}

data object Recordings : MaeTopLevelDestination {
    override val icon = MaeIcons.ViewList
    override val labelId = R.string.recordings_title
    override val route = "recordings"
}

data object Categories : MaeTopLevelDestination {
    override val icon = MaeIcons.Category
    override val labelId = R.string.categories_title
    override val route = "categories"
}

data object LabelList : MaeTopLevelDestination {
    override val icon = MaeIcons.Label
    override val labelId = R.string.labels_title
    override val route = "labels"
}

val TopLevelDestinations: List<MaeTopLevelDestination> =
    listOf(Recorder, Recordings, Categories, LabelList)