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
import dev.syta.myaudioevents.data.local.entities.EventType
import dev.syta.myaudioevents.designsystem.MaeBackground
import dev.syta.myaudioevents.ui.theme.MaeTheme
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyEventPlot(
    minMs: Long,
    maxMs: Long,
    data: List<List<PlotEventInfo>>,
    minorTick: @Composable (timeMs: Long) -> Unit,
    majorTick: @Composable (timeMs: Long) -> Unit,
    label: @Composable (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    minorTickMs: Int = 5_000,
    majorTickMs: Int = 60_000,
    minorTickWidth: Dp = 128.dp,
    state: LazyEventPlotState = rememberLazyEventPlotState(),
) {

    val itemProviderLambda = lazyEventPlotItemProviderLambda(
        data = data,
        minMs = minMs,
        maxMs = maxMs,
        minorTick = minorTick,
        majorTick = majorTick,
        label = label,
        minorTickMs = minorTickMs,
        majorTickMs = majorTickMs,
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
        var totalHeight = 0


        val majorTickWidthPx = minorTickWidthPx * majorTickMs / minorTickMs
        val majorTickIndices =
            itemProviderLambda().getMajorTickIndicesInRange(minVisibleMs, maxVisibleMs)
        val majorTickPlaceables = majorTickIndices.map { index ->
            measure(
                index, Constraints(0, majorTickWidthPx, 0)
            ).single()
        }
        val majorTickHeight = majorTickPlaceables.maxOf { it.height }
        totalHeight += majorTickHeight


        val minorTickIndices =
            itemProviderLambda().getMinorTickIndicesInRange(minVisibleMs, maxVisibleMs)
        val minorTickPlaceables = minorTickIndices.map { index ->
            measure(
                index, Constraints.fixedWidth(minorTickWidthPx)
            ).single()
        }
        val minorTickHeight = minorTickPlaceables.maxOf { it.height }
        totalHeight += minorTickHeight

        val labelIndices = itemProviderLambda().getLabelIndices()
        val labelPlaceables = labelIndices.map { index ->
            measure(
                index, Constraints()
            ).single()
        }
        val labelWidth = labelPlaceables.maxOf { it.width }
        totalHeight += labelPlaceables.sumOf { it.height }

        val totalWidth = minorTickWidthPx * (maxMs - minMs).toInt() / minorTickMs
        val layoutWidth = totalWidth.coerceAtMost(constraints.maxWidth)

        val viewportWidth = constraints.maxWidth
        state.updateMaxScrollOffset(
            maxOf(0, totalWidth - viewportWidth)
        )

        layout(layoutWidth, totalHeight) {
            var yPosition = 0
            val firstMajorTickOffset = -state.scrollOffset % majorTickWidthPx
            var xPosition = firstMajorTickOffset
            majorTickPlaceables.forEachIndexed { index, placeable ->
                if (index == 0) {
                    val maxX = firstMajorTickOffset + majorTickWidthPx - placeable.width
                    val stickyXPosition = 0.coerceAtMost(maxX)
                    placeable.place(x = stickyXPosition, y = yPosition)
                } else {
                    placeable.place(x = xPosition, y = yPosition)
                }
                xPosition += majorTickWidthPx
            }
            yPosition += majorTickHeight

            val firstMinorTickOffset = -state.scrollOffset % minorTickWidthPx
            xPosition = firstMinorTickOffset
            minorTickPlaceables.forEach { placeable ->
                placeable.place(x = xPosition, y = yPosition)
                xPosition += minorTickWidthPx
            }

            yPosition += minorTickHeight
            labelPlaceables.forEach { placeable ->
                placeable.place(
                    x = 0,
                    y = yPosition
                )
                yPosition += placeable.height
            }

        }
    }
}

data class PlotEventInfo(
    val timeMs: Long,
    val labelIdx: Int,
)

@Preview
@Composable
fun LazyEventPlotPreview() {

    val eventTypes = listOf(
        EventType(35, "Whistling"),
        EventType(396, "Whistle"),
        EventType(382, "Alarm"),
        EventType(0, "Speech"),
        EventType(67, "Animal"),
        EventType(106, "Bird"),
        EventType(103, "Wild animals"),
        EventType(500, "Inside, small room"),
        EventType(3, "Narration, monologue"),
        EventType(68, "Domestic animals, pets"),
    )


    MaeTheme {
        MaeBackground {
            Column {
                LazyEventPlot(
                    minMs = 0,
                    maxMs = 240_000,
                    data = List(eventTypes.size) { emptyList() },
                    minorTick = { timeMs ->
                        MinorTick(formattedTime(timeMs, ":ss"))
                    },
                    majorTick = { timeMs ->
                        MajorTick(formattedTime(timeMs, "HH:mm"))
                    },
                    label = { index ->
                        Label(eventTypes[index].name)
                    },
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
fun Label(text: String) {
    val color = MaterialTheme.colorScheme.onSurface
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier.padding(4.dp)
    )
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
            .padding(8.dp)
    )
}