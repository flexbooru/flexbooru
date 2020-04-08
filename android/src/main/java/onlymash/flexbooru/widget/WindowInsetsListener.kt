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

fun AppCompatActivity.hideNavBar(insetsCallback: (insets: WindowInsets) -> Unit) {
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