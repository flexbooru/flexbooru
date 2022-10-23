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

package onlymash.flexbooru.ui.fragment

import android.app.Activity
import android.app.SharedElementCallback
import android.content.*
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import onlymash.flexbooru.R
import onlymash.flexbooru.animation.RippleAnimation
import onlymash.flexbooru.app.Keys.PAGE_TYPE
import onlymash.flexbooru.app.Keys.POST_POSITION
import onlymash.flexbooru.app.Keys.POST_QUERY
import onlymash.flexbooru.app.Settings.GRID_MODE_KEY
import onlymash.flexbooru.app.Settings.GRID_RATIO_KEY
import onlymash.flexbooru.app.Settings.GRID_ROUNDED_KEY
import onlymash.flexbooru.app.Settings.GRID_WIDTH_KEY
import onlymash.flexbooru.app.Settings.PAGE_LIMIT_KEY
import onlymash.flexbooru.app.Settings.SAFE_MODE_KEY
import onlymash.flexbooru.app.Settings.SHOW_ALL_TAGS_KEY
import onlymash.flexbooru.app.Settings.SHOW_INFO_BAR_KEY
import onlymash.flexbooru.app.Settings.gridMode
import onlymash.flexbooru.app.Settings.gridRatio
import onlymash.flexbooru.app.Settings.gridWidthResId
import onlymash.flexbooru.app.Settings.isLargeWidth
import onlymash.flexbooru.app.Settings.isRoundedGrid
import onlymash.flexbooru.app.Settings.isShowAllTags
import onlymash.flexbooru.app.Settings.pageLimit
import onlymash.flexbooru.app.Settings.safeMode
import onlymash.flexbooru.app.Settings.showInfoBar
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL_LEGACY
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.app.Values.BOORU_TYPE_UNKNOWN
import onlymash.flexbooru.app.Values.PAGE_TYPE_POPULAR
import onlymash.flexbooru.app.Values.PAGE_TYPE_POSTS
import onlymash.flexbooru.data.action.ActionPost
import onlymash.flexbooru.data.action.ActionVote
import onlymash.flexbooru.data.database.MyDatabase
import onlymash.flexbooru.data.database.TagFilterManager
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.TagFilter
import onlymash.flexbooru.data.repository.favorite.VoteRepositoryImpl
import onlymash.flexbooru.data.repository.post.PostRepositoryImpl
import onlymash.flexbooru.data.repository.tagfilter.TagFilterRepositoryImpl
import onlymash.flexbooru.extension.asMergedLoadStates
import onlymash.flexbooru.extension.getScreenWidthPixels
import onlymash.flexbooru.extension.rotate
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.activity.DetailActivity
import onlymash.flexbooru.ui.activity.SauceNaoActivity
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.ui.adapter.PostAdapter
import onlymash.flexbooru.ui.adapter.StateAdapter
import onlymash.flexbooru.ui.adapter.TagFilterAdapter
import onlymash.flexbooru.ui.base.PathActivity
import onlymash.flexbooru.ui.base.SearchBarFragment
import onlymash.flexbooru.ui.viewmodel.PostViewModel
import onlymash.flexbooru.ui.viewmodel.TagFilterViewModel
import onlymash.flexbooru.ui.viewmodel.getPostViewModel
import onlymash.flexbooru.ui.viewmodel.getTagFilterViewModel
import onlymash.flexbooru.util.ViewTransition
import onlymash.flexbooru.widget.searchbar.SearchBar
import onlymash.flexbooru.worker.DownloadWorker
import org.kodein.di.instance
import java.util.*
import kotlin.math.roundToInt

private const val SCALE_DAY = "day"
private const val SCALE_WEEK = "week"
private const val SCALE_MONTH = "month"
private const val PERIOD_DAY = "1d"
private const val PERIOD_WEEK = "1w"
private const val PERIOD_MONTH = "1m"
private const val PERIOD_YEAR = "1y"

private const val ROTATION_DEGREE = 135f

class PostFragment : SearchBarFragment() {

    private val db by instance<MyDatabase>()

    private val voteRepository by lazy { VoteRepositoryImpl(booruApis, db.postDao()) }

    private lateinit var date: ActionPost.Date

    private var action: ActionPost? = null
    private var currentPageType: Int = PAGE_TYPE_POSTS
    private lateinit var query: String
    private lateinit var postAdapter: PostAdapter
    private lateinit var postViewModel: PostViewModel
    private lateinit var tagFilterAdapter: TagFilterAdapter
    private lateinit var tagFilterViewModel: TagFilterViewModel

