package onlymash.flexbooru.ui.fragment

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.action.ActionTag
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.UserManager
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.repository.suggestion.SuggestionRepositoryImpl
import onlymash.flexbooru.ui.activity.MainActivity
import onlymash.flexbooru.ui.viewmodel.BooruViewModel
import onlymash.flexbooru.ui.viewmodel.SuggestionViewModel
import onlymash.flexbooru.ui.viewmodel.getBooruViewModel
import onlymash.flexbooru.ui.viewmodel.getSuggestionViewModel
import onlymash.flexbooru.widget.searchbar.SearchBar
import onlymash.flexbooru.widget.searchbar.SearchBarMover
import org.kodein.di.erased.instance

abstract class ListFragment : BaseFragment(), SearchBar.Helper,
    SearchBar.StateListener, SearchBarMover.Helper {

    private val booruDao by instance<BooruDao>()
    val booruApis by instance<BooruApis>()

    var user: User? = null

    private var actionTag: ActionTag? = null

    private lateinit var booruViewModel: BooruViewModel
    private lateinit var suggestionViewModel: SuggestionViewModel

    private lateinit var searchBar: SearchBar
    private lateinit var searchBarMover: SearchBarMover
    private lateinit var leftDrawable: DrawerArrowDrawable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        booruViewModel = getBooruViewModel(booruDao)
        suggestionViewModel = getSuggestionViewModel(SuggestionRepositoryImpl(booruApis))
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSwipeRefresh()
        searchBar = view.findViewById(R.id.search_bar)
        initSearchBar()
        booruViewModel.booru.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                actionTag = null
                user = null
            } else {
                user = UserManager.getUserByBooruUid(it.uid)
                actionTag = ActionTag(
                    booru = it,
                    user = user,
                    limit = 10,
                    order = "count"
                )
            }
            onBooruLoaded(it)
        })
        booruViewModel.loadBooru(activatedBooruUid)
        suggestionViewModel.suggestions.observe(viewLifecycleOwner, Observer {
            searchBar.updateSuggestions(it)
        })
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
        actionTag?.let {
            it.query = when (it.booru.type) {
                BOORU_TYPE_MOE,
                BOORU_TYPE_DAN,
                BOORU_TYPE_DAN1 -> "$query*"
                else -> query
            }
            suggestionViewModel.fetchSuggestions(it)
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
}