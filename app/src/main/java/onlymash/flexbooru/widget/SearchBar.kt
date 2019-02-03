package onlymash.flexbooru.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.*
import android.widget.TextView
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.widget_search_bar.view.*
import onlymash.flexbooru.R

class SearchBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr), View.OnClickListener, TextView.OnEditorActionListener, TextWatcher {

    companion object {
        private const val STATE_KEY_SUPER = "super"
        private const val STATE_KEY_STATE = "state"
        private const val ANIMATE_TIME = 300L

        const val STATE_NORMAL = 0
        const val STATE_SEARCH = 1
        const val STATE_SEARCH_LIST = 2
    }
    private var helper: Helper? = null
    init {
        LayoutInflater.from(context).inflate(R.layout.widget_search_bar, this)
        search_bar_menu_view.setOnMenuItemClickListener { menuItem ->
            helper?.onMenuItemClick(menuItem)
            true
        }
        menu_button.setOnClickListener {
            helper?.onLeftButtonClick()
        }
    }

    fun setLeftDrawable(drawable: Drawable) {
        menu_button.setImageDrawable(drawable)
    }

    fun setMenu(menuId: Int, menuInflater: MenuInflater) {
        menuInflater.inflate(menuId,search_bar_menu_view.menu)
    }


    override fun onClick(v: View) {

    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable) {

    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        return false
    }

    fun setHelper(helper: Helper) {
        this.helper = helper
    }

    interface Helper {
        fun onLeftButtonClick()
        fun onMenuItemClick(menuItem: MenuItem)
    }
}