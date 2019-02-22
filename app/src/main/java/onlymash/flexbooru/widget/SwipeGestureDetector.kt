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

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration

class SwipeGestureDetector(context: Context, private val listener: OnSwipeGestureListener) {
    private var direction = 0
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var initialMotionX = 0f
    private var initialMotionY = 0f
    private var lastMotionX = 0f
    private var lastMotionY = 0f
    private var isBeingDragged: Boolean = false

    fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked

        val x = event.rawX
        val y = event.rawY

        // Always take care of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the drag.
            reset(initialMotionX, initialMotionY)
            return false
        }

        if (action != MotionEvent.ACTION_DOWN && isBeingDragged) {
            return true
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastMotionX = x
                initialMotionX = lastMotionX
                lastMotionY = y
                initialMotionY = lastMotionY
            }
            MotionEvent.ACTION_MOVE -> {
                val xDiff = Math.abs(x - initialMotionX)
                val yDiff = Math.abs(y - initialMotionY)
                if (xDiff > touchSlop && xDiff > yDiff) {
                    isBeingDragged = true
                    //direction horizon
                    direction = DIRECTION_LEFT_RIGHT
                } else if (yDiff > touchSlop && yDiff > xDiff) {
                    isBeingDragged = true
                    //direction vertical
                    direction = DIRECTION_TOP_BOTTOM
                }
            }
        }
        return isBeingDragged
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val x = event.rawX
        val y = event.rawY

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastMotionX = x
                initialMotionX = x
                lastMotionY = y
                initialMotionY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - lastMotionX
                val deltaY = y - lastMotionY
                lastMotionX = x
                lastMotionY = y
                if (isBeingDragged) {
                    if (direction == DIRECTION_LEFT_RIGHT) {
                        listener.onSwipeLeftRight(deltaX, deltaY)
                    } else if (direction == DIRECTION_TOP_BOTTOM) {
                        listener.onSwipeTopBottom(deltaX, deltaY)
                    }
                } else {
                    val xDiff = Math.abs(x - initialMotionX)
                    val yDiff = Math.abs(y - initialMotionY)
                    if (xDiff > touchSlop && xDiff > yDiff) {
                        isBeingDragged = true
                        //direction horizon
                        direction = DIRECTION_LEFT_RIGHT
                    } else if (yDiff > touchSlop && yDiff > xDiff) {
                        isBeingDragged = true
                        //direction vertical
                        direction = DIRECTION_TOP_BOTTOM
                    }
                }
            }
            MotionEvent.ACTION_UP -> reset(x, y)
            MotionEvent.ACTION_CANCEL -> reset(x, y)
        }
        return true
    }

    private fun reset(x: Float, y: Float) {
        if (isBeingDragged) {
            listener.onFinish(direction, x - initialMotionX, y - initialMotionY)
        }
        isBeingDragged = false
    }

    interface OnSwipeGestureListener {
        fun onSwipeTopBottom(deltaX: Float, deltaY: Float)
        fun onSwipeLeftRight(deltaX: Float, deltaY: Float)
        fun onFinish(direction: Int, distanceX: Float, distanceY: Float)
    }

    companion object {
        const val DIRECTION_TOP_BOTTOM = 1
        const val DIRECTION_LEFT_RIGHT = 4
    }

}