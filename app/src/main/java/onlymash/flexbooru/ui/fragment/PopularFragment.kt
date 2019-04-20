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

package onlymash.flexbooru.ui.fragment

import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.entity.Vote
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.popular.PopularRepository
import onlymash.flexbooru.ui.AccountConfigActivity
import onlymash.flexbooru.ui.BrowseActivity
import onlymash.flexbooru.ui.MainActivity
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.ui.adapter.PostAdapter
import onlymash.flexbooru.ui.viewholder.PostViewHolder
import onlymash.flexbooru.ui.viewmodel.PopularViewModel
import onlymash.flexbooru.util.DownloadUtil
import onlymash.flexbooru.util.gridWidth
import onlymash.flexbooru.widget.AutoStaggeredGridLayoutManager
import onlymash.flexbooru.widget.search.SearchBar
import java.util.*


/**
 * Use the [PopularFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PopularFragment : ListFragment() {

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param booru
         * @return A new instance of fragment PopularFragment.
         */
        @JvmStatic
        fun newInstance(booru: Booru, user: User?) =
            PopularFragment().apply {
                arguments = when (booru.type) {
                    Constants.TYPE_DANBOORU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_DANBOORU)
                        if (user != null) {
                            putString(Constants.USERNAME_KEY, user.name)
                            putString(Constants.AUTH_KEY, user.api_key)
                        } else {
                            putString(Constants.USERNAME_KEY, "")
                            putString(Constants.AUTH_KEY, "")
                        }
                    }
                    Constants.TYPE_MOEBOORU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_MOEBOORU)
                        if (user != null) {
                            putString(Constants.USERNAME_KEY, user.name)
                            putString(Constants.AUTH_KEY, user.password_hash)
                        } else {
                            putString(Constants.USERNAME_KEY, "")
                            putString(Constants.AUTH_KEY, "")
                        }
                    }
                    Constants.TYPE_DANBOORU_ONE -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_DANBOORU_ONE)
                        if (user != null) {
                            putString(Constants.USERNAME_KEY, user.name)
                            putString(Constants.AUTH_KEY, user.password_hash)
                        } else {
                            putString(Constants.USERNAME_KEY, "")
                            putString(Constants.AUTH_KEY, "")
                        }
                    }
                    Constants.TYPE_SANKAKU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_SANKAKU)
                        if (user != null) {
                            putString(Constants.USERNAME_KEY, user.name)
                            putString(Constants.AUTH_KEY, user.password_hash)
                        } else {
                            putString(Constants.USERNAME_KEY, "")
                            putString(Constants.AUTH_KEY, "")
                        }
                    }
                    else -> Bundle().apply {
                        putInt(Constants.TYPE_KEY, Constants.TYPE_UNKNOWN)
                    }
                }
            }

        private const val SCALE_DAY = "day"
        private const val SCALE_WEEK = "week"
        private const val SCALE_MONTH = "month"
        private const val PERIOD_DAY = "1d"
        private const val PERIOD_WEEK = "1w"
        private const val PERIOD_MONTH = "1m"
        private const val PERIOD_YEAR = "1y"
    }
    private var type: Int = -1

    private lateinit var popular: SearchPopular

    private var currentYear = -1
    private var currentMonth = -1
    private var currentDay = -1

    private val voteRepo by lazy { ServiceLocator.instance().getVoteRepository() }

    private val itemListener = object : PostViewHolder.ItemListener {
        override fun onClickItem(post: PostBase?, view: View) {
            if (post == null) return
            currentPostId = post.getPostId()
            (requireActivity() as MainActivity).sharedElement = list.findViewWithTag<View>(currentPostId)?.findViewById(R.id.preview)
            BrowseActivity.startActivity(requireActivity(), view, currentPostId, post.keyword, Constants.PAGE_TYPE_POPULAR)
        }

        override fun onLongClickItem(post: PostBase?) {
            if (post == null) return
            val id = post.getPostId()
            val context = requireContext()
            AlertDialog.Builder(context)
                .setTitle("Post $id")
                .setItems(context.resources.getTextArray(R.array.post_item_action)) { _, which ->
                    when (which) {
                        0 -> {
                            DownloadUtil.downloadPost(post, requireActivity())
                        }
                        1 -> {
                            if (popular.auth_key.isEmpty()) {
                                requireActivity().startActivity(Intent(requireActivity(), AccountConfigActivity::class.java))
                            } else {
                                val vote = Vote(
                                    scheme = popular.scheme,
                                    host = popular.host,
                                    post_id = id,
                                    score = 3,
                                    username = popular.username,
                                    auth_key = popular.auth_key
                                )
                                when (post) {
                                    is PostDan -> voteRepo.addDanFav(vote, post)
                                    is PostMoe -> voteRepo.voteMoePost(vote)
                                    is PostDanOne -> voteRepo.addDanOneFav(vote, post)
                                    is PostSankaku -> voteRepo.addSankakuFav(vote, post)
                                }
                            }
                        }
                    }
                }
                .create()
                .show()
        }
    }

    private lateinit var popularViewModel: PopularViewModel
    private lateinit var glide: GlideRequests

    private lateinit var postAdapter: PostAdapter

    private var keyword = ""
    private var date = ""

    override val stateChangeListener: SearchBar.StateChangeListener
        get() = object : SearchBar.StateChangeListener {
            override fun onStateChange(newState: Int, oldState: Int, animation: Boolean) {
                toggleArrowLeftDrawable()
            }
        }
    override val searchBarHelper: SearchBarHelper
        get() = object : SearchBarHelper {
            override fun onMenuItemClick(menuItem: MenuItem) {
                when (type) {
                    Constants.TYPE_DANBOORU -> {
                        when (menuItem.itemId) {
                            R.id.action_date -> {
                                val currentTimeMillis = System.currentTimeMillis()
                                val minCalendar = Calendar.getInstance(Locale.getDefault()).apply {
                                    timeInMillis = currentTimeMillis
                                    add(Calendar.YEAR, -20)
                                }
                                DatePickerDialog(
                                    requireContext(),
                                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                                        currentYear = year
                                        currentMonth = month
                                        currentDay = dayOfMonth
                                        val yearString = year.toString()
                                        val realMonth = month + 1
                                        val monthString = if (realMonth < 10) "0$realMonth" else realMonth.toString()
                                        val dayString = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
                                        date = "$yearString-$monthString-$dayString"
                                        popular.date = date
                                        popularViewModel.show(popular)
                                        swipe_refresh.isRefreshing = true
                                        popularViewModel.refreshDan()
                                    },
                                    currentYear,
                                    currentMonth,
                                    currentDay
                                ).apply {
                                    datePicker.minDate = minCalendar.timeInMillis
                                    datePicker.maxDate = currentTimeMillis
                                }
                                    .show()
                            }
                            R.id.action_day -> {
                                popular.scale = SCALE_DAY
                                popularViewModel.show(popular)
                                swipe_refresh.isRefreshing = true
                                popularViewModel.refreshDan()
                            }
                            R.id.action_week -> {
                                popular.scale = SCALE_WEEK
                                popularViewModel.show(popular)
                                swipe_refresh.isRefreshing = true
                                popularViewModel.refreshDan()
                            }
                            R.id.action_month -> {
                                popular.scale = SCALE_MONTH
                                popularViewModel.show(popular)
                                swipe_refresh.isRefreshing = true
                                popularViewModel.refreshDan()
                            }
                            else -> throw IllegalArgumentException("unknown menu item. title: ${menuItem.title}")
                        }
                        keyword = popular.scale
                    }
                    Constants.TYPE_MOEBOORU -> {
                        when (menuItem.itemId) {
                            R.id.action_day -> popular.period = PERIOD_DAY
                            R.id.action_week -> popular.period = PERIOD_WEEK
                            R.id.action_month -> popular.period = PERIOD_MONTH
                            R.id.action_year -> popular.period = PERIOD_YEAR
                            else -> throw IllegalArgumentException("unknown menu item. title: ${menuItem.title}")
                        }
                        keyword = popular.period
                        popularViewModel.show(popular)
                        swipe_refresh.isRefreshing = true
                        popularViewModel.refreshMoe()
                    }
                    Constants.TYPE_DANBOORU_ONE -> {
                        when (menuItem.itemId) {
                            R.id.action_date -> {
                                val currentTimeMillis = System.currentTimeMillis()
                                val minCalendar = Calendar.getInstance(Locale.getDefault()).apply {
                                    timeInMillis = currentTimeMillis
                                    add(Calendar.YEAR, -20)
                                }
                                DatePickerDialog(
                                    requireContext(),
                                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                                        currentYear = year
                                        currentMonth = month
                                        currentDay = dayOfMonth
                                        val yearString = year.toString()
                                        val realMonth = month + 1
                                        val monthString = if (realMonth < 10) "0$realMonth" else realMonth.toString()
                                        val dayString = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
                                        date = "$yearString-$monthString-$dayString"
                                        popular.date = date
                                        popular.day = dayString
                                        popular.month = monthString
                                        popular.year = yearString
                                        popularViewModel.show(popular)
                                        swipe_refresh.isRefreshing = true
                                        popularViewModel.refreshDanOne()
                                    },
                                    currentYear,
                                    currentMonth,
                                    currentDay
                                ).apply {
                                    datePicker.minDate = minCalendar.timeInMillis
                                    datePicker.maxDate = currentTimeMillis
                                }
                                    .show()
                            }
                            R.id.action_day -> {
                                popular.scale = SCALE_DAY
                                popularViewModel.show(popular)
                                swipe_refresh.isRefreshing = true
                                popularViewModel.refreshDanOne()
                            }
                            R.id.action_week -> {
                                popular.scale = SCALE_WEEK
                                popularViewModel.show(popular)
                                swipe_refresh.isRefreshing = true
                                popularViewModel.refreshDanOne()
                            }
                            R.id.action_month -> {
                                popular.scale = SCALE_MONTH
                                popularViewModel.show(popular)
                                swipe_refresh.isRefreshing = true
                                popularViewModel.refreshDanOne()
                            }
                            else -> throw IllegalArgumentException("unknown menu item. title: ${menuItem.title}")
                        }
                        keyword = popular.scale
                    }
                }
            }

            override fun onApplySearch(query: String) {
                if (query.isNotEmpty()) SearchActivity.startActivity(requireContext(), query)
            }
        }

    private val navigationListener = object : MainActivity.NavigationListener {
        override fun onClickPosition(position: Int) {
            if (position == 1) {
                list.smoothScrollToPosition(0)
            }
        }
    }

    private var currentPostId = 0
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val bundle= intent.extras ?: return
            val pos = bundle.getInt(BrowseActivity.EXT_POST_POSITION_KEY, -1)
            val key = bundle.getString(BrowseActivity.EXT_POST_KEYWORD_KEY)
            if (pos >= 0 && keyword == key) {
                currentPostId = bundle.getInt(BrowseActivity.EXT_POST_ID_KEY, currentPostId)
                list.smoothScrollToPosition(pos + 1)
                (requireActivity() as MainActivity).sharedElement =
                    list.findViewWithTag<View>(currentPostId)?.findViewById(R.id.preview)
            }
        }
    }

    private val userListener = object : UserManager.Listener {
        override fun onAdd(user: User) {
            updateUserInfoAndRefresh(user)
        }

        override fun onDelete(user: User) {
            if (user.booru_uid != Settings.instance().activeBooruUid) return
            popular.username = ""
            popular.auth_key = ""
            when (type) {
                Constants.TYPE_DANBOORU -> {
                    popularViewModel.apply {
                        show(popular)
                        refreshDan()
                    }
                }
                Constants.TYPE_MOEBOORU -> {
                    popularViewModel.apply {
                        show(popular)
                        refreshMoe()
                    }
                }
                Constants.TYPE_DANBOORU_ONE -> {
                    popularViewModel.apply {
                        show(popular)
                        refreshDanOne()
                    }
                }
            }
        }

        override fun onUpdate(user: User) {
            updateUserInfoAndRefresh(user)
        }

    }

    private fun updateUserInfoAndRefresh(user: User) {
        when (type) {
            Constants.TYPE_DANBOORU -> {
                popular.username = user.name
                popular.auth_key = user.api_key ?: ""
                popularViewModel.apply {
                    show(popular)
                    refreshDan()
                }
            }
            Constants.TYPE_MOEBOORU -> {
                popular.username = user.name
                popular.auth_key = user.password_hash ?: ""
                popularViewModel.apply {
                    show(popular)
                    refreshMoe()
                }
            }
            Constants.TYPE_DANBOORU_ONE -> {
                popular.username = user.name
                popular.auth_key = user.password_hash ?: ""
                popularViewModel.apply {
                    show(popular)
                    refreshDanOne()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arg = arguments ?: throw RuntimeException("arg is null")
        type = arg.getInt(Constants.TYPE_KEY, -1)
        if (type < 0) return
        popular = SearchPopular(
            scheme = arg.getString(Constants.SCHEME_KEY, ""),
            host = arg.getString(Constants.HOST_KEY, ""),
            username = arg.getString(Constants.USERNAME_KEY, ""),
            auth_key = arg.getString(Constants.AUTH_KEY, ""),
            safe_mode = Settings.instance().safeMode
        )
        val currentTimeMillis = System.currentTimeMillis()
        val currentCalendar = Calendar.getInstance(Locale.getDefault()).apply {
            timeInMillis = currentTimeMillis
        }
        currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
        currentMonth = currentCalendar.get(Calendar.MONTH)
        currentYear = currentCalendar.get(Calendar.YEAR)
        val yearString = currentYear.toString()
        val realMonth = currentMonth + 1
        val monthString = if (realMonth < 10) "0$realMonth" else realMonth.toString()
        val dayString = if (currentDay < 10) "0$currentDay" else currentDay.toString()
        popular.year = yearString
        popular.month = monthString
        popular.day = dayString
        requireActivity().registerReceiver(broadcastReceiver, IntentFilter(BrowseActivity.ACTION))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        if (type < 0) return
        UserManager.listeners.add(userListener)
    }

    private fun init() {
        searchBar.setTitle(R.string.title_popular)
        searchBar.setEditTextHint(getString(R.string.search_bar_hint_search_posts))
        if (type < 0) {
            list.visibility = View.GONE
            swipe_refresh.visibility = View.GONE
            notSupported.visibility = View.VISIBLE
            return
        }
        when (type) {
            Constants.TYPE_DANBOORU -> searchBar.setMenu(R.menu.popular_dan, requireActivity().menuInflater)
            Constants.TYPE_MOEBOORU -> searchBar.setMenu(R.menu.popular_moe, requireActivity().menuInflater)
            Constants.TYPE_DANBOORU_ONE -> searchBar.setMenu(R.menu.popular_dan, requireActivity().menuInflater)
        }
        popularViewModel = getPopularViewModel(ServiceLocator.instance().getPopularRepository())
        glide = GlideApp.with(this)
        val staggeredGridLayoutManager = AutoStaggeredGridLayoutManager(
            columnSize = resources.gridWidth(),
            orientation = StaggeredGridLayoutManager.VERTICAL).apply {
            setStrategy(AutoStaggeredGridLayoutManager.STRATEGY_SUITABLE_SIZE)
        }
        postAdapter = PostAdapter(
            glide = glide,
            listener = itemListener,
            showInfoBar = Settings.instance().showInfoBar,
            pageType = Constants.PAGE_TYPE_POPULAR,
            retryCallback = {
                when (type) {
                    Constants.TYPE_DANBOORU -> popularViewModel.retryDan()
                    Constants.TYPE_MOEBOORU -> popularViewModel.retryMoe()
                    Constants.TYPE_DANBOORU_ONE -> popularViewModel.retryDanOne()
                    Constants.TYPE_SANKAKU -> popularViewModel.retrySankaku()
                }
            })
        list.apply {
            setHasFixedSize(true)
            layoutManager = staggeredGridLayoutManager
            adapter = postAdapter
        }
        when (type) {
            Constants.TYPE_DANBOORU -> {
                keyword = SCALE_DAY
                popularViewModel.postsDan.observe(this, Observer<PagedList<PostDan>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                popularViewModel.networkStateDan.observe(this, Observer { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                keyword = PERIOD_DAY
                popularViewModel.postsMoe.observe(this, Observer<PagedList<PostMoe>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                popularViewModel.networkStateMoe.observe(this, Observer { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshMoe()
            }
            Constants.TYPE_DANBOORU_ONE -> {
                keyword = SCALE_DAY
                popularViewModel.postsDanOne.observe(this, Observer<PagedList<PostDanOne>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                popularViewModel.networkStateDanOne.observe(this, Observer { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDanOne()
            }
            Constants.TYPE_SANKAKU -> {
                keyword = SCALE_DAY
                popularViewModel.postsSankaku.observe(this, Observer<PagedList<PostSankaku>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                popularViewModel.networkStateSankaku.observe(this, Observer { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshSankaku()
            }
        }
        popularViewModel.show(popular)
        (requireActivity() as MainActivity).addNavigationListener(navigationListener)
    }

    private fun initSwipeToRefreshDan() {
        popularViewModel.refreshStateDan.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { popularViewModel.refreshDan() }
    }

    private fun initSwipeToRefreshDanOne() {
        popularViewModel.refreshStateDanOne.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { popularViewModel.refreshDanOne() }
    }

    private fun initSwipeToRefreshMoe() {
        popularViewModel.refreshStateMoe.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { popularViewModel.refreshMoe() }
    }

    private fun initSwipeToRefreshSankaku() {
        popularViewModel.refreshStateSankaku.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { popularViewModel.refreshSankaku() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getPopularViewModel(repo: PopularRepository): PopularViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PopularViewModel(repo) as T
            }
        })[PopularViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        if (type < 0) return
        UserManager.listeners.remove(userListener)
        (requireActivity() as MainActivity).apply {
            unregisterReceiver(broadcastReceiver)
            removeNavigationListener(navigationListener)
            sharedElement = null
        }
    }
}
