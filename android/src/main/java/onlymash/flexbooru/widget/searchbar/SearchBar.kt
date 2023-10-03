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

package onlymash.flexbooru.widget.searchbar

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.ActionMenuView
import com.google.android.material.textview.MaterialTextView
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import onlymash.flexbooru.R
import onlymash.flexbooru.data.utils.toQuery
import onlymash.flexbooru.extension.toVisibility
import onlymash.flexbooru.util.ViewAnimation
import onlymash.flexbooru.util.ViewTransition

class SearchBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr),
    View.OnClickListener, TextWatcher, TextView.OnEditorActionListener,
    SearchEditText.SearchEditTextListener {

    companion object {
        private const val STATE_KEY_SUPER = "super"
        private const val STATE_KEY_STATE = "state"
        const val STATE_NORMAL = 0
        const val STATE_SEARCH = 1
        const val STATE_EXPAND = 2
    }

    private var state = STATE_NORMAL

    private val editText: SearchEditText
    private val menuView: ActionMenuView
    private val leftButton: ImageButton
    private val title: MaterialTextView

    private val dividerHeader: View
    private val listView: ListView
    private val listContainer: LinearLayout

    private var helper: Helper? = null
    private var stateListener: StateListener? = null

    private val viewTransition: ViewTransition
    private val suggestions: MutableList<String> = mutableListOf()
    private val suggestionAdapter: SuggestionAdapter

    private val inputMethodManager by lazy { this@SearchBar.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.widget_searchbar, this)
        editText = findViewById(R.id.search_edit_text)
        menuView = findViewById(R.id.search_bar_menu_view)
        leftButton = findViewById(R.id.menu_button)
        title = findViewById(R.id.search_title)
        dividerHeader = findViewById(R.id.divider_header)
        listView = findViewById(R.id.list_view)
        listContainer = findViewById(R.id.list_container)

        listContainer.toVisibility(false)
        menuView.setOnMenuItemClickListener {
            helper?.onMenuItemClick(it)
            true
        }
        leftButton.setOnClickListener(this)
        title.setOnClickListener(this)
        viewTransition = ViewTransition(title, editText)
        suggestionAdapter = SuggestionAdapter(inflater)
        listView.apply {
            adapter = suggestionAdapter
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                addTagToQuery(suggestions[position])
            }
        }
        editText.apply {
            addTextChangedListener(this@SearchBar)
            setOnEditorActionListener(this@SearchBar)
            setSearchEditTextListener(this@SearchBar)
        }
    }

    fun setTitle(text: CharSequence) {
        title.text = text
    }

    fun setEditText(text: CharSequence) {
        editText.setText(text)
        editText.setSelection(text.length)
    }

    fun setEditText(text: String) {
        editText.setText(text)
        editText.setSelection(text.length)
    }

    fun setEditTextSelectionModeCallback(callback: ActionMode.Callback) {
        editText.customSelectionActionModeCallback = callback
    }

    fun setLeftDrawable(drawable: Drawable) {
        leftButton.setImageDrawable(drawable)
    }

    fun getLeftButton(): ImageButton = leftButton

    fun setMenu(menuId: Int, menuInflater: MenuInflater) {
        menuView.menu.clear()
        menuInflater.inflate(menuId, menuView.menu)
    }

    val currentState: Int
        get() = state

    fun getQueryText(): String = (editText.text ?: "").toString().trim()

    private fun addTagToQuery(tag: String) {
        val queryList = (editText.text ?: "").toString()
            .replace(" +".toRegex(), " ").split(" ").toMutableList()
        if (tag in queryList) {
            return
        }
        if (queryList.isEmpty()) {
            setEditText("$tag ")
        } else {
            queryList.removeLast()
            queryList.add(tag)
            setEditText(queryList.toQuery() + " ")
        }
    }

    private fun getLastTag(query: String): String {
        val queryList = query.replace(" +".toRegex(), " ").split(" ")
        return if (queryList.isEmpty()) "" else queryList[queryList.lastIndex]
    }

    fun getSelectedText(): String {
        val text = editText.text ?: return ""
        val selectedText = text.toString().substring(editText.selectionStart, editText.selectionEnd)
        editText.setSelection(text.length)
        return selectedText
    }

    fun setEditTextHint(hint: CharSequence) {
        editText.hint = hint
    }

    override fun onClick(v: View?) {
        when (v) {
            leftButton -> {
                if (state == STATE_SEARCH || state == STATE_EXPAND) {
                    setIMEState(false)
                    updateState(state = STATE_NORMAL, showIME = false)
                } else {
                    helper?.onLeftButtonClick()
                }
            }
            title -> {
                helper?.onClickTitle()
                if (state == STATE_NORMAL) {
                    updateState(state = STATE_SEARCH, showIME = true)
                }
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable?) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (state != STATE_SEARCH) return
        when {
            s.isNullOrEmpty() -> {
                if (suggestions.isNotEmpty()) {
                    clearSuggestions()
                }
            }
            else -> {
                clearSuggestions()
                val tag = getLastTag(s.toString())
                if (tag.isNotBlank()) {
                    helper?.onFetchSuggestion(tag)
                }
            }
        }
    }

    private fun clearSuggestions() {
        suggestions.clear()
        suggestionAdapter.notifyDataSetChanged()
        hideSuggestion(false)
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) {
            val query = getQueryText()
            if (query.isNotEmpty()) {
                updateState(STATE_NORMAL)
                helper?.onApplySearch(query)
            }
            return true
        }
        return false
    }

    override fun onBackPressed() {
        when (state) {
            STATE_SEARCH -> updateState(
                STATE_NORMAL, showIME = false)
            STATE_EXPAND -> {
                if (inputMethodManager.isActive(editText)) {
                    hideIME()
                } else {
                    updateState(STATE_NORMAL, showIME = false)
                }
            }
        }
        helper?.onEditTextBackPressed()
    }

    private fun updateState(state: Int, animation: Boolean = true, showIME: Boolean = false) {
        if (this.state == state) return
        val oldState = this.state
        this.state = state
        setSuggestionVisibility()
        when (oldState) {
            STATE_NORMAL -> {
                viewTransition.showView(1, animation)
                editText.requestFocus()
                setIMEState(showIME)
            }
            STATE_SEARCH, STATE_EXPAND -> {
                if (state == STATE_NORMAL) {
                    viewTransition.showView(0, animation)
                    setIMEState(false)
                }
            }
        }
        stateListener?.onStateChange(state, oldState, animation)
    }

    fun toExpandState() {
        updateState(STATE_EXPAND, animation = true, showIME = false)
    }

    fun toNormalState() {
        updateState(STATE_NORMAL, animation = true, showIME = false)
    }

    fun clearText() {
        if (editText.text.isNullOrEmpty()) {
            updateState(STATE_NORMAL)
        } else {
            editText.text?.clear()
        }
    }

    private fun setIMEState(show: Boolean) {
        if (show) {
            inputMethodManager.showSoftInput(editText, 0)
        } else {
            hideIME()
        }
    }

    private fun hideIME() {
        inputMethodManager.hideSoftInputFromWindow(editText.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS)
        editText.clearFocus()
    }

    private fun setSuggestionVisibility(state: Int = this.state) {
        if (state == STATE_SEARCH && suggestions.isNotEmpty()) {
            showSuggestion()
        } else {
            hideSuggestion()
        }
    }

    private fun hideSuggestion(animation: Boolean = true) {
        if (listContainer.isVisible) {
            if (animation) {
                ViewAnimation.collapse(listContainer)
            } else {
                listContainer.isVisible = false
            }
        }
    }

    private fun showSuggestion() {
        if (!listContainer.isVisible) {
            ViewAnimation.expand(listContainer)
        }
    }

    override fun onSaveInstanceState(): Parcelable =
        Bundle().apply {
            putParcelable(STATE_KEY_SUPER, super.onSaveInstanceState())
            putInt(STATE_KEY_STATE, state)
        }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    state.getParcelable(STATE_KEY_SUPER, Parcelable::class.java)
                else
                    @Suppress("DEPRECATION")
                    state.getParcelable(STATE_KEY_SUPER)
            )
            updateState(state.getInt(STATE_KEY_STATE, STATE_NORMAL), animation = false)
        }
    }

    interface Helper {
        fun onLeftButtonClick()
        fun onMenuItemClick(menuItem: MenuItem)
        fun onClickTitle()
        fun onApplySearch(query: String)
        fun onEditTextBackPressed()
        fun onFetchSuggestion(query: String)
    }

    fun setHelper(helper: Helper) {
        this.helper = helper
    }

    interface StateListener {
        fun onStateChange(newState: Int, oldState: Int, animation: Boolean)
    }

    fun setStateListener(stateListener: StateListener) {
        this.stateListener = stateListener
    }

    fun updateSuggestions(suggestions: List<String>) {
        this.suggestions.apply {
            clear()
            addAll(suggestions)
        }
        suggestionAdapter.notifyDataSetChanged()
        setSuggestionVisibility()
    }

    private inner class SuggestionAdapter(
        private val inflater: LayoutInflater) : BaseAdapter() {

        override fun getCount(): Int = suggestions.size

        override fun getItem(position: Int): Any = suggestions[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val textView = (convertView as? TextView) ?:
            (inflater.inflate(R.layout.item_suggestion, parent, false)) as TextView
            textView.text = suggestions[position]
            return textView
        }
    }
}