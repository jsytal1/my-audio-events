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
    fun getMarkerIndicesInRange(labelIdx: Int, minMs: Long, maxMs: Long): Pair<Int, IntRange>
}

@Composable
internal fun lazyEventPlotItemProviderLambda(
    data: List<List<PlotEventInfo>>,
    minMs: Long,
    maxMs: Long,
    minorTickMs: Int,
    majorTickMs: Int,
    markerWidthMs: Int,
    minorTick: @Composable (timeMs: Long) -> Unit,
    majorTick: @Composable (timeMs: Long) -> Unit,
    label: @Composable (index: Int) -> Unit,
    marker: @Composable (labelIdx: Int, idx: Int) -> Unit,
): () -> LazyEventPlotItemProvider {
    val firstMinorTickMs = minMs / minorTickMs * minorTickMs
    val minorTickCount = (maxMs - firstMinorTickMs).toInt() / minorTickMs + 1
    val firstMajorTickMs = minMs / majorTickMs * majorTickMs
    val majorTickCount = (maxMs - firstMajorTickMs).toInt() / majorTickMs + 1

    return {
        LazyEventPlotItemProviderImpl(
            data = data,
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
    private val data: List<List<PlotEventInfo>>,
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
    private val marker: @Composable (labelIdx: Int, idx: Int) -> Unit,
) : LazyEventPlotItemProvider {

    private val majorTickIdxOffset = 0
    private val minorTickIdxOffset = majorTickCount
    private val labelIdxOffset = majorTickCount + minorTickCount
    private val labelCount = data.size
    private val markerIdxOffset = labelIdxOffset + labelCount
    private val markerIntervals = data.map { it.size }
    private val markerPrefixSums = markerIntervals.scan(0) { acc, i -> acc + i }
    private val markerCount = markerPrefixSums.last()

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
            val markerIdx = index - markerIdxOffset
            val labelSearchIdx = markerPrefixSums.binarySearch(markerIdx)
            val labelIdx = if (labelSearchIdx >= 0) labelSearchIdx else -labelSearchIdx - 2
            val relativeIdx = markerIdx - (markerPrefixSums.getOrNull(labelIdx) ?: 0)
            marker(labelIdx, relativeIdx)
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
            labelIdxOffset + key.removePrefix("L-").toInt()
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
        labelIdx: Int,
        minMs: Long,
        maxMs: Long
    ): Pair<Int, IntRange> {
        val events = data[labelIdx]
        val minIdx = events.binarySearchBy(minMs - markerWidthMs) { it.timeMs }
        val firstIndex = if (minIdx < 0) -minIdx - 1 else minIdx
        val maxIdx = events.binarySearchBy(maxMs, fromIndex = firstIndex) { it.timeMs }
        val lastIndex = if (maxIdx < 0) -maxIdx - 1 else maxIdx
        return Pair(
            markerIdxOffset + markerPrefixSums[labelIdx],
            firstIndex.coerceAtLeast(0) until lastIndex.coerceAtMost(events.size - 1)
        )
    }
}
