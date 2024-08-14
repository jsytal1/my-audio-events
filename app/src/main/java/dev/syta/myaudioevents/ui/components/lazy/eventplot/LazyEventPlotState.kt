package dev.syta.myaudioevents.ui.components.lazy.eventplot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot

@Composable
fun rememberLazyEventPlotState(
    minMs: Long = 0L,
): LazyEventPlotState {
    return remember {
        LazyEventPlotState(
            minMs = minMs,
        )
    }
}

class LazyEventPlotState(
    val minMs: Long = 0L,
) : ScrollableState {
    var scrollOffset by mutableIntStateOf(0)
        private set

    var msPerPx by mutableDoubleStateOf(0.0)
    var width by mutableIntStateOf(0)

    suspend fun animateScrollToMsRight(targetMsRight: Long, durationMillis: Int = 1_000) {
        val targetMsLeft = targetMsRight - width * msPerPx
        val targetOffset = (targetMsLeft - minMs).coerceAtLeast(0.0) / msPerPx
        animateScrollTo(targetOffset.toInt())
    }

    var maxOffset by mutableIntStateOf(0)
        private set

    private val animatableScrollOffset = Animatable(0f)

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

    suspend fun animateScrollTo(targetOffset: Int) {
        animatableScrollOffset.animateTo(
            targetValue = targetOffset.toFloat(),
            animationSpec = tween(durationMillis = 195, easing = LinearEasing)
        ) {
            scrollOffset = value.toInt()
        }
    }

    override val isScrollInProgress: Boolean
        get() = scrollableState.isScrollInProgress

    override val canScrollForward: Boolean
        get() = scrollOffset < maxOffset

    override val canScrollBackward: Boolean
        get() = scrollOffset > 0

    override fun dispatchRawDelta(delta: Float): Float = scrollableState.dispatchRawDelta(delta)

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ) {
        scrollableState.scroll(scrollPriority, block)
    }
}
