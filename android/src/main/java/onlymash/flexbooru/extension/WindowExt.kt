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

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*


fun Window.showSystemBars() {
    ViewCompat.getWindowInsetsController(decorView)?.show(WindowInsetsCompat.Type.systemBars())
}

fun Window.hideSystemBars() {
    ViewCompat.getWindowInsetsController(decorView)?.apply {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

@Suppress("DEPRECATION")
inline val Window.isStatusBarShown: Boolean
    get() {
        val flags = attributes.flags
        val newFlags = flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
        return flags == newFlags
    }

fun AppCompatActivity.setupInsets(insetsCallback: (insets: WindowInsetsCompat) -> Unit) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val container = findViewById<View>(android.R.id.content)
    ViewCompat.setOnApplyWindowInsetsListener(container) { _, insets ->
        val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        container.updatePadding(
            left = systemBarsInsets.left,
            right = systemBarsInsets.right
        )
        insetsCallback(insets)
        insets
    }
}

fun Activity.getScreenWidthPixels(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.width() - insets.left - insets.right
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

fun Activity.getScreenHeightPixels(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.height() - insets.top - insets.bottom
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}

fun Activity.getScreenWidthDp(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        val width = windowMetrics.bounds.width() - insets.left - insets.right
        (width / resources.displayMetrics.density).toInt()
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        (displayMetrics.widthPixels / displayMetrics.density).toInt()
    }
}