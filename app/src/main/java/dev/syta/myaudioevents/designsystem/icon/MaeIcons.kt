package dev.syta.myaudioevents.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import dev.syta.myaudioevents.R


object MaeIcons {
    val Check = Icons.Filled.Check
    val Settings = Icons.Filled.Settings
    val Filter: ImageVector
        @Composable get() = ImageVector.vectorResource(id = R.drawable.baseline_filter_alt_24)
}