package onlymash.flexbooru.widget

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class AutoStaggeredGridLayoutManager(columnSize: Int, orientation: Int) : StaggeredGridLayoutManager(1, orientation) {

    companion object {

        const val STRATEGY_MIN_SIZE = 0
        const val STRATEGY_SUITABLE_SIZE = 1

        fun getSpanCountForSuitableSize(total: Int, single: Int): Int {
            val span = total / single
            if (span <= 0) {
                return 1
            }
            val span2 = span + 1
            val deviation = Math.abs(1 - total.toFloat() / span.toFloat() / single.toFloat())
            val deviation2 = Math.abs(1 - total.toFloat() / span2.toFloat() / single.toFloat())
            return if (deviation < deviation2) span else span2
        }

        fun getSpanCountForMinSize(total: Int, single: Int): Int {
            return Math.max(1, total / single)
        }
    }

    private var mColumnSize = -1
    private var mColumnSizeChanged = true
    private var mStrategy: Int = 0

    private var mListeners: MutableList<OnUpdateSpanCountListener>? = null

    init {
        setColumnSize(columnSize)
    }

    fun setColumnSize(columnSize: Int) {
        if (columnSize == mColumnSize) {
            return
        }
        mColumnSize = columnSize
        mColumnSizeChanged = true
    }

    fun setStrategy(strategy: Int) {
        if (strategy == mStrategy) {
            return
        }
        mStrategy = strategy
        mColumnSizeChanged = true
    }

    override fun onMeasure(recycler: RecyclerView.Recycler, state: RecyclerView.State, widthSpec: Int, heightSpec: Int) {
        if (mColumnSizeChanged && mColumnSize > 0) {
            val totalSpace: Int = if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if (View.MeasureSpec.EXACTLY != View.MeasureSpec.getMode(widthSpec)) {
                    throw IllegalStateException("RecyclerView need a fixed width for AutoStaggeredGridLayoutManager")
                }
                View.MeasureSpec.getSize(widthSpec) - paddingRight - paddingLeft
            } else {
                if (View.MeasureSpec.EXACTLY != View.MeasureSpec.getMode(heightSpec)) {
                    throw IllegalStateException("RecyclerView need a fixed height for AutoStaggeredGridLayoutManager")
                }
                View.MeasureSpec.getSize(heightSpec) - paddingTop - paddingBottom
            }

            val spanCount: Int = when (mStrategy) {
                STRATEGY_MIN_SIZE -> getSpanCountForMinSize(totalSpace, mColumnSize)
                STRATEGY_SUITABLE_SIZE -> getSpanCountForSuitableSize(totalSpace, mColumnSize)
                else -> getSpanCountForMinSize(totalSpace, mColumnSize)
            }
            setSpanCount(spanCount)
            mColumnSizeChanged = false

            if (null != mListeners) {
                var i = 0
                val n = mListeners!!.size
                while (i < n) {
                    mListeners!![i].onUpdateSpanCount(spanCount)
                    i++
                }
            }
        }
        super.onMeasure(recycler, state, widthSpec, heightSpec)
    }

    fun addOnUpdateSpanCountListener(listener: OnUpdateSpanCountListener) {
        if (null == mListeners) {
            mListeners = mutableListOf()
        }
        mListeners!!.add(listener)
    }

    fun removeOnUpdateSpanCountListener(listener: OnUpdateSpanCountListener) {
        if (null != mListeners) {
            mListeners!!.remove(listener)
        }
    }

    interface OnUpdateSpanCountListener {
        fun onUpdateSpanCount(spanCount: Int)
    }
}