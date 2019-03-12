/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation

object ViewAnimation {

    fun expand(v: View, animListener: AnimListener) {
        val a = expandAction(v)
        a.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                animListener.onFinish()
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        v.startAnimation(a)
    }

    fun expand(v: View) {
        val a = expandAction(v)
        v.startAnimation(a)
    }

    private fun expandAction(v: View): Animation {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val targetHeight = v.measuredHeight

        v.layoutParams.height = 0
        v.visibility = View.VISIBLE
        val a = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                v.layoutParams.height = if (interpolatedTime == 1f)
                    ViewGroup.LayoutParams.WRAP_CONTENT
                else
                    (targetHeight * interpolatedTime).toInt()
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        a.duration = (targetHeight / v.context.resources.displayMetrics.density).toInt().toLong()
        v.startAnimation(a)
        return a
    }

    fun collapse(v: View) {
        val initialHeight = v.measuredHeight

        val a = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        a.duration = (initialHeight / v.context.resources.displayMetrics.density).toInt().toLong()
        v.startAnimation(a)
    }

    fun flyInDown(v: View, animListener: AnimListener?) {
        v.visibility = View.VISIBLE
        v.alpha = 0.0f
        v.translationY = 0f
        v.translationY = (-v.height).toFloat()
        // Prepare the View for the animation
        v.animate()
            .setDuration(200)
            .translationY(0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animListener?.onFinish()
                    super.onAnimationEnd(animation)
                }
            })
            .alpha(1.0f)
            .start()
    }

    fun flyOutDown(v: View, animListener: AnimListener?) {
        v.visibility = View.VISIBLE
        v.alpha = 1.0f
        v.translationY = 0f
        // Prepare the View for the animation
        v.animate()
            .setDuration(200)
            .translationY(v.height.toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animListener?.onFinish()
                    super.onAnimationEnd(animation)
                }
            })
            .alpha(0.0f)
            .start()
    }

    fun fadeIn(v: View) {
        ViewAnimation.fadeIn(v, null)
    }

    fun fadeIn(v: View, animListener: AnimListener?) {
        v.visibility = View.GONE
        v.alpha = 0.0f
        // Prepare the View for the animation
        v.animate()
            .setDuration(200)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    v.visibility = View.VISIBLE
                    animListener?.onFinish()
                    super.onAnimationEnd(animation)
                }
            })
            .alpha(1.0f)
    }

    fun fadeOut(v: View) {
        ViewAnimation.fadeOut(v, null)
    }

    fun fadeOut(v: View, animListener: AnimListener?) {
        v.alpha = 1.0f
        // Prepare the View for the animation
        v.animate()
            .setDuration(500)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animListener?.onFinish()
                    super.onAnimationEnd(animation)
                }
            })
            .alpha(0.0f)
    }

    fun showIn(v: View) {
        v.visibility = View.VISIBLE
        v.alpha = 0f
        v.translationY = v.height.toFloat()
        v.animate()
            .setDuration(200)
            .translationY(0f)
            .alpha(1f)
            .start()
    }

    fun initShowOut(v: View) {
        v.visibility = View.GONE
        v.translationY = v.height.toFloat()
        v.alpha = 0f
    }

    fun showOut(v: View) {
        v.visibility = View.VISIBLE
        v.alpha = 1f
        v.translationY = 0f
        v.animate()
            .setDuration(200)
            .translationY(v.height.toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    v.visibility = View.GONE
                    super.onAnimationEnd(animation)
                }
            }).alpha(0f)
            .start()
    }

    fun rotateFab(v: View, rotate: Boolean): Boolean {
        v.animate().setDuration(200)
            .rotation(if (rotate) 135f else 0f)
        return rotate
    }


    interface AnimListener {
        fun onFinish()
    }

    fun fadeOutIn(view: View) {
        view.alpha = 0f
        val animatorSet = AnimatorSet()
        val animatorAlpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 0.5f, 1f)
        ObjectAnimator.ofFloat(view, "alpha", 0f).start()
        animatorAlpha.duration = 500
        animatorSet.play(animatorAlpha)
        animatorSet.start()
    }


    fun showScale(v: View) {
        ViewAnimation.showScale(v, null)
    }

    fun showScale(v: View, animListener: AnimListener?) {
        v.animate()
            .scaleY(1f)
            .scaleX(1f)
            .setDuration(200)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animListener?.onFinish()
                    super.onAnimationEnd(animation)
                }
            })
            .start()
    }

    fun hideScale(v: View) {
        ViewAnimation.fadeOut(v, null)
    }

    fun hideScale(v: View, animListener: AnimListener?) {
        v.animate()
            .scaleY(0f)
            .scaleX(0f)
            .setDuration(200)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animListener?.onFinish()
                    super.onAnimationEnd(animation)
                }
            })
            .start()
    }
}