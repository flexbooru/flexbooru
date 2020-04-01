package onlymash.flexbooru.ui.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.content.SharedPreferences
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
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.refreshable_list.*
import kotlinx.android.synthetic.main.search_layout.*
import onlymash.flexbooru.R
import onlymash.flexbooru.animation.RippleAnimation
import onlymash.flexbooru.common.Keys.PAGE_TYPE
import onlymash.flexbooru.common.Keys.POST_QUERY
import onlymash.flexbooru.common.Settings.GRID_WIDTH_KEY
import onlymash.flexbooru.common.Settings.PAGE_LIMIT_KEY
import onlymash.flexbooru.common.Settings.SAFE_MODE_KEY
import onlymash.flexbooru.common.Settings.SHOW_ALL_TAGS
import onlymash.flexbooru.common.Settings.SHOW_INFO_BAR_KEY
import onlymash.flexbooru.common.Settings.gridWidthResId
import onlymash.flexbooru.common.Settings.isShowAllTags
import onlymash.flexbooru.common.Settings.pageLimit
import onlymash.flexbooru.common.Settings.safeMode
import onlymash.flexbooru.common.Settings.showInfoBar
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.common.Values.PAGE_TYPE_POPULAR
import onlymash.flexbooru.common.Values.PAGE_TYPE_POSTS
import onlymash.flexbooru.data.action.ActionPost
import onlymash.flexbooru.data.database.MyDatabase
import onlymash.flexbooru.data.database.TagFilterManager
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.TagFilter
import onlymash.flexbooru.data.repository.NetworkState
import onlymash.flexbooru.data.repository.post.PostRepositoryImpl
import onlymash.flexbooru.data.repository.tagfilter.TagFilterRepositoryImpl
import onlymash.flexbooru.extension.rotate
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.ui.adapter.PostAdapter
import onlymash.flexbooru.ui.adapter.TagFilterAdapter
import onlymash.flexbooru.ui.viewmodel.PostViewModel
import onlymash.flexbooru.ui.viewmodel.TagFilterViewModel
import onlymash.flexbooru.ui.viewmodel.getPostViewModel
import onlymash.flexbooru.ui.viewmodel.getTagFilterViewModel
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

class PostFragment : SearchBarFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val sp by instance<SharedPreferences>()
    private val db by instance<MyDatabase>()
    private val ioExecutor by instance<Executor>()

    private lateinit var date: ActionPost.Date

    private var action: ActionPost? = null
    private var currentPageType: Int = PAGE_TYPE_POSTS
    private lateinit var query: String
    private lateinit var postViewModel: PostViewModel

    private lateinit var tagFilterViewModel: TagFilterViewModel

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
        tagFilterViewModel = getTagFilterViewModel(TagFilterRepositoryImpl(db.tagFilterDao()))
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
        initPostsList()
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    private fun initPostsList() {
        val glide = GlideApp.with(this)
        postAdapter = PostAdapter(glide, showInfoBar) {
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
            if (tags_filter_list.adapter == null) {
                initFilterList(booru)
            }
        } else {
            action == null
        }
        postViewModel.show(action)
    }

    private fun initFilterList(booru: Booru) {
        val orders = resources.getStringArray(when(booru.type) {
            BOORU_TYPE_DAN -> R.array.filter_order_danbooru
            BOORU_TYPE_SANKAKU -> R.array.filter_order_sankaku
            else -> R.array.filter_order
        })
        val ratings = resources.getStringArray(R.array.filter_rating)
        val thresholds =
            if (booru.type == BOORU_TYPE_SANKAKU)
                resources.getStringArray(R.array.filter_threshold)
            else arrayOf()
        val tagFilterAdapter = TagFilterAdapter(
            orders = orders,
            ratings = ratings,
            thresholds = thresholds,
            booruType = booru.type
        ) {
            val text = getEditQuery()
            if (text.isNotEmpty()) {
                TagFilterManager.createTagFilter(
                    TagFilter(
                        booruUid = booru.uid,
                        name = text
                    )
                )
            }
        }
        tags_filter_list.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexWrap = FlexWrap.WRAP
                flexDirection = FlexDirection.ROW
                alignItems = AlignItems.STRETCH
            }
            adapter = tagFilterAdapter
        }
        tagFilterViewModel.loadTags().observe(viewLifecycleOwner, Observer {
            tagFilterAdapter.updateData(it, booruUid = booru.uid, showAll = isShowAllTags)
        })
        action_search.setOnClickListener {
            val context = context ?: return@setOnClickListener
            val tagString = tagFilterAdapter.getSelectedTagsString()
            if (tagString.isNotEmpty()) {
                SearchActivity.startSearch(context, tagString)
            }
        }
    }

    private fun initDate() {
        val calendar = Calendar.getInstance(Locale.US).apply {
            timeInMillis = System.currentTimeMillis()
        }
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        calendar.add(Calendar.DATE, -1)
        val beforeDay = calendar.get(Calendar.DAY_OF_MONTH)
        val beforeMonth = calendar.get(Calendar.MONTH)
        val beforeYear = calendar.get(Calendar.YEAR)
        date = ActionPost.Date(
            dayStart = beforeDay,
            monthStart = beforeMonth,
            yearStart = beforeYear,
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
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                date.yearEnd = year
                date.monthEnd = month
                date.dayEnd = dayOfMonth
                action?.let {
                    it.date = date
                    updateActionAndRefresh(it)
                }
            },
            date.yearEnd,
            date.monthEnd,
            date.dayEnd
        ).apply {
            datePicker.apply {
                minDate = minCalendar.timeInMillis
                maxDate = currentTimeMillis
            }
        }
            .show()
    }

    private fun pickDateRange() {
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
                    dayStart = startDay
                    monthStart = startMonth
                    yearStart = startYear
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
            startDay = date.dayStart,
            startMonth = date.monthStart,
            startYear = date.yearStart,
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val action = action ?: return
        when (key) {
            SHOW_ALL_TAGS -> {
                val adapter = tags_filter_list.adapter
                if (adapter is TagFilterAdapter) {
                    adapter.updateData(action.booru.uid, isShowAllTags)
                }
            }
            PAGE_LIMIT_KEY -> {
                action.limit = pageLimit
                postViewModel.show(action)
            }
            GRID_WIDTH_KEY -> {
                list.layoutManager = StaggeredGridLayoutManager(spanCount, RecyclerView.VERTICAL)
            }
            SAFE_MODE_KEY -> {
                action.isSafeMode = safeMode
                updateActionAndRefresh(action)
            }
            SHOW_INFO_BAR_KEY -> {
                postAdapter.showInfoBar = showInfoBar
                postAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }
}