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
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.widget_search_bar.view.*
import onlymash.flexbooru.R
import onlymash.flexbooru.util.ViewTransition

class SearchBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr), View.OnClickListener {

    companion object {
        private const val STATE_KEY_SUPER = "super"
        private const val STATE_KEY_STATE = "state"
        private const val ANIMATE_TIME = 300L

        const val STATE_NORMAL = 0
        const val STATE_SEARCH = 1
        const val STATE_SEARCH_LIST = 2
    }
    private var state = STATE_NORMAL

    private var helper: Helper? = null
    private var stateChangeListener: StateChangeListener? = null
    private val viewTransition: ViewTransition

    private val inputMethodManager by lazy { this@SearchBar.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }

    private val searchEditTextListener = object : SearchEditText.SearchEditTextListener {
        override fun onClick() {
            helper?.onSearchEditTextClick()
        }

        override fun onBackPressed() {
            hideIME()
            if (state == STATE_SEARCH) {
                setState(STATE_NORMAL)
            }
            helper?.onSearchEditTextBackPressed()
        }
    }

    private fun hideIME() {
        if (inputMethodManager.isActive(search_edit_text)) {
            inputMethodManager.hideSoftInputFromWindow(search_edit_text.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            search_edit_text.clearFocus()
        }
    }

    private fun showIME() {
        inputMethodManager.showSoftInput(search_edit_text, 0)
    }

    private val editorActionListener = object : TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            if (v == search_edit_text) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) {
                    applySearch()
                    return true
                }
            }
            return false
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

    }

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_search_bar, this)
        search_bar_menu_view.setOnMenuItemClickListener { menuItem ->
            helper?.onMenuItemClick(menuItem)
            true
        }
        menu_button.setOnClickListener {
            if (state == STATE_SEARCH) {
                hideIME()
                setState(STATE_NORMAL)
            }
            helper?.onLeftButtonClick()
        }
        search_title.setOnClickListener(this)
        search_edit_text.apply {
            setSearchEditTextListener(searchEditTextListener)
            setOnEditorActionListener(editorActionListener)
            addTextChangedListener(textWatcher)
        }
        viewTransition = ViewTransition(search_title, search_edit_text)
    }

    fun applySearch() {
        val text = search_edit_text.text
        if (!text.isNullOrBlank()) {
            val query = text.toString().trim()
            if (query.isNotBlank()) {
                helper?.onApplySearch(query)
            }
        }
    }

    fun getEditTextTextSize(): Float = search_edit_text.textSize

    fun setEditTextHint(hint: CharSequence) {
        search_edit_text.hint = hint
    }

    fun setText(text: String) {
        search_edit_text.setText(text)
    }

    fun setLeftDrawable(drawable: Drawable) {
        menu_button.setImageDrawable(drawable)
    }

    fun setLeftIconVisibility(visibility: Int) {
        menu_button.visibility = visibility
    }

    fun setMenu(menuId: Int, menuInflater: MenuInflater) {
        menuInflater.inflate(menuId,search_bar_menu_view.menu)
    }

    fun setTitle(title: String) {
        search_title.text = title
    }

    fun setTitle(resId: Int) {
        search_title.setText(resId)
    }

    override fun onClick(v: View) {
        when (v) {
            search_title -> {
                if (state == STATE_NORMAL) {
                    setState(STATE_SEARCH)
                }
                helper?.onClickTitle()
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? =
        Bundle().apply {
            putParcelable(STATE_KEY_SUPER, super.onSaveInstanceState())
            putInt(STATE_KEY_STATE, state)
        }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(STATE_KEY_SUPER))
            setState(state.getInt(STATE_KEY_STATE), false)
        }
    }


    fun getState(): Int = state

    fun setState(state: Int) {
        setState(state, true)
    }

    private fun setState(state: Int, animation: Boolean) {
        if (this.state == state) return
        val oldState = this.state
        this.state = state
        when (oldState) {
            STATE_NORMAL -> {
                viewTransition.showView(1, animation)
                search_edit_text.requestFocus()
                showIME()
                stateChangeListener?.onStateChange(state, oldState, animation)
            }
            STATE_SEARCH -> {
                when (state) {
                    STATE_NORMAL -> viewTransition.showView(0, animation)
                    else -> {

                    }
                }
                stateChangeListener?.onStateChange(state, oldState, animation)
            }
        }
    }


    fun setHelper(helper: Helper) {
        this.helper = helper
    }
    interface Helper {
        fun onLeftButtonClick()
        fun onMenuItemClick(menuItem: MenuItem)
        fun onClickTitle()
        fun onSearchEditTextClick()
        fun onApplySearch(query: String)
        fun onSearchEditTextBackPressed()
    }

    fun setStateChangeListener(listener: StateChangeListener) {
        stateChangeListener = listener
    }
    interface StateChangeListener {
        fun onStateChange(newState: Int, oldState: Int, animation: Boolean)
    }
}