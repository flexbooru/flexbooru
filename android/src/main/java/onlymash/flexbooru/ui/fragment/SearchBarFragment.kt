package onlymash.flexbooru.ui.fragment

import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import androidx.annotation.FloatRange
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.AUTO_HIDE_BOTTOM_BAR_KEY
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.common.Settings.autoHideBottomBar
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.common.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.data.action.ActionTag
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.MuzeiManager
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Muzei
import onlymash.flexbooru.data.repository.suggestion.SuggestionRepositoryImpl
import onlymash.flexbooru.ui.activity.MainActivity
import onlymash.flexbooru.ui.viewmodel.*
import onlymash.flexbooru.widget.searchbar.SearchBar
import onlymash.flexbooru.widget.searchbar.SearchBarMover
import org.kodein.di.erased.instance

abstract class SearchBarFragment : BaseFragment(), SearchBar.Helper,
    SearchBar.StateListener, SearchBarMover.Helper, ActionMode.Callback,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val sp by instance<SharedPreferences>()
    val booruApis by instance<BooruApis>()
    private val booruDao by instance<BooruDao>()

    private var actionTag: ActionTag? = null

    private lateinit var booruViewModel: BooruViewModel
    private lateinit var suggestionViewModel: SuggestionViewModel

    private lateinit var searchBar: SearchBar
    private lateinit var searchBarMover: SearchBarMover
    private lateinit var leftDrawable: DrawerArrowDrawable
    internal lateinit var mainList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        booruViewModel = getBooruViewModel(booruDao)
        suggestionViewModel = getSuggestionViewModel(SuggestionRepositoryImpl(booruApis))
        return inflater.inflate(R.layout.fragment_searchbar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainList = view.findViewById(R.id.list)
        setupMainListPadding()
        initSwipeRefresh()
        searchBar = view.findViewById(R.id.search_bar)
        initSearchBar()
        booruViewModel.booru.observe(viewLifecycleOwner, Observer {
            actionTag = if (it == null) {
                null
            } else {
                ActionTag(
                    booru = it,
                    limit = 6,
                    order = "count"
                )
            }
            onBooruLoaded(it)
        })
        booruViewModel.loadBooru(activatedBooruUid)
        suggestionViewModel.suggestions.observe(viewLifecycleOwner, Observer {
            searchBar.updateSuggestions(it)
        })
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    private fun initSwipeRefresh() {
        val start = resources.getDimensionPixelSize(R.dimen.swipe_refresh_layout_offset_start)
        val end = resources.getDimensionPixelSize(R.dimen.swipe_refresh_layout_offset_end)
        swipe_refresh.apply {
            setProgressViewOffset(false, start, end)
            setColorSchemeResources(
                R.color.blue,
                R.color.purple,
                R.color.green,
                R.color.orange,
                R.color.red
            )
        }
    }

    private fun initSearchBar() {
        leftDrawable = DrawerArrowDrawable(context)
        searchBar.setLeftDrawable(leftDrawable)
        searchBar.setHelper(this)
        searchBar.setStateListener(this)
        searchBar.setEditTextHint(getSearchBarHint())
        searchBarMover = SearchBarMover(this, searchBar, list)
        searchBar.setEditTextSelectionModeCallback(this)
    }

    private fun setupMainListPadding() {
        val activity = activity
        if (activity is MainActivity) {
            mainList.updatePadding(bottom = if (autoHideBottomBar) 0 else activity.getNavigationBarHeight())
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

    fun toExpandState() {
        searchBar.toExpandState()
    }

    fun toNormalState() {
        searchBar.toNormalState()
    }

    fun clearSearchBarText() {
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

    abstract fun onBooruLoaded(booru: Booru?)


    override fun onApplySearch(query: String) {

    }

    override fun onClickTitle() {

    }

    override fun onEditTextBackPressed() {

    }

    override fun onFetchSuggestion(query: String) {
        val action = actionTag ?: return
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
        }
    }

    override val validRecyclerView: RecyclerView
        get() = list

    override fun isValidView(recyclerView: RecyclerView): Boolean =
        searchBar.currentState == SearchBar.STATE_NORMAL &&
                recyclerView == list

    override fun forceShowSearchBar(): Boolean {
        return (searchBar.currentState == SearchBar.STATE_SEARCH) ||
                (searchBar.currentState == SearchBar.STATE_EXPAND)
    }

    open fun onBackPressed(): Boolean = true

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

    override fun onDestroy() {
        super.onDestroy()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == AUTO_HIDE_BOTTOM_BAR_KEY) {
            setupMainListPadding()
        }
    }

    companion object {
        private const val MUZEI_MENU_GROUP_ID = 101
        private const val MUZEI_MENU_ITEM_ID = 101
        private const val MUZEI_MENU_ORDER = 0
    }
}