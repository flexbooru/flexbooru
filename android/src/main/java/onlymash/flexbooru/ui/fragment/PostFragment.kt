package onlymash.flexbooru.ui.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.refreshable_list.*
import kotlinx.android.synthetic.main.search_layout.*
import onlymash.flexbooru.R
import onlymash.flexbooru.animation.RippleAnimation
import onlymash.flexbooru.common.Keys.PAGE_TYPE
import onlymash.flexbooru.common.Keys.POST_QUERY
import onlymash.flexbooru.common.Settings.gridWidthResId
import onlymash.flexbooru.common.Settings.pageLimit
import onlymash.flexbooru.common.Settings.safeMode
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.common.Values.PAGE_TYPE_POPULAR
import onlymash.flexbooru.common.Values.PAGE_TYPE_POSTS
import onlymash.flexbooru.data.action.ActionPost
import onlymash.flexbooru.data.database.MyDatabase
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.repository.NetworkState
import onlymash.flexbooru.data.repository.post.PostRepositoryImpl
import onlymash.flexbooru.extension.rotate
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.ui.adapter.PostAdapter
import onlymash.flexbooru.ui.viewmodel.PostViewModel
import onlymash.flexbooru.ui.viewmodel.getPostViewModel
import onlymash.flexbooru.util.ViewTransition
import onlymash.flexbooru.widget.DateRangePickerDialogFragment
import onlymash.flexbooru.widget.searchbar.SearchBar
import org.kodein.di.erased.instance
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.math.roundToInt

private const val SCALE_DAY = "day"
private const val SCALE_WEEK = "week"
private const val SCALE_MONTH = "month"
private const val PERIOD_DAY = "1d"
private const val PERIOD_WEEK = "1w"
private const val PERIOD_MONTH = "1m"
private const val PERIOD_YEAR = "1y"

class PostFragment : ListFragment() {

    private lateinit var date: ActionPost.Date

    private var action: ActionPost? = null
    private var currentPageType: Int = PAGE_TYPE_POSTS
    private lateinit var query: String
    private lateinit var postViewModel: PostViewModel

    private val db by instance<MyDatabase>()
    private val ioExecutor by instance<Executor>()

    private lateinit var postAdapter: PostAdapter

    private lateinit var viewTransition: ViewTransition

