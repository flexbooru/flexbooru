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

package onlymash.flexbooru.widget.search

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.ActionMenuView
import androidx.cardview.widget.CardView
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.SuggestionManager
import onlymash.flexbooru.entity.Suggestion
import onlymash.flexbooru.entity.tag.BaseTag
import onlymash.flexbooru.entity.tag.SearchTag
import onlymash.flexbooru.util.ViewAnimation
import onlymash.flexbooru.util.ViewTransition

class SearchBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr), View.OnClickListener, TextWatcher {

    companion object {
        private const val STATE_KEY_SUPER = "super"
        private const val STATE_KEY_STATE = "state"
        private const val ANIMATE_TIME = 300L

        const val STATE_NORMAL = 0
        const val STATE_SEARCH = 1
    }
    private var state = STATE_NORMAL

    private var helper: Helper? = null
    private var stateChangeListener: StateChangeListener? = null
    private val viewTransition: ViewTransition

    private var type: Int = -1
    private var searchTag: SearchTag? = null

    fun setType(type: Int) {
        this.type = type
    }

    fun setSearchTag(search: SearchTag) {
        searchTag = search
    }

    private var suggestionsOnline: MutableList<BaseTag>? = null
    private val suggestionsRepo by lazy { ServiceLocator.instance().getSuggestionRepository() }
    private val ioExecutor by lazy { ServiceLocator.instance().getDiskIOExecutor() }
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private var suggestions: MutableList<Suggestion>
    private val suggestionAdapter: SuggestionAdapter
    private val suggestionOnlineAdapter: SuggestionOnlineAdapter

    private val suggestionContainer: LinearLayout
    private val editText: SearchEditText
    private val menuView: ActionMenuView
    private val leftButton: ImageButton
    private val title: TextView
    private val suggestionList: ListView
    private val dividerHeader: View
    
    private val booruUid: Long

    private val inputMethodManager by lazy { this@SearchBar.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }

    private val searchEditTextListener = object :
        SearchEditText.SearchEditTextListener {
        override fun onClick() {
            helper?.onSearchEditTextClick()
        }

        override fun onBackPressed() {
            if (state == STATE_SEARCH) {
                setState(STATE_NORMAL)
            }
            helper?.onSearchEditTextBackPressed()
        }
    }