    private lateinit var viewTransition: ViewTransition

    private lateinit var leftButton: ImageButton
    private var rightButton: View? = null
    private lateinit var tagsFilterList: RecyclerView

    private var sharedElement: View? = null

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
            db = db,
            booruApis = booruApis
        ))
        tagFilterViewModel = getTagFilterViewModel(TagFilterRepositoryImpl(db.tagFilterDao()))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onSearchBarViewCreated(view: View, savedInstanceState: Bundle?) {
        if (isPopularPage()) {
            setSearchBarTitle(getString(R.string.title_popular))
        } else if (activity is SearchActivity) {
            setLeftDrawableProgress(1.0f)
            setSearchBarTitle(query)
            setSearchBarText(query)
        }
        leftButton = getSearchBarLeftButton()
        tagsFilterList = view.findViewById(R.id.tags_filter_list)
        viewTransition = ViewTransition(swipeRefresh, searchLayout)
        initFilterList()
        initPostsList()
    }

    private fun initPostsList() {
        val glide = GlideApp.with(this)
        postAdapter = PostAdapter(
            glide = glide,
            clickItemCallback = { view, position, tranName ->
                activity?.let {
                    sharedElement = view
                    DetailActivity.start(it, query, position, view, tranName)
                }
            },
            longClickItemCallback = { handleLongClick(it) }
        )
        setupListPadding(isRoundedGrid)
        mainList.apply {
            layoutManager = StaggeredGridLayoutManager(spanCount, RecyclerView.VERTICAL)
            adapter = postAdapter.withLoadStateFooterSafe(StateAdapter(postAdapter))
        }
        postAdapter.addLoadStateListener { loadStates ->
            swipeRefresh.isRefreshing = loadStates.mediator?.refresh is LoadState.Loading
            progressBarHorizontal.isVisible = loadStates.mediator?.append is LoadState.Loading
            updateState(loadStates.mediator?.refresh)
        }
        lifecycleScope.launchWhenCreated {
            postViewModel.posts.collectLatest {
                postAdapter.submitData(it)
            }
        }
        lifecycleScope.launchWhenCreated {
            postAdapter.loadStateFlow
                .asMergedLoadStates()
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collect { mainList.scrollToPosition(0) }
        }
        swipeRefresh.setOnRefreshListener { postAdapter.refresh() }
    }

    private fun setupListPadding(isRounded: Boolean) {
        val paddingResId = if (isRounded) R.dimen.list_padding_horizontal else R.dimen.list_padding_horizontal_reverse
        val paddingHorizontal =  resources.getDimensionPixelSize(paddingResId)
        mainList.updatePadding(
            left = paddingHorizontal,
            right = paddingHorizontal
        )
    }

    override fun onBooruLoaded(booru: Booru?) {
        super.onBooruLoaded(booru)
        if (booru != null) {
            if (action == null) {
                action = ActionPost(
                    booru = booru,
                    pageType = currentPageType,
                    limit = pageLimit,
                    date = date,
                    isSafeMode = safeMode,
                    query = query
                ).also {
                    postViewModel.show(it)
                }
            } else {
                action?.let {
                    val retry = it.booru.type == booru.type && it.booru.host != booru.host
                    it.booru = booru
                    postViewModel.show(it)
                    if (retry) {
                        retry()
                    }
                }
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
                if (currentState != SearchBar.STATE_NORMAL) {
                    rightButton?.rotation = ROTATION_DEGREE
                }
            }
            updateTagsFilterBooru(booru.uid, booru.type)
        } else {
            action = null
            tagFilterAdapter.updateBooru(-1L, BOORU_TYPE_UNKNOWN, arrayOf(), arrayOf(), arrayOf())
        }
    }

    override fun retry() {
        postAdapter.refresh()
    }

    private fun initFilterList() {
        tagFilterAdapter = TagFilterAdapter(
            deleteTagCallback = { deleteTagFilter(it) },
            addSearchBarTextCallback = { createTagFilter() }
        )
        tagsFilterList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexWrap = FlexWrap.WRAP
                flexDirection = FlexDirection.ROW
                alignItems = AlignItems.STRETCH
            }
            adapter = tagFilterAdapter
        }
        tagFilterViewModel.loadTags().observe(viewLifecycleOwner) {
            tagFilterAdapter.updateData(it)
        }
        searchLayout.findViewById<FloatingActionButton>(R.id.action_search).setOnClickListener {
            val tagString = tagFilterAdapter.getSelectedTagsString()
            if (tagString.isNotEmpty()) {
                search(tagString)
            }
        }
    }

    private fun updateTagsFilterBooru(booruUid: Long, booruType: Int) {
        val ratingsRes = if (booruType == BOORU_TYPE_DAN) R.array.filter_rating_danbooru else R.array.filter_rating
        val ratings = resources.getStringArray(ratingsRes)
        val orders = resources.getStringArray(when(booruType) {
            BOORU_TYPE_DAN -> R.array.filter_order_danbooru
            BOORU_TYPE_SANKAKU -> R.array.filter_order_sankaku
            else -> R.array.filter_order
        })
        val thresholds = if (booruType == BOORU_TYPE_SANKAKU) resources.getStringArray(R.array.filter_threshold) else arrayOf()
        tagFilterAdapter.updateBooru(
            booruUid = booruUid,
            booruType = booruType,
            ratings = ratings,
            orders = orders,
            thresholds = thresholds
        )
    }

    private fun createTagFilter() {
        val uid = action?.booru?.uid
        val text = getEditQuery()
        if (uid == null || text.isEmpty()) {
            return
        }
        TagFilterManager.createTagFilter(
            TagFilter(
                booruUid = uid,
                name = text
            )
        )
    }

    private fun deleteTagFilter(tag: TagFilter) {
        val activity = activity
        if (activity == null || activity.isFinishing) {
            return
        }
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.tag_delete_title))
            .setMessage(String.format(getString(R.string.tag_delete_content), tag.name))
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                TagFilterManager.deleteTagFilter(tag)
            }
            .setNegativeButton(R.string.dialog_no, null)
            .create()
            .show()
    }

    private fun initDate() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = MaterialDatePicker.todayInUtcMilliseconds()
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
        val count = (getScreenWidthPixels().toFloat() / itemWidth.toFloat()).roundToInt()
        return if (count < 1) 1 else count
    }

    override fun getSearchBarHint(): CharSequence {
        return getString(R.string.search_bar_hint_search_posts)
    }


    private fun handleLongClick(post: Post) {
        val activity = activity as? PathActivity
        if (activity == null || activity.isFinishing) {
            return
        }
        AlertDialog.Builder(activity)
            .setTitle("Post ${post.id}")
            .setItems(resources.getTextArray(R.array.post_item_action)) { _, which ->
                when (which) {
                    0 -> {
                        action?.apply {
                            DownloadWorker.downloadPost(post, booru.host, activity)
                        }
                    }
                    1 -> {
                        action?.apply {
                            if (booru.user != null && booru.type != BOORU_TYPE_GEL && booru.type != BOORU_TYPE_GEL_LEGACY) {
                                val actionVote = ActionVote(booru, post.id)
                                lifecycleScope.launch {
                                    voteRepository.addFav(actionVote)
                                }
                            }
                        }
                    }
                    2 -> SauceNaoActivity.startSearch(activity, post.sample)
                }
            }
            .create()
            .show()
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
        if (!isViewCreated) {
            return
        }
        rightButton?.rotate(ROTATION_DEGREE)
        when {
            oldState == SearchBar.STATE_NORMAL && newState == SearchBar.STATE_EXPAND -> {
                if (!searchLayout.isVisible) {
                    if (animation) {
                        rightButton?.let {
                            RippleAnimation.create(it).setDuration(300).start()
                        }
                    }
                    viewTransition.showView(1)
                }
            }
            oldState == SearchBar.STATE_NORMAL && newState == SearchBar.STATE_SEARCH -> {

            }
            oldState == SearchBar.STATE_EXPAND && newState == SearchBar.STATE_NORMAL -> {
                if (!swipeRefresh.isVisible) {
                    if (animation) {
                        RippleAnimation.create(leftButton).setDuration(300).start()
                    }
                    viewTransition.showView(0)
                }
            }
            oldState == SearchBar.STATE_SEARCH && newState == SearchBar.STATE_NORMAL -> {

            }
        }
    }

    private fun pickDate() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, date.yearEnd)
            set(Calendar.MONTH, date.monthEnd)
            set(Calendar.DAY_OF_MONTH, date.dayEnd)
        }
        val selectedTime = calendar.timeInMillis
        val calendarConstraints = CalendarConstraints.Builder()
            .setOpenAt(selectedTime)
            .setValidator(DateValidatorPointBackward.now())
            .build()
        val dialog = MaterialDatePicker.Builder.datePicker()
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setCalendarConstraints(calendarConstraints)
            .setSelection(selectedTime)
            .build()
        dialog.addOnPositiveButtonClickListener { time ->
            calendar.timeInMillis = time
            date.yearEnd = calendar.get(Calendar.YEAR)
            date.monthEnd = calendar.get(Calendar.MONTH)
            date.dayEnd = calendar.get(Calendar.DAY_OF_MONTH)
            action?.let {
                it.date = date
                updateActionAndRefresh(it)
            }
        }
        dialog.show(childFragmentManager, "date_picker")
    }

    private fun pickDateRange() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, date.yearStart)
            set(Calendar.MONTH, date.monthStart)
            set(Calendar.DAY_OF_MONTH, date.dayStart)
        }
        val selectionStart = calendar.timeInMillis
        calendar.apply {
            set(Calendar.YEAR, date.yearEnd)
            set(Calendar.MONTH, date.monthEnd)
            set(Calendar.DAY_OF_MONTH, date.dayEnd)
        }
        val selectionEnd = calendar.timeInMillis
        val selection = androidx.core.util.Pair(selectionStart, selectionEnd)
        val calendarConstraints = CalendarConstraints.Builder()
            .setOpenAt(selectionEnd)
            .setValidator(DateValidatorPointBackward.now())
            .build()
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(R.style.MaterialCalendarTheme)
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setCalendarConstraints(calendarConstraints)
            .setSelection(selection)
            .build()
        picker.addOnPositiveButtonClickListener { times ->
            calendar.timeInMillis = times.first
            date.yearStart = calendar.get(Calendar.YEAR)
            date.monthStart = calendar.get(Calendar.MONTH)
            date.dayStart = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.timeInMillis = times.second
            date.yearEnd = calendar.get(Calendar.YEAR)
            date.monthEnd = calendar.get(Calendar.MONTH)
            date.dayEnd = calendar.get(Calendar.DAY_OF_MONTH)
            action?.let {
                it.date = date
                updateActionAndRefresh(it)
            }
        }
        picker.show(childFragmentManager, "date_range_picker")
    }

    private fun updateActionAndRefresh(action: ActionPost) {
        postViewModel.show(action)
        postAdapter.refresh()
    }

    override fun onApplySearch(query: String) {
       search(query)
    }

    private fun search(query: String) {
        val context = context ?: return
        SearchActivity.startSearch(context, query)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        val action = action ?: return
        when (key) {
            SHOW_ALL_TAGS_KEY -> {
                val adapter = tagsFilterList.adapter
                if (adapter is TagFilterAdapter) {
                    adapter.isShowAll = isShowAllTags
                }
            }
            PAGE_LIMIT_KEY -> {
                action.limit = pageLimit
                postViewModel.show(action)
            }
            GRID_ROUNDED_KEY -> {
                val isRounded = isRoundedGrid
                setupListPadding(isRounded)
                postAdapter.isRounded = isRounded
            }
            GRID_MODE_KEY -> {
                postAdapter.isRatioFixed = gridMode == "fixed"
                toListTop()
            }
            GRID_RATIO_KEY -> {
                postAdapter.itemRatio = gridRatio
                toListTop()
            }
            GRID_WIDTH_KEY -> {
                postAdapter.isLargeItemWidth = isLargeWidth
                (mainList.layoutManager as? StaggeredGridLayoutManager)?.spanCount = spanCount
            }
            SAFE_MODE_KEY -> {
                action.isSafeMode = safeMode
                updateActionAndRefresh(action)
                toListTop()
            }
            SHOW_INFO_BAR_KEY -> postAdapter.isShowBar = showInfoBar
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle= intent?.extras ?: return
            if (query != bundle.getString(POST_QUERY)) return
            val position = bundle.getInt(POST_POSITION, -1)
            if (position >= 0 && position < postAdapter.itemCount) {
                mainList.smoothScrollToPosition(position)
                sharedElement = mainList.findViewHolderForAdapterPosition(position)?.itemView?.findViewById(R.id.preview)
            }
        }
    }

    private val sharedElementCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
            if (names == null || sharedElements == null) return
            names.clear()
            sharedElements.clear()
            sharedElement?.let { view ->
                view.transitionName?.let { name ->
                    names.add(name)
                    sharedElements[name] = view
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setActivityExitSharedElementCallback(sharedElementCallback)
        context?.registerReceiver(
            broadcastReceiver,
            IntentFilter(DetailActivity.ACTION_DETAIL_POST_POSITION)
        )
    }

    override fun onStop() {
        super.onStop()
        setActivityExitSharedElementCallback(null)
        context?.unregisterReceiver(broadcastReceiver)
    }

    private fun setActivityExitSharedElementCallback(callback: SharedElementCallback?) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            activity?.setExitSharedElementCallback(callback)
        }
    }
}