/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding

fun AppCompatActivity.setupInsets(insetsCallback: (insets: WindowInsets) -> Unit) {
    findViewById<View>(android.R.id.content).apply {
        setOnApplyWindowInsetsListener { _, insets ->
            updatePadding(left = insets.systemWindowInsetLeft, right = insets.systemWindowInsetRight)
            insetsCallback(insets)
            insets
        }
        systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }
}

fun AppCompatActivity.drawNavBar(insetsCallback: (insets: WindowInsets) -> Unit) {
    findViewById<View>(android.R.id.content).apply {
        systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        setOnApplyWindowInsetsListener { _, insets ->
            updatePadding(
                top = insets.systemWindowInsetTop,
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )
            insetsCallback(insets)
            insets
        }
    }
}

object ListListener : View.OnApplyWindowInsetsListener {
    override fun onApplyWindowInsets(view: View, insets: WindowInsets) = insets.apply {
        view.updatePadding(bottom = systemWindowInsetBottom)
    }
}