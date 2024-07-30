package dev.syta.myaudioevents.ui.components.lazy.eventplot

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot

@Composable
fun rememberLazyEventPlotState(
): LazyEventPlotState {
    return remember {
        LazyEventPlotState()
    }
}

class LazyEventPlotState {
    var scrollOffset by mutableIntStateOf(0)
        private set

    private var maxOffset by mutableIntStateOf(0)

    internal fun updateMaxScrollOffset(newMaxOffset: Int) {
        val currentOffset = Snapshot.withoutReadObservation { scrollOffset }
        if (currentOffset > newMaxOffset) {
            scrollOffset = newMaxOffset
        }
        maxOffset = newMaxOffset
    }


    val scrollableState = ScrollableState { -onScroll(-it) }

    private var scrollToBeConsumed: Float = 0f

    private fun onScroll(distance: Float): Float {
        scrollToBeConsumed += distance


        val intDelta = scrollToBeConsumed.toInt()
        val absoluteOffset = scrollOffset + intDelta
        val clampedOffset = absoluteOffset.coerceIn(0, maxOffset)
        val wasClamped = clampedOffset != absoluteOffset

        val consumed = clampedOffset - scrollOffset
        scrollOffset += consumed

        if (wasClamped) {
            scrollToBeConsumed = 0f
            return consumed.toFloat()
        } else {
            scrollToBeConsumed = distance - consumed
            return distance
        }
    }

}