    private val editorActionListener: TextView.OnEditorActionListener

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_search_bar, this)
        suggestionContainer = findViewById(R.id.list_container)
        editText = findViewById(R.id.search_edit_text)
        menuView = findViewById(R.id.search_bar_menu_view)
        leftButton = findViewById(R.id.menu_button)
        title = findViewById(R.id.search_title)
        dividerHeader = findViewById(R.id.divider_header)
        suggestionList = findViewById(R.id.suggestion_list)

        editorActionListener = object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (v == editText) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) {
                        applySearch()
                        return true
                    }
                }
                return false
            }
        }
        menuView.setOnMenuItemClickListener { menuItem ->
            helper?.onMenuItemClick(menuItem)
            true
        }
        leftButton.setOnClickListener {
            if (state == STATE_SEARCH) {
                hideIME()
                setState(STATE_NORMAL)
            } else {
                helper?.onLeftButtonClick()
            }
        }
        title.setOnClickListener(this)
        editText.apply {
            setSearchEditTextListener(searchEditTextListener)
            setOnEditorActionListener(editorActionListener)
            addTextChangedListener(this@SearchBar)
        }
        viewTransition = ViewTransition(title, editText)
        booruUid = Settings.instance().activeBooruUid
        suggestions = mutableListOf()
        val inflater = LayoutInflater.from(context)
        suggestionAdapter = SuggestionAdapter(inflater)
        suggestionOnlineAdapter = SuggestionOnlineAdapter(inflater)
        suggestionList.apply {
            adapter = suggestionAdapter
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                if (adapter == suggestionAdapter) {
                    setText(suggestions[position].keyword)
                } else {
                    setText(suggestionsOnline!![position].getTagName())
                }
            }
            onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
                if (adapter == suggestionAdapter) {
                    SuggestionManager.deleteSuggestion(suggestions[position].uid)
                }
                true
            }
        }
    }

    private fun hideIME() {
        if (inputMethodManager.isActive(editText)) {
            inputMethodManager.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            editText.clearFocus()
        }
    }

    private fun showIME() {
        inputMethodManager.showSoftInput(editText, 0)
    }

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (state == STATE_SEARCH) {
            when {
                s.isNullOrEmpty() -> {
                    suggestionList.adapter = suggestionAdapter
                    showSuggestion()
                }
                s.isNotBlank() && type > -1 && searchTag != null -> {
                    if (type == Constants.TYPE_GELBOORU) {
                        searchTag?.name = s.toString()
                    } else {
                        searchTag?.name = "$s*"
                    }
                    fetchSuggestions()
                }
                else -> hideSuggestion()
            }
        } else {
            hideSuggestion()
        }
    }

    private fun fetchSuggestions() {
        searchTag?.let {
            ioExecutor.execute {
                suggestionsOnline = suggestionsRepo.fetchSuggestions(type, it)
                Log.w("Test", suggestionsOnline?.toString() ?: "Null")
                mainHandler.post {
                    showSuggestion()
                    suggestionList.adapter = suggestionOnlineAdapter
                    suggestionOnlineAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    fun updateSuggestions(suggestions: MutableList<Suggestion>) {
        this.suggestions = suggestions
        suggestionAdapter.notifyDataSetChanged()
        if (suggestions.size > 0) {
            dividerHeader.visibility = View.VISIBLE
        } else {
            dividerHeader.visibility = View.GONE
        }
    }

    fun isSearchState(): Boolean = state == STATE_SEARCH

    fun applySearch() {
        val query = (editText.text ?: "").toString().trim()
        if (query.isNotEmpty()) {
            SuggestionManager.createSuggestion(Suggestion(booru_uid = booruUid, keyword = query))
        }
        helper?.onApplySearch(query)
        setState(STATE_NORMAL)
    }

    fun getEditTextText(): String = (editText.text ?: "").toString()

    fun getEditTextTextSize(): Float = editText.textSize

    fun setEditTextHint(hint: CharSequence) {
        editText.hint = hint
    }

    fun setText(text: String) {
        editText.setText(text)
        val size = text.length
        if (size > 0) editText.setSelection(size)
    }

    fun setLeftDrawable(drawable: Drawable) {
        leftButton.setImageDrawable(drawable)
    }

    fun setLeftIconVisibility(visibility: Int) {
        leftButton.visibility = visibility
    }

    fun setMenu(menuId: Int, menuInflater: MenuInflater) {
        menuInflater.inflate(menuId,menuView.menu)
    }

    fun setTitle(title: String) {
        this.title.text = title
    }

    fun setTitle(resId: Int) {
        title.setText(resId)
    }

    fun setTitleOnLongClickCallback(titleOnLongClick: () -> Unit) {
        title.setOnLongClickListener {
            titleOnLongClick()
            true
        }
    }

    override fun onClick(v: View) {
        when (v) {
            title -> {
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
            setState(state = state.getInt(STATE_KEY_STATE, STATE_NORMAL), animation = false, showIME = false, showSuggestion = false)
        }
    }

    fun getState(): Int = state

    fun setState(state: Int) {
        setState(state = state, animation = true, showIME = true, showSuggestion = true)
    }

    private fun setState(state: Int, showIME: Boolean) {
        setState(state = state, animation = true, showIME = showIME, showSuggestion = true)
    }

    private fun setState(state: Int, showIME: Boolean, showSuggestion: Boolean) {
        setState(state = state, animation = true, showIME = showIME, showSuggestion = showSuggestion)
    }

    fun enableSearchState(showIME: Boolean) {
        setState(STATE_SEARCH, showIME)
    }

    fun enableSearchState(showIME: Boolean, showSuggestion: Boolean) {
        setState(STATE_SEARCH, showIME, showSuggestion)
    }

    fun enableSearchState() {
        setState(STATE_SEARCH)
    }

    fun disableSearchState() {
        setState(STATE_NORMAL)
    }

    private fun showSuggestion() {
        if (suggestionContainer.visibility == View.VISIBLE) return
        ViewAnimation.expand(suggestionContainer)
    }

    private fun hideSuggestion() {
        if (suggestionContainer.visibility == View.GONE) return
        ViewAnimation.collapse(suggestionContainer)
    }

    private fun setState(
        state: Int,
        animation: Boolean,
        showIME: Boolean,
        showSuggestion: Boolean) {
        if (this.state == state) return
        val oldState = this.state
        this.state = state
        when (oldState) {
            STATE_NORMAL -> {
                viewTransition.showView(1, animation)
                editText.requestFocus()
                if (showIME) showIME()
                if (state == STATE_SEARCH && showSuggestion && editText.text.isNullOrEmpty()) {
                    showSuggestion()
                } else hideSuggestion()
                stateChangeListener?.onStateChange(state, oldState, animation)
            }
            STATE_SEARCH -> {
                hideIME()
                hideSuggestion()
                viewTransition.showView(0, animation)
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

    /**
     * Set search bar state change callback
     * */
    fun setStateChangeListener(listener: StateChangeListener) {
        stateChangeListener = listener
    }
    /**
     * Search bar state change listener
     * */
    interface StateChangeListener {
        fun onStateChange(newState: Int, oldState: Int, animation: Boolean)
    }

    private inner class SuggestionAdapter(private val inflater: LayoutInflater) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val textView = (convertView as? TextView) ?:
            (inflater.inflate(R.layout.item_suggestion, parent, false)) as TextView
            textView.text = suggestions[position].keyword
            return textView
        }

        override fun getItem(position: Int): Any = suggestions[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount(): Int = suggestions.size

    }

    private inner class SuggestionOnlineAdapter(private val inflater: LayoutInflater) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val textView = (convertView as? TextView) ?:
            (inflater.inflate(R.layout.item_suggestion, parent, false)) as TextView
            textView.text = suggestionsOnline?.get(position)?.getTagName() ?: ""
            return textView
        }

        override fun getItem(position: Int): Any = suggestionsOnline!![position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount(): Int = suggestionsOnline?.size ?: 0

    }
}