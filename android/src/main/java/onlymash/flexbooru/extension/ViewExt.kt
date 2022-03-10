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

package onlymash.flexbooru.extension

import android.view.View

fun View.toVisibility(constraint : Boolean) {
    visibility = if (constraint) View.VISIBLE else View.GONE
}

fun View.toggleArrow(): Boolean =
    when (rotation) {
        0f -> {
            animate().apply {
                duration = 200
                rotation(180f)
            }
            true
        }
        else -> {
            animate().apply {
                duration = 200
                rotation(0f)
            }
            false
        }
    }

fun View.toggleArrow(show: Boolean): Boolean =
    toggleArrow(show, true)

fun View.toggleArrow(show: Boolean, delay: Boolean): Boolean =
    when {
        show -> {
            animate().apply {
                duration = if (delay) 200 else 0
                rotation(180f)
            }
            true
        }
        else -> {
            animate().apply {
                duration = if (delay) 200 else 0
                rotation(0f)
            }
            false
        }
    }

fun View.rotate(degree: Float): Boolean =
    when (rotation) {
        0f -> {
            animate().apply {
                duration = 300
                rotation(degree)
            }
            true
        }
        else -> {
            animate().apply {
                duration = 300
                rotation(0f)
            }
            false
        }
    }
