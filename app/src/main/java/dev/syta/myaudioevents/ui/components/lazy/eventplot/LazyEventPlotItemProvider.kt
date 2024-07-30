package dev.syta.myaudioevents.ui.components.lazy.eventplot

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.getDefaultLazyLayoutKey
import androidx.compose.runtime.Composable

@OptIn(ExperimentalFoundationApi::class)
internal interface LazyEventPlotItemProvider : LazyLayoutItemProvider {
    fun getMajorTickIndicesInRange(minMs: Long, maxMs: Long): Iterable<Int>
    fun getMinorTickIndicesInRange(minMs: Long, maxMs: Long): Iterable<Int>
}

@Composable
internal fun lazyEventPlotItemProviderLambda(
    minMs: Long,
    maxMs: Long,
    minorTick: @Composable (timeMs: Long) -> Unit,
    majorTick: @Composable (timeMs: Long) -> Unit,
    minorTickMs: Int,
    majorTickMs: Int,
): () -> LazyEventPlotItemProvider {
    val firstMinorTickMs = minMs / minorTickMs * minorTickMs
    val minorTickCount = (maxMs - firstMinorTickMs).toInt() / minorTickMs + 1
    val firstMajorTickMs = minMs / majorTickMs * majorTickMs
    val majorTickCount = (maxMs - firstMajorTickMs).toInt() / majorTickMs + 1

    return {
        LazyEventPlotItemProviderImpl(
            firstMinorTickMs = firstMinorTickMs,
            minorTickCount = minorTickCount,
            minorTickMs = minorTickMs,
            minorTick = minorTick,
            firstMajorTickMs = firstMajorTickMs,
            majorTickCount = majorTickCount,
            majorTickMs = majorTickMs,
            majorTick = majorTick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private class LazyEventPlotItemProviderImpl(
    private val firstMinorTickMs: Long = 0,
    private val minorTickCount: Int = 0,
    private val minorTickMs: Int = 0,
    private val minorTick: @Composable (timeMs: Long) -> Unit = {},
    private val firstMajorTickMs: Long = 0,
    private val majorTickCount: Int = 0,
    private val majorTickMs: Int = 0,
    private val majorTick: @Composable (timeMs: Long) -> Unit = {},
) : LazyEventPlotItemProvider {
    override val itemCount: Int
        get() = minorTickCount + majorTickCount

    @Composable
    override fun Item(index: Int, key: Any) = when {
        index < majorTickCount -> {
            val timeMs = firstMajorTickMs + index * majorTickMs
            majorTick(timeMs)
        }

        index < majorTickCount + minorTickCount -> {
            val relativeIdx = index - majorTickCount
            val timeMs = firstMinorTickMs + relativeIdx * minorTickMs
            minorTick(timeMs)
        }

        else -> Unit
    }


    override fun getContentType(index: Int): Any? = when {
        index < majorTickCount -> "MajorTick"
        index < majorTickCount + minorTickCount -> "MinorTick"
        else -> null
    }

    override fun getKey(index: Int): Any = when {
        index < majorTickCount -> {
            val tickMs = firstMajorTickMs + index * majorTickMs
            "MajorTick-$tickMs"
        }

        index < minorTickCount -> {
            val tickMs = firstMinorTickMs + index * minorTickMs
            "MinorTick-$tickMs"
        }

        else -> getDefaultLazyLayoutKey(index)
    }

    override fun getIndex(key: Any): Int = when {
        key !is String -> -1
        key.startsWith("MajorTick-") -> {
            val timeMs = key.removePrefix("MajorTick-").toLong()
            ((timeMs - firstMinorTickMs) / minorTickMs).toInt()
        }

        key.startsWith("MinorTick-") -> {
            val timeMs = key.removePrefix("MinorTick-").toLong()
            majorTickCount + ((timeMs - firstMinorTickMs) / minorTickMs).toInt()
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
}
