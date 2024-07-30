package dev.syta.myaudioevents.ui.components.lazy.eventplot

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.getDefaultLazyLayoutKey
import androidx.compose.runtime.Composable

@OptIn(ExperimentalFoundationApi::class)
internal interface LazyEventPlotItemProvider : LazyLayoutItemProvider {
    fun getMajorTickIndicesInRange(minMs: Long, maxMs: Long): Iterable<Int>
    fun getMinorTickIndicesInRange(minMs: Long, maxMs: Long): Iterable<Int>
    fun getLabelIndices(): Iterable<Int>
}

@Composable
internal fun lazyEventPlotItemProviderLambda(
    data: List<List<PlotEventInfo>>,
    minMs: Long,
    maxMs: Long,
    minorTickMs: Int,
    majorTickMs: Int,
    minorTick: @Composable (timeMs: Long) -> Unit,
    majorTick: @Composable (timeMs: Long) -> Unit,
    label: @Composable (index: Int) -> Unit,
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
            minorTick = minorTick,
            majorTick = majorTick,
            label = label,
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
    private val minorTick: @Composable (timeMs: Long) -> Unit = {},
    private val majorTick: @Composable (timeMs: Long) -> Unit = {},
    private val label: @Composable (index: Int) -> Unit = {},
) : LazyEventPlotItemProvider {

    private val majorTickIdxOffset = 0
    private val minorTickIdxOffset = majorTickCount
    private val labelIdxOffset = majorTickCount + minorTickCount
    private val labelCount = data.size

    override val itemCount: Int
        get() = minorTickCount + majorTickCount + labelCount

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

        index < itemCount -> {
            val relativeIdx = index - labelIdxOffset
            label(relativeIdx)
        }

        else -> Unit
    }


    override fun getContentType(index: Int): Any? = when {
        index < minorTickIdxOffset -> "MajorTick"
        index < labelIdxOffset -> "MinorTick"
        index < itemCount -> "Label"
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

        index < itemCount -> "Label-$index"

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

        else -> -1
    }

    override fun getMajorTickIndicesInRange(minMs: Long, maxMs: Long): Iterable<Int> {
        val firstIndex = ((minMs - firstMajorTickMs) / majorTickMs).toInt()
        val lastIndex = ((maxMs - firstMajorTickMs) / majorTickMs).toInt()
        return firstIndex..lastIndex
    }

    override fun getMinorTickIndicesInRange(minMs: Long, maxMs: Long): Iterable<Int> {
        val firstIndex = majorTickCount + ((minMs - firstMinorTickMs) / minorTickMs).toInt()
        val lastIndex = majorTickCount + ((maxMs - firstMinorTickMs) / minorTickMs).toInt()
        return firstIndex..lastIndex
    }

    override fun getLabelIndices(): Iterable<Int> = labelIdxOffset until itemCount
}
