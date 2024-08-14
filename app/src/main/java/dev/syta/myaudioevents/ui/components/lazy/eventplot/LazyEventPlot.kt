package dev.syta.myaudioevents.ui.components.lazy.eventplot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
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
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyEventPlot(
    minMs: Long,
    eventCount: () -> Int,
    dataProvider: (index: Int) -> PlotEventInfo,
    labelCount: () -> Int,
    minorTick: @Composable (timeMs: Long) -> Unit,
    majorTick: @Composable (timeMs: Long) -> Unit,
    label: @Composable (index: Int) -> Unit,
    marker: @Composable (idx: Int) -> Unit,
    modifier: Modifier = Modifier,
    maxMsLambda: () -> Long = { System.currentTimeMillis() + 2_000 },
    minorTickMs: Int = 5_000,
    majorTickMs: Int = 60_000,
    minorTickWidth: Dp = 128.dp,
    markerWidthMs: Int = 975,
    markerHeight: Dp = 32.dp,
    state: LazyEventPlotState = rememberLazyEventPlotState(minMs = minMs),
    liveScroll: Boolean = false,
) {
    var maxMs by remember { mutableLongStateOf(minMs + 60_000) }

    LaunchedEffect(liveScroll) {
        while (liveScroll) {
            val newMaxMs = maxMsLambda()
            if (newMaxMs + 30_000 > maxMs) {
                maxMs += 30_000
            }
            state.animateScrollToMsRight(maxMsLambda(), 1_000)
        }
    }

    val itemProviderLambda = lazyEventPlotItemProviderLambda(
        eventCount = eventCount,
        labelCount = labelCount,
        dataProvider = dataProvider,
        minMs = minMs,
        maxMs = maxMs,
        minorTick = minorTick,
        majorTick = majorTick,
        label = label,
        marker = marker,
        minorTickMs = minorTickMs,
        majorTickMs = majorTickMs,
        markerWidthMs = markerWidthMs,
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
        val labelPlaceables = labelIndices.second.map { labelIdx ->
            measure(
                labelIndices.first + labelIdx, Constraints()
            ).single()
        }
        totalHeight += labelPlaceables.sumOf { it.height + markerHeight.roundToPx() }

        val (markerOffset, markerIndices) = itemProviderLambda().getMarkerIndicesInRange(
            minVisibleMs, maxVisibleMs
        )
        val markerPlaceables = markerIndices.map { idx ->
            val markerStartMs = dataProvider(idx).timeMs
            val markerEndMs = markerStartMs + markerWidthMs
            val leftPx = ((markerStartMs - minVisibleMs) / msPerPx).toInt()
            val rightPx = ((markerEndMs - minVisibleMs) / msPerPx).toInt()
            val widthPx = rightPx - leftPx
            val placeable = measure(
                markerOffset + idx, Constraints.fixed(widthPx, markerHeight.roundToPx())
            ).single()
            Pair(idx, placeable)
        }

        val totalWidth = minorTickWidthPx * (maxMs - minMs).toInt() / minorTickMs
        val layoutWidth = totalWidth.coerceAtMost(constraints.maxWidth)

        val viewportWidth = constraints.maxWidth
        state.updateMaxScrollOffset(
            maxOf(0, totalWidth - viewportWidth)
        )
        state.msPerPx = msPerPx
        state.width = layoutWidth

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
            val labelPositions = mutableMapOf<Int, Int>()
            labelPlaceables.forEachIndexed { index, placeable ->
                placeable.place(
                    x = 0, y = yPosition
                )
                labelPositions[index] = yPosition
                yPosition += placeable.height + markerHeight.roundToPx()
            }
            var maxMarkerHeight = 0
            markerPlaceables.forEach { (eventIdx, markerPlaceable) ->
                val timeMs = dataProvider(eventIdx).timeMs
                val xPos = ((timeMs - minVisibleMs) / msPerPx).toInt()
                maxMarkerHeight = max(maxMarkerHeight, markerPlaceable.height)
                val labelIdx = dataProvider(eventIdx).labelIdx
                markerPlaceable.place(
                    x = xPos, y = labelPositions[labelIdx]!! + labelPlaceables[labelIdx].height
                )
            }

        }
    }
}

