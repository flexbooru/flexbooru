/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

package onlymash.flexbooru.widget.searchbar

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.math.MathUtils
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.animation.SimpleAnimatorListener

class SearchBarMover(private val helper: Helper,
                     private val searchBar: View,
                     vararg recyclerViews: RecyclerView) : RecyclerView.OnScrollListener() {

    companion object {
        private const val ANIMATE_TIME = 400L
    }

    private var isShow: Boolean = false
    private var searchBarMoveAnimator: ValueAnimator? = null

    init {
        for (recyclerView in recyclerViews) {
            recyclerView.addOnScrollListener(this)
        }
    }

    fun cancelAnimation() {
        searchBarMoveAnimator?.cancel()
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE && helper.isValidView(recyclerView)) {
            returnSearchBarPosition(true)
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (helper.isValidView(recyclerView)) {
            val y2 = getY2(searchBar).toInt()
            val ty = searchBar.translationY.toInt()
            val offsetYStep = MathUtils.clamp(-dy, -y2, -ty)
            if (offsetYStep != 0) {
                searchBar.translationY += offsetYStep
            }
        }
    }

    fun returnSearchBarPosition(animation: Boolean) {
        if (searchBar.height == 0) {
            // Layout not called
            return
        }

        val show = if (helper.isForceShowSearchBar) {
            true
        } else {
            val recyclerView = helper.validRecyclerView
            val systemUiTopSize = searchBar.marginTop - searchBar.resources.getDimensionPixelSize(R.dimen.search_bar_vertical_margin)
            if (!recyclerView.isShown) {
                true
            } else if (recyclerView.computeVerticalScrollOffset() < searchBar.bottom - systemUiTopSize) {
                true
            } else {
                getY2(searchBar).toInt() - systemUiTopSize > searchBar.height / 2
            }
        }

        val offset = if (show) {
            -searchBar.translationY.toInt()
        } else {
            -getY2(searchBar).toInt()
        }

        if (offset == 0) {
            // No need to scroll
            return
        }

        if (animation) {
            if (searchBarMoveAnimator != null) {
                if (isShow == show) {
                    // The same target, no need to do animation
                    return
                } else {
                    // Cancel it
                    searchBarMoveAnimator?.cancel()
                    searchBarMoveAnimator = null
                }
            }

            isShow = show
            val va = ValueAnimator.ofInt(0, offset)
            va.duration = ANIMATE_TIME
            va.addListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator) {
                    searchBarMoveAnimator = null
                }
            })
            va.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                var lastValue: Int = 0
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    val value = animation.animatedValue as Int
                    val offsetStep = value - lastValue
                    lastValue = value
                    searchBar.translationY += offsetStep
                }
            })
            searchBarMoveAnimator = va
            va.start()
        } else {
            searchBarMoveAnimator?.cancel()
            searchBar.translationY += offset
        }
    }

    @JvmOverloads
    fun showSearchBar(animation: Boolean = true) {
        if (searchBar.height == 0) {
            // Layout not called
            return
        }

        val offset = -searchBar.translationY.toInt()

        if (offset == 0) {
            // No need to scroll
            return
        }

        if (animation) {
            if (searchBarMoveAnimator != null) {
                if (isShow) {
                    // The same target, no need to do animation
                    return
                } else {
                    // Cancel it
                    searchBarMoveAnimator?.cancel()
                    searchBarMoveAnimator = null
                }
            }

            isShow = true
            val va = ValueAnimator.ofInt(0, offset)
            va.duration = ANIMATE_TIME
            va.addListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    searchBarMoveAnimator = null
                }
            })
            va.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                var lastValue: Int = 0
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    val value = animation.animatedValue as Int
                    val offsetStep = value - lastValue
                    lastValue = value
                    searchBar.translationY += offsetStep
                }
            })
            searchBarMoveAnimator = va
            va.start()
        } else {
            searchBarMoveAnimator?.cancel()
            searchBar.translationY += offset
        }
    }

    private fun getY2(view: View): Float {
        return view.y + view.height
    }

    interface Helper {

        val validRecyclerView: RecyclerView

        val isForceShowSearchBar: Boolean

        fun isValidView(recyclerView: RecyclerView): Boolean
    }

}