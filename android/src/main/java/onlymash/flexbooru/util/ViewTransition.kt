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

package onlymash.flexbooru.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import onlymash.flexbooru.animation.SimpleAnimatorListener

class ViewTransition(vararg views: View) {

    companion object {
        private const val ANIMATE_TIME = 300L
    }

    private val views: Array<out View>

    var shownViewIndex = -1
        private set

    private var animator1: Animator? = null
    private var animator2: Animator? = null

    private var onShowViewListener: OnShowViewListener? = null

    init {
        if (views.size < 2) {
            throw IllegalStateException("You must pass view to ViewTransition")
        }
        this.views = views
        showView(0, false)
    }

    fun setOnShowViewListener(listener: OnShowViewListener) {
        onShowViewListener = listener
    }

    @JvmOverloads
    fun showView(shownView: Int, animation: Boolean = true): Boolean {
        val length = views.size
        if (shownView >= length || shownView < 0) {
            throw IndexOutOfBoundsException(
                "Only " + length + " view(s) in " +
                        "the ViewTransition, but attempt to show " + shownView
            )
        }
        return if (shownViewIndex != shownView) {
            val oldShownView = shownViewIndex
            shownViewIndex = shownView
            // Cancel animation
            animator1?.cancel()
            animator2?.cancel()
            if (animation) {
                for (i in 0 until length) {
                    if (i != oldShownView && i != shownView) {
                        val v = views[i]
                        v.alpha = 0f
                        v.visibility = View.GONE
                    }
                }
                startAnimations(views[oldShownView], views[shownView])
            } else {
                for (i in 0 until length) {
                    val v = views[i]
                    if (i == shownView) {
                        v.alpha = 1f
                        v.visibility = View.VISIBLE
                    } else {
                        v.alpha = 0f
                        v.visibility = View.GONE
                    }
                }
            }
            onShowViewListener?.onShowView(views[oldShownView], views[shownView])
            true
        } else {
            false
        }
    }

    private fun startAnimations(hiddenView: View, shownView: View) {
        animator1 = ObjectAnimator.ofFloat(hiddenView, "alpha", 0f).apply {
            duration = ANIMATE_TIME
            addListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator) {
                    hiddenView.visibility = View.GONE
                    animator1 = null
                }
            })
            start()
        }
        shownView.visibility = View.VISIBLE
        animator2 = ObjectAnimator.ofFloat(shownView, "alpha", 1f).apply {
            duration = ANIMATE_TIME
            addListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator) {
                    animator2 = null
                }
            })
            start()
        }
    }

    interface OnShowViewListener {
        fun onShowView(hiddenView: View, shownView: View)
    }
}