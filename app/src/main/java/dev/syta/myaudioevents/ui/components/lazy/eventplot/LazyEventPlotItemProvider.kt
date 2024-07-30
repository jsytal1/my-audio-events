package dev.syta.myaudioevents.ui.components.lazy.eventplot

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.getDefaultLazyLayoutKey
import androidx.compose.runtime.Composable

@OptIn(ExperimentalFoundationApi::class)
internal interface LazyEventPlotItemProvider : LazyLayoutItemProvider {
    fun getMinorTickIndicesInRange(minMs: Long, maxMs: Long): Iterable<Int>
}

@Composable
internal fun lazyEventPlotItemProviderLambda(
    minMs: Long,
    maxMs: Long,
    minorTick: @Composable (timeMs: Long) -> Unit,
    minorTickMs: Int,
): () -> LazyEventPlotItemProvider {
    val firstMinorTickMs = minMs / minorTickMs * minorTickMs
    val minorTickCount = (maxMs - firstMinorTickMs).toInt() / minorTickMs + 1

    return {
        LazyEventPlotItemProviderImpl(
            firstMinorTickMs = firstMinorTickMs,
            minorTickCount = minorTickCount,
            minorTickMs = minorTickMs,
            minorTick = minorTick,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private class LazyEventPlotItemProviderImpl(
    private val firstMinorTickMs: Long = 0,
    private val minorTickCount: Int = 0,
    private val minorTickMs: Int = 0,
    private val minorTick: @Composable (timeMs: Long) -> Unit = {},
) : LazyEventPlotItemProvider {
    override val itemCount: Int
        get() = minorTickCount

    @Composable
    override fun Item(index: Int, key: Any) {
        minorTick(firstMinorTickMs + index * minorTickMs)
    }

    override fun getContentType(index: Int): Any? = when {
        index < minorTickCount -> "MinorTick"
        else -> null
    }

    override fun getKey(index: Int): Any = when {
        index < minorTickCount -> {
            val tickMs = firstMinorTickMs + index * minorTickMs
            "MinorTick-$tickMs"
        }

        else -> getDefaultLazyLayoutKey(index)
    }

    override fun getIndex(key: Any): Int = when {
        key !is String -> -1
        key.startsWith("MinorTick-") -> {
            val timeMs = key.removePrefix("MinorTick-").toLong()
            ((timeMs - firstMinorTickMs) / minorTickMs).toInt()
        }

        else -> -1
    }

    override fun getMinorTickIndicesInRange(minMs: Long, maxMs: Long): Iterable<Int> {
        val firstIndex = ((minMs - firstMinorTickMs) / minorTickMs).toInt()
        val lastIndex = ((maxMs - firstMinorTickMs) / minorTickMs).toInt()
        return firstIndex..lastIndex
    }
}
