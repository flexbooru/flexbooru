/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

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

    private var columnSize = -1
    private var columnSizeChanged = true
    private var strategy: Int = 0

    private var listeners: MutableList<OnUpdateSpanCountListener>? = null

    init {
        setColumnSize(columnSize)
    }

    private fun setColumnSize(columnSize: Int) {
        if (this.columnSize == columnSize) {
            return
        }
        this.columnSize = columnSize
        columnSizeChanged = true
    }

    fun setStrategy(strategy: Int) {
        if (this.strategy == strategy) {
            return
        }
        this.strategy = strategy
        columnSizeChanged = true
    }

    override fun onMeasure(recycler: RecyclerView.Recycler, state: RecyclerView.State, widthSpec: Int, heightSpec: Int) {
        if (columnSizeChanged && columnSize > 0) {
            val totalSpace: Int = if (orientation == VERTICAL) {
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

            val spanCount: Int = when (strategy) {
                STRATEGY_MIN_SIZE -> getSpanCountForMinSize(totalSpace, columnSize)
                STRATEGY_SUITABLE_SIZE -> getSpanCountForSuitableSize(totalSpace, columnSize)
                else -> getSpanCountForMinSize(totalSpace, columnSize)
            }
            setSpanCount(spanCount)
            columnSizeChanged = false

            if (null != listeners) {
                var i = 0
                val n = listeners!!.size
                while (i < n) {
                    listeners!![i].onUpdateSpanCount(spanCount)
                    i++
                }
            }
        }
        super.onMeasure(recycler, state, widthSpec, heightSpec)
    }

    fun addOnUpdateSpanCountListener(listener: OnUpdateSpanCountListener) {
        if (null == listeners) {
            listeners = mutableListOf()
        }
        listeners!!.add(listener)
    }

    fun removeOnUpdateSpanCountListener(listener: OnUpdateSpanCountListener) {
        if (null != listeners) {
            listeners!!.remove(listener)
        }
    }

    interface OnUpdateSpanCountListener {
        fun onUpdateSpanCount(spanCount: Int)
    }
}