    private lateinit var leftButton: ImageButton
    private var rightButton: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentPageType = arguments?.getInt(PAGE_TYPE, PAGE_TYPE_POSTS) ?: PAGE_TYPE_POSTS
        query = if (isPopularPage()) {
            "order:popular"
        } else {
            activity?.intent?.getStringExtra(POST_QUERY) ?: ""
        }
        initDate()
    }

    private fun isPopularPage() = currentPageType == PAGE_TYPE_POPULAR

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        postViewModel = getPostViewModel(PostRepositoryImpl(
            booruApis = booruApis,
            db = db,
            ioExecutor = ioExecutor
        ))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isPopularPage()) {
            setSearchBarTitle(getString(R.string.title_popular))
        } else if (activity is SearchActivity) {
            setLeftDrawableProgress(1.0f)
            setSearchBarTitle(getString(R.string.title_search))
            setSearchBarText(query)
        }
        leftButton = getSearchBarLeftButton()
        viewTransition = ViewTransition(swipe_refresh, search_layout)
        val glide = GlideApp.with(this)
        postAdapter = PostAdapter(glide) {
            postViewModel.retry()
        }
        list.apply {
            layoutManager = StaggeredGridLayoutManager(spanCount, RecyclerView.VERTICAL)
            adapter = postAdapter
        }
        postViewModel.posts.observe(viewLifecycleOwner, Observer {
            postAdapter.submitList(it)
        })
        postViewModel.networkState.observe(viewLifecycleOwner, Observer {
            postAdapter.setNetworkState(it)
        })
        postViewModel.refreshState.observe(viewLifecycleOwner, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            postViewModel.refresh()
        }
    }

    override fun onBooruLoaded(booru: Booru?) {
        if (booru != null) {
            if (action == null) {
                action = ActionPost(
                    booru = booru,
                    user = user,
                    pageType = currentPageType,
                    limit = pageLimit,
                    date = date,
                    isSafeMode = safeMode,
                    query = query
                )
            } else {
                action?.booru = booru
            }
            if (isPopularPage()) {
                if (booru.type == BOORU_TYPE_SANKAKU) {
                    setSearchBarMenu(R.menu.popular_sankaku)
                } else {
                    setSearchBarMenu(R.menu.popular_dan)
                }
            } else {
                setSearchBarMenu(R.menu.post)
                rightButton = view?.findViewById(R.id.action_expand_or_clear)
            }
        } else {
            action == null
        }
        postViewModel.show(action)
    }

    private fun initDate() {
        val calendar = Calendar.getInstance(Locale.US).apply {
            timeInMillis = System.currentTimeMillis()
        }
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        date = ActionPost.Date(
            day = currentDay,
            month = currentMonth,
            year = currentYear,
            dayEnd = currentDay,
            monthEnd = currentMonth,
            yearEnd = currentYear
        )
    }

    private val spanCount: Int
        get() = activity?.getSanCount() ?: 3

    private fun Activity.getSanCount(): Int {
        val itemWidth = resources.getDimensionPixelSize(gridWidthResId)
        val count = (getWindowWidth().toFloat() / itemWidth.toFloat()).roundToInt()
        return if (count < 1) 1 else count
    }

    private fun Activity.getWindowWidth(): Int {
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }

    override fun getSearchBarHint(): CharSequence {
        return getString(R.string.search_bar_hint_search_posts)
    }

    override fun onMenuItemClick(menuItem: MenuItem) {
        super.onMenuItemClick(menuItem)
        when (menuItem.itemId) {
            R.id.action_expand_or_clear -> {
                when(currentState) {
                    SearchBar.STATE_NORMAL -> toExpandState()
                    SearchBar.STATE_SEARCH,
                    SearchBar.STATE_EXPAND -> clearSearchBarText()
                }
            }
            R.id.action_date -> {
                pickDate()
            }
            R.id.action_date_range -> {
                if (action?.booru?.type == BOORU_TYPE_SANKAKU) {
                    pickDateRange()
                }
            }
            R.id.action_day -> {
                action?.let {
                    it.scale = SCALE_DAY
                    it.period = PERIOD_DAY
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_week -> {
                action?.let {
                    it.scale = SCALE_WEEK
                    it.period = PERIOD_WEEK
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_month -> {
                action?.let {
                    it.scale = SCALE_MONTH
                    it.period = PERIOD_MONTH
                    updateActionAndRefresh(it)
                }
            }
        }
    }

    override fun onStateChange(newState: Int, oldState: Int, animation: Boolean) {
        super.onStateChange(newState, oldState, animation)
        rightButton?.rotate(135f)
        when {
            oldState == SearchBar.STATE_NORMAL && newState == SearchBar.STATE_EXPAND -> {
                if (!search_layout.isVisible) {
                    rightButton?.let {
                        RippleAnimation.create(it).setDuration(300).start()
                    }
                    viewTransition.showView(1)
                }
            }
            oldState == SearchBar.STATE_NORMAL && newState == SearchBar.STATE_SEARCH -> {

            }
            oldState == SearchBar.STATE_EXPAND && newState == SearchBar.STATE_NORMAL -> {
                if (!swipe_refresh.isVisible) {
                    RippleAnimation.create(leftButton).setDuration(300).start()
                    viewTransition.showView(0)
                }
            }
            oldState == SearchBar.STATE_SEARCH && newState == SearchBar.STATE_NORMAL -> {

            }
        }
    }

    override fun onBackPressed(): Boolean {
        return if (currentState == SearchBar.STATE_EXPAND) {
            toNormalState()
            false
        } else {
            true
        }
    }

    private fun pickDate() {
        val context = context ?: return
        val currentTimeMillis = System.currentTimeMillis()
        val minCalendar = Calendar.getInstance(Locale.US).apply {
            timeInMillis = currentTimeMillis
            add(Calendar.YEAR, -20)
        }
        DatePickerDialog(
            context,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                date.year = year
                date.month = month
                date.day = dayOfMonth
                action?.let {
                    it.date = date
                    updateActionAndRefresh(it)
                }
            },
            date.year,
            date.month,
            date.day
        ).apply {
            datePicker.apply {
                minDate = minCalendar.timeInMillis
                maxDate = currentTimeMillis
            }
        }
            .show()
    }

    private fun pickDateRange() {
        val context = context ?: return
        val currentTimeMillis = System.currentTimeMillis()
        val minCalendar = Calendar.getInstance(Locale.US).apply {
            timeInMillis = currentTimeMillis
            add(Calendar.YEAR, -20)
        }
        val callback = object : DateRangePickerDialogFragment.OnDateRangeSetListener {
            override fun onDateRangeSet(
                startDay: Int,
                startMonth: Int,
                startYear: Int,
                endDay: Int,
                endMonth: Int,
                endYear: Int
            ) {
                date.apply {
                    day = startDay
                    month = startMonth
                    year = startYear
                    dayEnd = endDay
                    monthEnd = endMonth
                    yearEnd = endYear
                }
                action?.let {
                    it.date = date
                    updateActionAndRefresh(it)
                }
            }
        }
        DateRangePickerDialogFragment.newInstance(
            listener = callback,
            startDay = date.day,
            startMonth = date.month,
            startYear = date.year,
            endDay = date.dayEnd,
            endMonth = date.monthEnd,
            endYear = date.yearEnd,
            minDate = minCalendar.timeInMillis,
            maxDate = currentTimeMillis
        )
            .show(childFragmentManager, "DateRangePicker")
    }

    private fun updateActionAndRefresh(action: ActionPost) {
        postViewModel.show(action)
        postViewModel.refresh()
    }

    override fun onApplySearch(query: String) {
        context?.let {
            SearchActivity.startSearch(it, query)
        }
    }
}