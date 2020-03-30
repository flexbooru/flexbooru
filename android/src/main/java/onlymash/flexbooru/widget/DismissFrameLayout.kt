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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max

class DismissFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var lastX = 0f
    private var lastY = 0f

    private val dragHelper: ViewDragHelper
    private val minimumFlingVelocity: Int
    private var dismissListener: OnDismissListener? = null

    init {
        dragHelper = ViewDragHelper.create(this, 1f / 8f, ViewDragCallback())
        minimumFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = ev.x
                lastY = ev.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - lastX
                val dy = ev.y - lastY
                val angle = atan2(dy, dx) * 180 / PI
                if (angle in 20.0..160.0) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean =
        dragHelper.shouldInterceptTouchEvent(ev)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.run {
            dragHelper.processTouchEvent(event)
        }
        return true
    }

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun setDismissListener(dismissListener: OnDismissListener) {
        this.dismissListener = dismissListener
    }

    interface OnDismissListener {
        fun onDismissStart()
        fun onDismissProgress(progress: Float)
        fun onDismissed()
        fun onDismissCancel()
    }

    private inner class ViewDragCallback : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean = true

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int = 0

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int = max(0, top)

        override fun getViewHorizontalDragRange(child: View): Int = 0

        override fun getViewVerticalDragRange(child: View): Int = height

        override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
            dismissListener?.onDismissStart()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            dismissListener?.onDismissProgress(top.toFloat() / height.toFloat())
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val slop = if (yvel > minimumFlingVelocity) height / 6 else height / 3
            if (releasedChild.top > slop) {
                dismissListener?.onDismissed()
            } else {
                dismissListener?.onDismissCancel()
                dragHelper.settleCapturedViewAt(0, 0)
                invalidate()
            }
        }
    }
}