data class PlotEventInfo(
    val timeMs: Long,
    val labelIdx: Int,
    val score: Float,
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

    val eventData = listOf(
//        listOf(
        PlotEventInfo(800, 0, 0.002f),
        PlotEventInfo(900, 0, 0.050f),
        PlotEventInfo(1000, 0, 0.345f),
        PlotEventInfo(1100, 0, 0.820f),
        PlotEventInfo(1200, 0, 0.264f),
        PlotEventInfo(1300, 0, 0.721f),
        PlotEventInfo(1400, 0, 0.650f),
        PlotEventInfo(1500, 0, 0.961f),
        PlotEventInfo(1600, 0, 0.828f),
        PlotEventInfo(1700, 0, 0.713f),
        PlotEventInfo(1800, 0, 0.957f),
        PlotEventInfo(1900, 0, 0.985f),
        PlotEventInfo(2000, 0, 0.993f),
        PlotEventInfo(2100, 0, 0.982f),
        PlotEventInfo(2200, 0, 0.989f),
        PlotEventInfo(2300, 0, 0.833f),
        PlotEventInfo(2400, 0, 0.630f),
        PlotEventInfo(2500, 0, 0.739f),
        PlotEventInfo(2600, 0, 0.406f),
        PlotEventInfo(2700, 0, 0.892f),
        PlotEventInfo(2800, 0, 0.285f),
        PlotEventInfo(2900, 0, 0.198f),
        PlotEventInfo(3000, 0, 0.037f),
        PlotEventInfo(3100, 0, 0.692f),
        PlotEventInfo(3200, 0, 0.475f),
        PlotEventInfo(3300, 0, 0.122f),
        PlotEventInfo(3400, 0, 0.519f),
        PlotEventInfo(3500, 0, 0.489f),
        PlotEventInfo(3600, 0, 0.874f),
        PlotEventInfo(3700, 0, 0.918f),
        PlotEventInfo(3800, 0, 0.882f),
        PlotEventInfo(3900, 0, 0.438f),
        PlotEventInfo(4000, 0, 0.509f),
//        ),
//        listOf(
        PlotEventInfo(1000, 1, 0.004f),
        PlotEventInfo(1100, 1, 0.321f),
        PlotEventInfo(1200, 1, 0.634f),
        PlotEventInfo(1300, 1, 0.454f),
        PlotEventInfo(1400, 1, 0.773f),
        PlotEventInfo(1500, 1, 0.579f),
        PlotEventInfo(1600, 1, 0.758f),
        PlotEventInfo(1700, 1, 0.658f),
        PlotEventInfo(1800, 1, 0.196f),
        PlotEventInfo(1900, 1, 0.005f),
        PlotEventInfo(2000, 1, 0.001f),
        PlotEventInfo(2100, 1, 0.053f),
        PlotEventInfo(2200, 1, 0.110f),
        PlotEventInfo(2300, 1, 0.809f),
        PlotEventInfo(2400, 1, 0.593f),
        PlotEventInfo(2500, 1, 0.280f),
        PlotEventInfo(2600, 1, 0.701f),
        PlotEventInfo(2700, 1, 0.022f),
        PlotEventInfo(2800, 1, 0.100f),
        PlotEventInfo(2900, 1, 0.242f),
        PlotEventInfo(3000, 1, 0.848f),
        PlotEventInfo(3100, 1, 0.017f),
        PlotEventInfo(3200, 1, 0.733f),
        PlotEventInfo(3300, 1, 0.821f),
        PlotEventInfo(3400, 1, 0.873f),
        PlotEventInfo(3500, 1, 0.780f),
        PlotEventInfo(3600, 1, 0.582f),
        PlotEventInfo(3700, 1, 0.680f),
        PlotEventInfo(3800, 1, 0.894f),
        PlotEventInfo(3900, 1, 0.952f),
        PlotEventInfo(4000, 1, 0.899f),
//        ),
//        listOf(
        PlotEventInfo(800, 2, 0.002f),
        PlotEventInfo(1000, 2, 0.003f),
        PlotEventInfo(1100, 2, 0.112f),
        PlotEventInfo(1200, 2, 0.353f),
        PlotEventInfo(1300, 2, 0.118f),
        PlotEventInfo(1400, 2, 0.386f),
        PlotEventInfo(1500, 2, 0.193f),
        PlotEventInfo(1600, 2, 0.399f),
        PlotEventInfo(1700, 2, 0.353f),
        PlotEventInfo(1800, 2, 0.074f),
        PlotEventInfo(1900, 2, 0.009f),
        PlotEventInfo(2000, 2, 0.003f),
        PlotEventInfo(2100, 2, 0.047f),
        PlotEventInfo(2200, 2, 0.084f),
        PlotEventInfo(2300, 2, 0.765f),
        PlotEventInfo(2400, 2, 0.472f),
        PlotEventInfo(2500, 2, 0.311f),
        PlotEventInfo(2600, 2, 0.732f),
        PlotEventInfo(2700, 2, 0.109f),
        PlotEventInfo(2800, 2, 0.335f),
        PlotEventInfo(2900, 2, 0.470f),
        PlotEventInfo(3000, 2, 0.906f),
        PlotEventInfo(3100, 2, 0.108f),
        PlotEventInfo(3200, 2, 0.651f),
        PlotEventInfo(3300, 2, 0.806f),
        PlotEventInfo(3400, 2, 0.794f),
        PlotEventInfo(3500, 2, 0.812f),
        PlotEventInfo(3600, 2, 0.524f),
        PlotEventInfo(3700, 2, 0.365f),
        PlotEventInfo(3800, 2, 0.578f),
        PlotEventInfo(3900, 2, 0.823f),
        PlotEventInfo(4000, 2, 0.730f),
//        ),
//        listOf(
        PlotEventInfo(0, 3, 0.986f),
        PlotEventInfo(100, 3, 0.999f),
        PlotEventInfo(200, 3, 1.000f),
        PlotEventInfo(300, 3, 0.999f),
        PlotEventInfo(400, 3, 1.000f),
        PlotEventInfo(500, 3, 1.000f),
        PlotEventInfo(600, 3, 0.999f),
        PlotEventInfo(700, 3, 0.997f),
        PlotEventInfo(800, 3, 0.689f),
        PlotEventInfo(900, 3, 0.195f),
        PlotEventInfo(1000, 3, 0.014f),
        PlotEventInfo(1100, 3, 0.005f),
        PlotEventInfo(1200, 3, 0.006f),
        PlotEventInfo(1300, 3, 0.005f),
        PlotEventInfo(1400, 3, 0.008f),
        PlotEventInfo(1500, 3, 0.003f),
        PlotEventInfo(1600, 3, 0.001f),
//        ),
//        listOf(
        PlotEventInfo(800, 4, 0.001f),
        PlotEventInfo(900, 4, 0.068f),
        PlotEventInfo(1000, 4, 0.369f),
        PlotEventInfo(1100, 4, 0.088f),
        PlotEventInfo(1200, 4, 0.087f),
        PlotEventInfo(1300, 4, 0.162f),
        PlotEventInfo(1400, 4, 0.083f),
        PlotEventInfo(1500, 4, 0.005f),
        PlotEventInfo(1600, 4, 0.004f),
        PlotEventInfo(1700, 4, 0.003f),
        PlotEventInfo(1800, 4, 0.004f),
        PlotEventInfo(2400, 4, 0.014f),
        PlotEventInfo(3200, 4, 0.001f),
        PlotEventInfo(3600, 4, 0.013f),
        PlotEventInfo(4000, 4, 0.003f),
//        ),
//        listOf(
        PlotEventInfo(800, 5, 0.001f),
        PlotEventInfo(900, 5, 0.105f),
        PlotEventInfo(1000, 5, 0.436f),
        PlotEventInfo(1100, 5, 0.078f),
        PlotEventInfo(1200, 5, 0.017f),
        PlotEventInfo(1300, 5, 0.090f),
        PlotEventInfo(1400, 5, 0.061f),
        PlotEventInfo(1500, 5, 0.007f),
        PlotEventInfo(1600, 5, 0.004f),
        PlotEventInfo(1700, 5, 0.003f),
        PlotEventInfo(1800, 5, 0.007f),
        PlotEventInfo(2400, 5, 0.009f),
        PlotEventInfo(3600, 5, 0.018f),
        PlotEventInfo(4000, 5, 0.004f),
//        ),
//        listOf(
        PlotEventInfo(900, 6, 0.079f),
        PlotEventInfo(1000, 6, 0.396f),
        PlotEventInfo(1100, 6, 0.070f),
        PlotEventInfo(1200, 6, 0.023f),
        PlotEventInfo(1300, 6, 0.099f),
        PlotEventInfo(1400, 6, 0.057f),
        PlotEventInfo(1500, 6, 0.005f),
        PlotEventInfo(1600, 6, 0.002f),
        PlotEventInfo(1700, 6, 0.002f),
        PlotEventInfo(1800, 6, 0.004f),
        PlotEventInfo(2400, 6, 0.004f),
        PlotEventInfo(3600, 6, 0.011f),
        PlotEventInfo(4000, 6, 0.003f),
//        ),
//        listOf(
        PlotEventInfo(0, 7, 0.009f),
        PlotEventInfo(100, 7, 0.001f),
        PlotEventInfo(300, 7, 0.003f),
        PlotEventInfo(500, 7, 0.002f),
        PlotEventInfo(600, 7, 0.003f),
        PlotEventInfo(700, 7, 0.011f),
        PlotEventInfo(800, 7, 0.064f),
        PlotEventInfo(900, 7, 0.090f),
        PlotEventInfo(1000, 7, 0.033f),
        PlotEventInfo(1100, 7, 0.029f),
        PlotEventInfo(1200, 7, 0.067f),
        PlotEventInfo(1300, 7, 0.037f),
        PlotEventInfo(1400, 7, 0.063f),
        PlotEventInfo(1500, 7, 0.031f),
        PlotEventInfo(1600, 7, 0.019f),
        PlotEventInfo(1700, 7, 0.012f),
        PlotEventInfo(1800, 7, 0.013f),
        PlotEventInfo(1900, 7, 0.002f),
        PlotEventInfo(2000, 7, 0.003f),
        PlotEventInfo(2100, 7, 0.001f),
        PlotEventInfo(2200, 7, 0.003f),
        PlotEventInfo(2300, 7, 0.005f),
        PlotEventInfo(2400, 7, 0.023f),
        PlotEventInfo(2500, 7, 0.005f),
        PlotEventInfo(2600, 7, 0.002f),
        PlotEventInfo(2900, 7, 0.002f),
        PlotEventInfo(3000, 7, 0.002f),
        PlotEventInfo(3100, 7, 0.002f),
        PlotEventInfo(3200, 7, 0.005f),
        PlotEventInfo(3300, 7, 0.002f),
        PlotEventInfo(3400, 7, 0.002f),
        PlotEventInfo(3500, 7, 0.003f),
        PlotEventInfo(3600, 7, 0.016f),
        PlotEventInfo(3700, 7, 0.005f),
        PlotEventInfo(3800, 7, 0.003f),
        PlotEventInfo(3900, 7, 0.002f),
        PlotEventInfo(4000, 7, 0.009f),
//        ),
//        listOf(
        PlotEventInfo(0, 8, 0.060f),
        PlotEventInfo(100, 8, 0.067f),
        PlotEventInfo(200, 8, 0.079f),
        PlotEventInfo(300, 8, 0.043f),
        PlotEventInfo(400, 8, 0.121f),
        PlotEventInfo(500, 8, 0.055f),
        PlotEventInfo(600, 8, 0.055f),
        PlotEventInfo(700, 8, 0.037f),
        PlotEventInfo(800, 8, 0.002f),
//        ),
//        listOf(
        PlotEventInfo(800, 9, 0.002f),
        PlotEventInfo(900, 9, 0.012f),
        PlotEventInfo(1000, 9, 0.027f),
        PlotEventInfo(1100, 9, 0.016f),
        PlotEventInfo(1200, 9, 0.043f),
        PlotEventInfo(1300, 9, 0.020f),
        PlotEventInfo(1400, 9, 0.015f),
        PlotEventInfo(1500, 9, 0.003f),
        PlotEventInfo(1600, 9, 0.001f),
        PlotEventInfo(2400, 9, 0.002f),
//        ),
    ).sortedBy { it.timeMs }

    MaeTheme {
        MaeBackground {
            Column {
                LazyEventPlot(minMs = 0,
                    maxMsLambda = { 4_960 },
                    minorTickMs = 1_000,
                    markerWidthMs = 100,
                    minorTickWidth = 128.dp,
                    majorTickMs = 60_000,
                    dataProvider = { index: Int -> eventData[index] },
                    eventCount = { eventData.size },
                    labelCount = { eventTypes.size },
                    minorTick = { timeMs ->
                        MinorTick(formattedTime(timeMs, ":ss"))
                    },
                    majorTick = { timeMs ->
                        MajorTick(formattedTime(timeMs, "HH:mm"))
                    },
                    label = { index ->
                        Label(eventTypes[index].name)
                    },
                    marker = { idx ->
                        Marker(alpha = eventData[idx].score)
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
fun Marker(alpha: Float) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        drawRoundRect(
            color = color.copy(alpha = alpha), size = size, cornerRadius = CornerRadius(8.dp.toPx())
        )
    }
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
            .padding(8.dp))
}