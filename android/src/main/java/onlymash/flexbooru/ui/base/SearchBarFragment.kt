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

package onlymash.flexbooru.ui.base

import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.FloatRange
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.AUTO_HIDE_BOTTOM_BAR_KEY
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.app.Settings.autoHideBottomBar
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.data.action.ActionTag
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.MuzeiManager
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Muzei
import onlymash.flexbooru.data.repository.suggestion.SuggestionRepositoryImpl
import onlymash.flexbooru.databinding.FragmentSearchbarBinding
import onlymash.flexbooru.ui.activity.MainActivity
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.ui.viewmodel.*
import onlymash.flexbooru.widget.searchbar.SearchBar
import onlymash.flexbooru.widget.searchbar.SearchBarMover
import org.kodein.di.instance

abstract class SearchBarFragment : BooruFragment<FragmentSearchbarBinding>(),
    SearchBar.Helper, SearchBar.StateListener, SearchBarMover.Helper, ActionMode.Callback {

    private val sp by instance<SharedPreferences>()
    val booruApis by instance<BooruApis>()

    private var actionTag: ActionTag? = null

    private lateinit var suggestionViewModel: SuggestionViewModel
    private lateinit var searchBarMover: SearchBarMover
    private lateinit var leftDrawable: DrawerArrowDrawable

    internal lateinit var mainList: RecyclerView
    internal lateinit var searchLayout: CoordinatorLayout
    internal lateinit var swipeRefresh: SwipeRefreshLayout
    internal lateinit var progressBarHorizontal: ProgressBar
    private lateinit var searchBar: SearchBar
    private lateinit var fabToListTop: FloatingActionButton
    private lateinit var networkStateContainer: LinearLayout
    private lateinit var errorMsg: TextView

    private var systemUiBottomSize = 0
    private var systemUiTopSize = 0

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if(!isViewCreated) {
                return
            }
            if (currentState == SearchBar.STATE_EXPAND) {
                toNormalState()
            }
        }
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchbarBinding {
        suggestionViewModel = getSuggestionViewModel(SuggestionRepositoryImpl(booruApis))
        return FragmentSearchbarBinding.inflate(inflater, container, false)
    }

    override fun onBaseViewCreated(view: View, savedInstanceState: Bundle?) {
        mainList = binding.refreshableList.list
        searchLayout = binding.searchLayout.searchLayoutContainer
        swipeRefresh = binding.refreshableList.swipeRefresh
        progressBarHorizontal = binding.progressHorizontal.progressBarHorizontal
        searchBar = binding.searchBar
        fabToListTop = binding.actionToTop
        networkStateContainer = binding.networkState.networkStateContainer
        errorMsg = binding.networkState.errorMsg
        networkStateContainer = binding.networkState.networkStateContainer
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            systemUiTopSize = systemBarsInsets.top
            systemUiBottomSize = systemBarsInsets.bottom
            searchBar.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                topMargin = resources.getDimensionPixelSize(R.dimen.search_bar_vertical_margin) + systemUiTopSize
            }
            fabToListTop.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                updateMargins(bottom = systemUiBottomSize + resources.getDimensionPixelSize(R.dimen.margin_normal))
            }
            setupMainListPadding()
            setupSwipeRefreshOffset()
            insets
        }
        setupFabToListTop()
        setupSwipeRefreshColor()
        initSearchBar()
        suggestionViewModel.suggestions.observe(viewLifecycleOwner) {
            searchBar.updateSuggestions(it)
        }
        binding.networkState.retryButton.setOnClickListener {
            retry()
        }
        onSearchBarViewCreated(view, savedInstanceState)
        sp.registerOnSharedPreferenceChangeListener(this)
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun onPause() {
        super.onPause()
        onBackPressedCallback.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        onBackPressedCallback.isEnabled = currentState == SearchBar.STATE_EXPAND
    }

    fun updateState(loadState: LoadState?) {
        if (loadState == null) return
        if (loadState is LoadState.Error && mainList.adapter?.itemCount == 0) {
            networkStateContainer.isVisible = true
            errorMsg.text = loadState.error.message
        } else {
            networkStateContainer.isVisible = false
        }
    }

    abstract fun retry()

    abstract fun onSearchBarViewCreated(view: View, savedInstanceState: Bundle?)

    override fun onBooruLoaded(booru: Booru?) {
        actionTag = if (booru == null) {
            null
        } else {
            ActionTag(
                booru = booru,
                limit = 6,
                order = "count"
            )
        }
    }

    private fun setupFabToListTop() {
        if (activity is SearchActivity) {
            fabToListTop.isVisible = true
            fabToListTop.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                behavior = HideBottomViewOnScrollBehavior<FloatingActionButton>()
            }
            fabToListTop.setOnClickListener {
                toListTop()
            }
        }
    }

    private fun setupSwipeRefreshColor() {
        swipeRefresh.setColorSchemeResources(
            R.color.blue,
            R.color.purple,
            R.color.green,
            R.color.orange,
            R.color.red
        )
    }

    private fun setupSwipeRefreshOffset() {
        val start = resources.getDimensionPixelSize(R.dimen.swipe_refresh_layout_offset_start) + systemUiTopSize
        val end = resources.getDimensionPixelSize(R.dimen.swipe_refresh_layout_offset_end) + systemUiTopSize
        swipeRefresh.setProgressViewOffset(false, start, end)
    }

    private fun initSearchBar() {
        leftDrawable = DrawerArrowDrawable(context)
        searchBar.setLeftDrawable(leftDrawable)
        searchBar.setHelper(this)
        searchBar.setStateListener(this)
        searchBar.setEditTextHint(getSearchBarHint())
        searchBarMover = SearchBarMover(this, searchBar, mainList)
        searchBar.setEditTextSelectionModeCallback(this)
    }

    private fun setupMainListPadding() {
        if (!isViewCreated) {
            return
        }
        val paddingTop = systemUiTopSize + resources.getDimensionPixelSize(R.dimen.header_item_height)
        if (activity is MainActivity) {
            val paddingBottom = systemUiBottomSize + resources.getDimensionPixelSize(R.dimen.nav_bar_height)
            searchLayout.updatePadding(top = paddingTop, bottom = paddingBottom)
            val paddingBottomAuto = if (autoHideBottomBar) systemUiBottomSize else paddingBottom
            mainList.updatePadding(top = paddingTop, bottom = paddingBottomAuto)
            progressBarHorizontal.updatePadding(bottom = paddingBottomAuto)
        } else {
            searchLayout.updatePadding(top = paddingTop, bottom = systemUiBottomSize)
            mainList.updatePadding(top = paddingTop, bottom = systemUiBottomSize)
            progressBarHorizontal.updatePadding(bottom = systemUiBottomSize)
        }
    }

    fun setLeftDrawableProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float) {
        leftDrawable.progress = progress
    }

    abstract fun getSearchBarHint(): CharSequence

    fun setSearchBarMenu(menuResId: Int) {
        activity?.menuInflater?.let {
            searchBar.setMenu(menuResId, it)
        }
    }

    fun setSearchBarTitle(title: CharSequence) {
        searchBar.setTitle(title)
    }

    fun setSearchBarText(text: CharSequence) {
        searchBar.setEditText(text)
    }

    fun getSearchBarLeftButton(): ImageButton {
        return searchBar.getLeftButton()
    }

    fun getEditQuery(): String = searchBar.getQueryText()

    val currentState: Int
        get() =  searchBar.currentState

    private fun forceShowNavBar() {
        val activity = activity
        if (activity is MainActivity) {
            activity.forceShowNavBar()
        }
    }

    private fun forceShowSearchBar() {
        searchBarMover.showSearchBar()
    }

    fun toExpandState() {
        if (!isViewCreated) {
            return
        }
        forceShowNavBar()
        searchBar.toExpandState()
        onBackPressedCallback.isEnabled = true
    }

    fun toNormalState() {
        if (!isViewCreated) {
            return
        }
        searchBar.toNormalState()
        forceShowSearchBar()
        forceShowNavBar()
        onBackPressedCallback.isEnabled = false
    }

    fun clearSearchBarText() {
        if (!isViewCreated) {
            return
        }
        searchBar.clearText()
    }

    private fun toggleArrowLeftDrawable() {
        toggleArrow(leftDrawable)
    }

    private fun toggleArrow(drawerArrow: DrawerArrowDrawable) {
        if (drawerArrow.progress == 0f) {
            ValueAnimator.ofFloat(0f, 1f)
        } else {
            ValueAnimator.ofFloat(1f, 0f)
        }.apply {
            addUpdateListener { animation ->
                drawerArrow.progress = animation.animatedValue as Float
            }
            interpolator = DecelerateInterpolator()
            duration = 300
            start()
        }
    }


    override fun onApplySearch(query: String) {

    }

    override fun onClickTitle() {

    }

    override fun onEditTextBackPressed() {

    }

    override fun onFetchSuggestion(query: String) {
        val action = actionTag
        if (!isViewCreated || action == null) {
            return
        }
        if (action.booru.type == BOORU_TYPE_SHIMMIE) return
        if (searchBar.currentState == SearchBar.STATE_SEARCH) {
            action.query = when (action.booru.type) {
                BOORU_TYPE_MOE,
                BOORU_TYPE_DAN,
                BOORU_TYPE_DAN1 -> "$query*"
                else -> query
            }
            suggestionViewModel.fetchSuggestions(action)
        }
    }

    override fun onLeftButtonClick() {
        val activity = activity
        if (activity is MainActivity) {
            activity.openDrawer()
        } else {
            activity?.finish()
        }
    }

    override fun onMenuItemClick(menuItem: MenuItem) {

    }

    override fun onStateChange(newState: Int, oldState: Int, animation: Boolean) {
        if (activity is MainActivity) {
            toggleArrowLeftDrawable()
        } else {
            if (!isViewCreated) {
                return
            }
            fabToListTop.isVisible = newState != SearchBar.STATE_EXPAND
        }
    }

    override val validRecyclerView: RecyclerView
        get() = mainList

    override val isForceShowSearchBar: Boolean
        get() = searchBar.currentState == SearchBar.STATE_SEARCH || searchBar.currentState == SearchBar.STATE_EXPAND

    override fun isValidView(recyclerView: RecyclerView): Boolean =
        searchBar.currentState == SearchBar.STATE_NORMAL && recyclerView == mainList

    fun toListTop() {
        if (!isViewCreated) {
            return
        }
        val itemCount = mainList.adapter?.itemCount
        if (itemCount != null && itemCount > 0) {
            mainList.scrollToPosition(0)
        }
        toNormalState()
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        menu?.add(
            MUZEI_MENU_GROUP_ID,
            MUZEI_MENU_ITEM_ID,
            MUZEI_MENU_ORDER,
            R.string.action_add_to_muzei)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        if (item?.itemId == MUZEI_MENU_ITEM_ID) {
            val query = searchBar.getSelectedText()
            val muzei = Muzei(
                booruUid = activatedBooruUid,
                query = query
            )
            MuzeiManager.createMuzei(muzei)
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {

    }

    override fun onDestroyView() {
        sp.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroyView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            AUTO_HIDE_BOTTOM_BAR_KEY -> setupMainListPadding()
        }
    }

    companion object {
        private const val MUZEI_MENU_GROUP_ID = 101
        private const val MUZEI_MENU_ITEM_ID = 102
        private const val MUZEI_MENU_ORDER = 0
    }
}