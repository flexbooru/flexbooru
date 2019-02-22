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

package onlymash.flexbooru.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout

class DismissFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var swipeGestureDetector: SwipeGestureDetector
    private var dismissListener: OnDismissListener? = null
    private var initHeight: Int = 0 //child view's original height;
    private var initWidth: Int = 0
    private var initLeft = 0
    private var initTop = 0

    init {
        swipeGestureDetector = SwipeGestureDetector(context,
            object : SwipeGestureDetector.OnSwipeGestureListener {
                override fun onSwipeTopBottom(deltaX: Float, deltaY: Float) {
                    dragChildView(deltaX, deltaY)
                }

                override fun onSwipeLeftRight(deltaX: Float, deltaY: Float) {

                }

                override fun onFinish(direction: Int, distanceX: Float, distanceY: Float) {
                    if (dismissListener != null && direction == SwipeGestureDetector.DIRECTION_TOP_BOTTOM) {
                        if (distanceY > initHeight / 7) {
                            dismissListener!!.onDismiss()
                        } else {
                            dismissListener!!.onCancel()
                            reset()
                        }
                    }
                }
            })
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.pointerCount == 1) return swipeGestureDetector.onInterceptTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount == 1) return swipeGestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    /**
     * 模仿微信图片查看时的拖动返回效果
     * @param deltaX
     * @param deltaY
     */
    private fun dragChildView(deltaX: Float, deltaY: Float) {
        val count = childCount
        if (count > 0) {
            val view = getChildAt(0)
            scaleAndMove(view, deltaX, deltaY)
        }
    }

    /**
     * 最小缩放到1/2
     * @param view
     * @param deltaY
     */
    private fun scaleAndMove(view: View, deltaX: Float, deltaY: Float) {
        var params = view.layoutParams
        if (params !is ViewGroup.MarginLayoutParams) {
            params = ViewGroup.MarginLayoutParams(view.width, view.height)
        }
        if (params.width <= 0 && params.height <= 0) {
            params.width = view.width
            params.height = view.height
        }
        if (initHeight <= 0) {
            initHeight = view.height
            initWidth = view.width
            initLeft = params.leftMargin
            initTop = params.topMargin
        }
        val percent = deltaY / height
        val scaleX = (initWidth * percent).toInt()
        val scaleY = (initHeight * percent).toInt()
        params.width = params.width - scaleX
        params.height = params.height - scaleY
        params.leftMargin += calXOffset(deltaX) + scaleX / 2
        params.topMargin += calYOffset(deltaY) + scaleY / 2
        view.layoutParams = params
        dismissListener?.onScaleProgress(percent)
    }

    private fun calXOffset(deltaX: Float): Int = deltaX.toInt()

    private fun calYOffset(deltaY: Float): Int = deltaY.toInt()

    private fun reset() {
        val count = childCount
        if (count > 0) {
            val view = getChildAt(0)
            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            val w = params.width
            val h = params.height
            val lm = params.leftMargin
            val tm = params.topMargin
            val dw= initWidth - w
            val dh = initHeight - h
            val dlm = initLeft - lm
            val dtm = initTop - tm
            ValueAnimator.ofFloat(0f, 1f).apply {
                addUpdateListener { animation ->
                    val value = animation.animatedValue as Float
                    view.layoutParams = params.apply {
                        width = (dw * value).toInt() + w
                        height = (dh * value).toInt() + h
                        leftMargin = (dlm * value).toInt() + lm
                        topMargin = (dtm * value).toInt() + tm
                    }
                }
                interpolator = DecelerateInterpolator()
                duration = 200
                start()
            }
        }
    }

    fun setDismissListener(dismissListener: OnDismissListener) {
        this.dismissListener = dismissListener
    }

    interface OnDismissListener {
        fun onScaleProgress(scale: Float)
        fun onDismiss()
        fun onCancel()
    }
}