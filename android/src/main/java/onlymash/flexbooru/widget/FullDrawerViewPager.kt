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

package onlymash.flexbooru.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import kotlin.math.abs

class FullDrawerViewPager : ViewPager {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

    private var startX = 0f
    private var startY = 0f

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                startX = ev.x
                startY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val endX = ev.x
                val endY = ev.y
                val dX = endX - startX
                val dY = endY - startY
                if(abs(dX) > abs(dY)) {
                    if (currentItem == 0 && dX > 0) {
                        parent.requestDisallowInterceptTouchEvent(false)
                    } else if (dX < 0) {
                        adapter?.let {
                            if (currentItem == it.count - 1) {
                                parent.requestDisallowInterceptTouchEvent(true)
                            }
                        }
                    } else {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}