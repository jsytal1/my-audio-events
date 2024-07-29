package dev.syta.myaudioevents.ui.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.syta.myaudioevents.designsystem.MaeBackground
import dev.syta.myaudioevents.ui.theme.MaeTheme
import java.text.SimpleDateFormat
import java.util.Locale

class TimeScrollState {
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


@Composable
fun TimeTicks(
    minMs: Long,
    maxMs: Long,
    majorTick: @Composable (timeMs: Long) -> Unit,
    minorTick: @Composable (timeMs: Long) -> Unit,
    modifier: Modifier = Modifier,
    majorTickMs: Int = 60_000,
    minorTickMs: Int = 5_000,
    tickWidth: Dp = 128.dp,
    state: TimeScrollState,
) {

    val minorTickStartMs = minMs / minorTickMs * minorTickMs
    val majorTickStartMs = minMs / majorTickMs * majorTickMs
    val majorTicks = @Composable {
        for (timeMs in majorTickStartMs..maxMs step majorTickMs.toLong()) {
            majorTick(timeMs)
        }
    }
    val minorTicks = @Composable {
        for (timeMs in minorTickStartMs..maxMs step minorTickMs.toLong()) {
            minorTick(timeMs)
        }
    }

    Layout(
        modifier = modifier,
        contents = listOf(
            majorTicks,
            minorTicks,
        ),
    ) { (majorTickMeasurables, minorTickMeasurables), constraints ->
        val tickWidthPx = tickWidth.roundToPx()
        val majorTickWidthPx = majorTickMs / minorTickMs * tickWidthPx

        val majorTickPlaceables = majorTickMeasurables.map { measurable ->
            measurable.measure(constraints.copy(minHeight = 0, minWidth = 0))
        }
        val minorTickPlaceables = minorTickMeasurables.map { measurable ->
            measurable.measure(constraints.copy(minHeight = 0, minWidth = 0))
        }
        val tickHeight = minorTickPlaceables.first().height

        val totalWidth = ((maxMs - minMs) / minorTickMs * tickWidth.toPx()).toInt()
        val layoutWidth = totalWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
        val totalHeight = (tickHeight * 2).coerceAtMost(constraints.maxHeight)


        val viewportWidth = constraints.maxWidth
        state.updateMaxScrollOffset(
            maxOf(0, totalWidth - viewportWidth)
        )

        layout(layoutWidth, totalHeight) {
            var xPosition = 0
            var yPosition = 0

            majorTickPlaceables.forEachIndexed() { index, placeable ->
                val minX = index * majorTickWidthPx
                val maxX = minX + majorTickWidthPx - placeable.width
                val xPos = when {
                    state.scrollOffset < minX -> minX
                    state.scrollOffset > maxX -> maxX
                    else -> state.scrollOffset
                }
                placeable.place(x = xPos - state.scrollOffset, y = yPosition)
                xPosition += majorTickWidthPx
            }

            xPosition = 0
            yPosition += majorTickPlaceables.first().height
            minorTickPlaceables.forEachIndexed() { index, placeable ->
                val xPos = index * tickWidthPx
                placeable.place(x = xPos - state.scrollOffset, y = yPosition)
                xPosition += majorTickWidthPx
            }

        }
    }
}


@Preview
@Composable
fun TimeTicksPreview() {
    MaeTheme {
        MaeBackground {
            rememberScrollState()
            Column {
                val scrollState = remember { TimeScrollState() }
                TimeTicks(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scrollable(
                            state = scrollState.scrollableState,
                            orientation = Orientation.Horizontal,
                        ),
                    minMs = 0,
                    maxMs = 240_000,
                    majorTickMs = 60_000,
                    minorTickMs = 5_000,
                    majorTick = { timeMs ->
                        MajorTick(formattedTime(timeMs, format = "HH:mm"))
                    },
                    minorTick = { timeMs ->
                        MinorTick(formattedTime(timeMs, format = ":ss"))
                    },
                    state = scrollState,
                )
            }
        }
    }
}

fun formattedTime(timeMs: Long, format: String = "HH:mm:ss"): String {
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    return dateFormat.format(timeMs)
}

@Composable
fun MajorTick(time: String) {
    val color = MaterialTheme.colorScheme.onSurface
    Text(
        text = time,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
fun MinorTick(time: String) {
    val color = MaterialTheme.colorScheme.onSurface
    Text(text = time,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = Modifier
            .drawBehind {
                drawLine(
                    color = color,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 1f
                )
            }
            .padding(8.dp))
}