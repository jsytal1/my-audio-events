package dev.syta.myaudioevents.ui.components.lazy.eventplot

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.syta.myaudioevents.designsystem.MaeBackground
import dev.syta.myaudioevents.ui.theme.MaeTheme
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyEventPlot(
    minMs: Long,
    maxMs: Long,
    minorTick: @Composable (timeMs: Long) -> Unit,
    modifier: Modifier = Modifier,
    minorTickMs: Int = 5_000,
    minorTickWidth: Dp = 128.dp,
    state: LazyEventPlotState = rememberLazyEventPlotState(),
) {

    val itemProviderLambda = lazyEventPlotItemProviderLambda(
        minMs,
        maxMs,
        minorTick,
        minorTickMs,
    )

    LazyLayout(
        itemProvider = itemProviderLambda,
        modifier
            .clipScrollableContainer(Orientation.Horizontal)
            .scrollable(state.scrollableState, Orientation.Horizontal)
    ) { constraints ->
        val minorTickWidthPx = minorTickWidth.roundToPx()
        val msPerPx = minorTickMs.toDouble() / minorTickWidthPx
        val visibleMs = (constraints.maxWidth * msPerPx).toInt()

        val minVisibleMs = (minMs + state.scrollOffset * msPerPx).toLong().coerceIn(minMs, maxMs)
        val maxVisibleMs = minVisibleMs + visibleMs
        val minorTickIndices =
            itemProviderLambda().getMinorTickIndicesInRange(minVisibleMs, maxVisibleMs)
        val minorTickPlaceables = minorTickIndices.map { index ->
            measure(
                index, Constraints.fixedWidth(minorTickWidthPx)
            ).single()
        }

        val tickHeight = minorTickPlaceables.maxOf { it.height }

        val totalWidth = minorTickWidthPx * (maxMs - minMs).toInt() / minorTickMs
        val layoutWidth = totalWidth.coerceAtMost(constraints.maxWidth)

        val viewportWidth = constraints.maxWidth
        state.updateMaxScrollOffset(
            maxOf(0, totalWidth - viewportWidth)
        )

        layout(layoutWidth, tickHeight) {

            val firstMinorTickOffset = -state.scrollOffset % minorTickWidthPx
            var xPosition = firstMinorTickOffset
            minorTickPlaceables.forEach { placeable ->
                placeable.place(x = xPosition, y = 0)
                xPosition += minorTickWidthPx
            }

        }
    }
}


@Preview
@Composable
fun LazyEventPlotPreview() {
    MaeTheme {
        MaeBackground {
            Column {
                LazyEventPlot(minMs = 0, maxMs = 240_000, minorTick = { timeMs ->
                    MinorTick(formattedTime(timeMs, ":ss"))
                })
            }
        }
    }
}

fun formattedTime(timeMs: Long, format: String = "HH:mm:ss"): String {
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    return dateFormat.format(timeMs)
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