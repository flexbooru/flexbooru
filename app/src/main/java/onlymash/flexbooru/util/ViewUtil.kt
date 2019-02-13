package onlymash.flexbooru.util

import android.view.View

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