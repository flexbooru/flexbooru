package onlymash.flexbooru.ui.activity

interface OnBackPressedListener {
    fun canBack(): Boolean
}

abstract class PostActivity : BaseActivity() {

    abstract var currentNavItem: Int

    private var backPressedListener: OnBackPressedListener? = null

    internal fun setOnBackPressedListener(listener: OnBackPressedListener) {
        backPressedListener = listener
    }

    override fun onBackPressed() {
        if (currentNavItem == 0) {
            if (backPressedListener?.canBack() != false) {
                super.onBackPressed()
            }
        } else super.onBackPressed()
    }
}