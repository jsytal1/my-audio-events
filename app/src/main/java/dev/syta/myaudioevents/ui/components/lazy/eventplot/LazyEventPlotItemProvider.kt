package dev.syta.myaudioevents.ui.components.lazy.eventplot

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.getDefaultLazyLayoutKey
import androidx.compose.runtime.Composable

@OptIn(ExperimentalFoundationApi::class)
internal interface LazyEventPlotItemProvider : LazyLayoutItemProvider {
    fun getMajorTickIndicesInRange(minMs: Long, maxMs: Long): Iterable<Int>
    fun getMinorTickIndicesInRange(minMs: Long, maxMs: Long): Iterable<Int>
    fun getLabelIndices(): Pair<Int, Iterable<Int>>
    fun getMarkerIndicesInRange(minMs: Long, maxMs: Long): Pair<Int, IntRange>
}

@Composable
internal fun lazyEventPlotItemProviderLambda(
    eventCount: Int,
    labelCount: Int,
    dataProvider: (eventIdx: Int) -> PlotEventInfo,
    minMs: Long,
    maxMs: Long,
    minorTickMs: Int,
    majorTickMs: Int,
    markerWidthMs: Int,
    minorTick: @Composable (timeMs: Long) -> Unit,
    majorTick: @Composable (timeMs: Long) -> Unit,
    label: @Composable (index: Int) -> Unit,
    marker: @Composable (idx: Int) -> Unit,
): () -> LazyEventPlotItemProvider {
    val firstMinorTickMs = minMs / minorTickMs * minorTickMs
    val minorTickCount = (maxMs - firstMinorTickMs).toInt() / minorTickMs + 1
    val firstMajorTickMs = minMs / majorTickMs * majorTickMs
    val majorTickCount = (maxMs - firstMajorTickMs).toInt() / majorTickMs + 1

    return {
        LazyEventPlotItemProviderImpl(
            eventCount = eventCount,
            labelCount = labelCount,
            dataProvider = dataProvider,
            firstMinorTickMs = firstMinorTickMs,
            firstMajorTickMs = firstMajorTickMs,
            minorTickCount = minorTickCount,
            majorTickCount = majorTickCount,
            minorTickMs = minorTickMs,
            majorTickMs = majorTickMs,
            markerWidthMs = markerWidthMs,
            minorTick = minorTick,
            majorTick = majorTick,
            label = label,
            marker = marker,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private class LazyEventPlotItemProviderImpl(
    eventCount: Int,
    private val labelCount: Int,
    private val dataProvider: (eventIdx: Int) -> PlotEventInfo,
    private val firstMinorTickMs: Long = 0,
    private val firstMajorTickMs: Long = 0,
    private val minorTickCount: Int = 0,
    private val majorTickCount: Int = 0,
    private val minorTickMs: Int = 0,
    private val majorTickMs: Int = 0,
    private val markerWidthMs: Int = 0,
    private val minorTick: @Composable (timeMs: Long) -> Unit = {},
    private val majorTick: @Composable (timeMs: Long) -> Unit = {},
    private val label: @Composable (index: Int) -> Unit = {},
    private val marker: @Composable (idx: Int) -> Unit,
) : LazyEventPlotItemProvider {

    private val majorTickIdxOffset = 0
    private val minorTickIdxOffset = majorTickCount
    private val labelIdxOffset = majorTickCount + minorTickCount
    private val markerIdxOffset = labelIdxOffset + labelCount
    private val markerCount = eventCount

    override val itemCount: Int
        get() = minorTickCount + majorTickCount + labelCount + markerCount

    @Composable
    override fun Item(index: Int, key: Any) = when {
        index < majorTickCount -> {
            val relativeIdx = index - majorTickIdxOffset
            val timeMs = firstMajorTickMs + relativeIdx * majorTickMs
            majorTick(timeMs)
        }

        index < labelIdxOffset -> {
            val relativeIdx = index - minorTickIdxOffset
            val timeMs = firstMinorTickMs + relativeIdx * minorTickMs
            minorTick(timeMs)
        }

        index < markerIdxOffset -> {
            val relativeIdx = index - labelIdxOffset
            label(relativeIdx)
        }

        index < itemCount -> {
            val relativeIdx = index - markerIdxOffset
            marker(relativeIdx)
        }

        else -> Unit
    }


    override fun getContentType(index: Int): Any? = when {
        index < minorTickIdxOffset -> "MajorTick"
        index < labelIdxOffset -> "MinorTick"
        index < markerIdxOffset -> "Label"
        index < itemCount -> "Marker"
        else -> null
    }

    override fun getKey(index: Int): Any = when {
        index < minorTickIdxOffset -> {
            val tickMs = firstMajorTickMs + index * majorTickMs
            "MajorTick-$tickMs"
        }

        index < labelIdxOffset -> {
            val tickMs = firstMinorTickMs + index * minorTickMs
            "MinorTick-$tickMs"
        }

        index < markerIdxOffset -> {
            val relativeIdx = index - labelIdxOffset
            "Label-$relativeIdx"
        }

        index < itemCount -> {
            val relativeIdx = index - labelIdxOffset
            "Marker-$relativeIdx"
        }

        else -> getDefaultLazyLayoutKey(index)
    }

    override fun getIndex(key: Any): Int = when {
        key !is String -> -1
        key.startsWith("MajorTick-") -> {
            val timeMs = key.removePrefix("MajorTick-").toLong()
            majorTickIdxOffset + ((timeMs - firstMinorTickMs) / minorTickMs).toInt()
        }

        key.startsWith("MinorTick-") -> {
            val timeMs = key.removePrefix("MinorTick-").toLong()
            minorTickIdxOffset + ((timeMs - firstMinorTickMs) / minorTickMs).toInt()
        }

        key.startsWith("Label-") -> {
            labelIdxOffset + key.removePrefix("Label-").toInt()
        }

        key.startsWith("Marker-") -> {
            val relativeIdx = key.removePrefix("Marker-").toInt()
            markerIdxOffset + relativeIdx
        }

        else -> -1
    }

    override fun getMajorTickIndicesInRange(minMs: Long, maxMs: Long): Iterable<Int> {
        val firstIndex = ((minMs - firstMajorTickMs) / majorTickMs).toInt()
        val lastIndex = ((maxMs - firstMajorTickMs) / majorTickMs).toInt()
        return firstIndex.coerceAtLeast(majorTickIdxOffset)..lastIndex.coerceAtMost(
            minorTickIdxOffset - 1
        )
    }

    override fun getMinorTickIndicesInRange(minMs: Long, maxMs: Long): Iterable<Int> {
        val firstIndex = minorTickIdxOffset + ((minMs - firstMinorTickMs) / minorTickMs).toInt()
        val lastIndex = minorTickIdxOffset + ((maxMs - firstMinorTickMs) / minorTickMs).toInt()
        return firstIndex.coerceAtLeast(minorTickIdxOffset)..lastIndex.coerceAtMost(labelIdxOffset - 1)
    }

    override fun getLabelIndices() = Pair(labelIdxOffset, 0 until labelCount)

    override fun getMarkerIndicesInRange(
        minMs: Long, maxMs: Long
    ): Pair<Int, IntRange> {

        val minIdx = binarySearchOnVirtualList(
            0,
            markerCount - 1,
            minMs - markerWidthMs,
        ) { dataProvider(it).timeMs }
        val firstIndex = if (minIdx < 0) -minIdx - 1 else minIdx
        val maxIdx = binarySearchOnVirtualList(
            firstIndex,
            markerCount - 1,
            maxMs,
        ) { dataProvider(it).timeMs }
        val lastIndex = if (maxIdx < 0) -maxIdx - 1 else maxIdx
        return Pair(
            markerIdxOffset,
            firstIndex.coerceAtLeast(0) until lastIndex.coerceAtMost(markerCount - 1)
        )
    }
}

fun binarySearchOnVirtualList(
    low: Int, high: Int, target: Long, valueAtIndex: (index: Int) -> Long
): Int {
    var start = low
    var end = high
    while (start <= end) {
        val mid = start + (end - start) / 2
        val value = valueAtIndex(mid)

        if (value == target) {
            return mid // Target found
        } else if (value < target) {
            start = mid + 1
        } else {
            end = mid - 1
        }
    }
    return start // Position where target should be if it's not found
}
