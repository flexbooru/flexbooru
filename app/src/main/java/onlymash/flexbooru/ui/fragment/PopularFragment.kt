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
import android.view.*
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.Constants

import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.entity.*
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.popular.PopularRepository
import onlymash.flexbooru.ui.BrowseActivity
import onlymash.flexbooru.ui.MainActivity
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.ui.adapter.PostAdapter
import onlymash.flexbooru.ui.viewholder.PostViewHolder
import onlymash.flexbooru.ui.viewmodel.PopularViewModel
import onlymash.flexbooru.util.gridWidth
import onlymash.flexbooru.widget.AutoStaggeredGridLayoutManager
import onlymash.flexbooru.widget.SearchBar
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
                    else -> throw IllegalArgumentException("unknown booru type ${booru.type}")
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

    private var popular: SearchPopular? = null

    private var currentYear = -1
    private var currentMonth = -1
    private var currentDay = -1

    private val itemListener = object : PostViewHolder.ItemListener {
        override fun onClickMoeItem(post: PostMoe, view: View) {
            BrowseActivity.startActivity(requireActivity(), view, post.id, post.keyword)
        }

        override fun onClickDanItem(post: PostDan, view: View) {
            BrowseActivity.startActivity(requireActivity(), view, post.id, post.keyword)
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
        get() = object : ListFragment.SearchBarHelper {
            override fun onMenuItemClick(menuItem: MenuItem) {
                when (type) {
                    Constants.TYPE_DANBOORU -> {
                        when (menuItem.itemId) {
                            R.id.action_date -> {
                                val currentTimeMillis = System.currentTimeMillis()
                                val currentCalendar = Calendar.getInstance(Locale.getDefault()).apply {
                                    timeInMillis = currentTimeMillis
                                }
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
                                        popular!!.date = date
                                        popularViewModel.show(popular!!)
                                        swipe_refresh.isRefreshing = true
                                        popularViewModel.refreshDan()
                                    },
                                    if (currentYear < 0) currentCalendar.get(Calendar.YEAR) else currentYear,
                                    if (currentMonth < 0) currentCalendar.get(Calendar.MONTH) else currentMonth,
                                    if (currentDay < 0) currentCalendar.get(Calendar.DAY_OF_MONTH) else currentDay
                                ).apply {
                                    datePicker.minDate = minCalendar.timeInMillis
                                    datePicker.maxDate = currentTimeMillis
                                }
                                    .show()
                            }
                            R.id.action_day -> {
                                popular!!.scale = SCALE_DAY
                                popularViewModel.show(popular!!)
                                swipe_refresh.isRefreshing = true
                                popularViewModel.refreshDan()
                            }
                            R.id.action_week -> {
                                popular!!.scale = SCALE_WEEK
                                popularViewModel.show(popular!!)
                                swipe_refresh.isRefreshing = true
                                popularViewModel.refreshDan()
                            }
                            R.id.action_month -> {
                                popular!!.scale = SCALE_MONTH
                                popularViewModel.show(popular!!)
                                swipe_refresh.isRefreshing = true
                                popularViewModel.refreshDan()
                            }
                            else -> throw IllegalArgumentException("unknown menu item. title: ${menuItem.title}")
                        }
                        keyword = popular!!.scale
                    }
                    Constants.TYPE_MOEBOORU -> {
                        when (menuItem.itemId) {
                            R.id.action_day -> popular!!.period = PERIOD_DAY
                            R.id.action_week -> popular!!.period = PERIOD_WEEK
                            R.id.action_month -> popular!!.period = PERIOD_MONTH
                            R.id.action_year -> popular!!.period = PERIOD_YEAR
                            else -> throw IllegalArgumentException("unknown menu item. title: ${menuItem.title}")
                        }
                        keyword = popular!!.period
                        popularViewModel.show(popular!!)
                        swipe_refresh.isRefreshing = true
                        popularViewModel.refreshMoe()
                    }
                }
            }

            override fun onApplySearch(query: String) {
                if (!query.isEmpty()) SearchActivity.startActivity(requireContext(), query)
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
            }
        }
    }

    private val sharedElementCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
            if (currentPostId > 0) {
                val view = list.findViewWithTag<View>(currentPostId) ?: return
                val newSharedElement = view.findViewById<View>(R.id.preview) ?: return
                val newTransitionName = newSharedElement.transitionName ?: return
                names.clear()
                names.add(newTransitionName)
                sharedElements.clear()
                sharedElements[newTransitionName] = newSharedElement
            }
        }
    }

    private val userListener = object : UserManager.Listener {
        override fun onAdd(user: User) {
            updateUserInfoAndRefresh(user)
        }

        override fun onDelete(user: User) {
            if (user.booru_uid != Settings.instance().activeBooruUid) return
            popular!!.username = ""
            popular!!.auth_key = ""
            when (type) {
                Constants.TYPE_DANBOORU -> {
                    popularViewModel.apply {
                        show(popular!!)
                        refreshDan()
                    }
                }
                Constants.TYPE_MOEBOORU -> {
                    popularViewModel.apply {
                        show(popular!!)
                        refreshMoe()
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
                popular!!.username = user.name
                popular!!.auth_key = user.api_key ?: ""
                popularViewModel.apply {
                    show(popular!!)
                    refreshDan()
                }
            }
            Constants.TYPE_MOEBOORU -> {
                popular!!.username = user.name
                popular!!.auth_key = user.password_hash ?: ""
                popularViewModel.apply {
                    show(popular!!)
                    refreshMoe()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getInt(Constants.TYPE_KEY, -1)
            popular = SearchPopular(
                scheme = it.getString(Constants.SCHEME_KEY, "") ,
                host = it.getString(Constants.HOST_KEY, ""),
                username = it.getString(Constants.USERNAME_KEY, ""),
                auth_key = it.getString(Constants.AUTH_KEY, ""),
                safe_mode = Settings.instance().safeMode
            )
        }
        val activity = requireActivity() as MainActivity
        activity.setPopularExitSharedElementCallback(sharedElementCallback)
        activity.registerReceiver(broadcastReceiver, IntentFilter(BrowseActivity.ACTION))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (popular == null) return
        init()
        UserManager.listeners.add(userListener)
    }

    private fun init() {
        search_bar.setTitle(R.string.title_popular)
        search_bar.setEditTextHint(getString(R.string.search_bar_hint_search_posts))
        when (type) {
            Constants.TYPE_DANBOORU -> search_bar.setMenu(R.menu.popular_dan, requireActivity().menuInflater)
            Constants.TYPE_MOEBOORU -> search_bar.setMenu(R.menu.popular_moe, requireActivity().menuInflater)
            else -> throw IllegalArgumentException("unknown type $type")
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
            retryCallback = {
                when (type) {
                    Constants.TYPE_DANBOORU -> popularViewModel.retryDan()
                    Constants.TYPE_MOEBOORU -> popularViewModel.retryMoe()
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
                    postAdapter.submitList(posts as PagedList<Any>)
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
                    postAdapter.submitList(posts as PagedList<Any>)
                })
                popularViewModel.networkStateMoe.observe(this, Observer { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshMoe()
            }
        }
        popularViewModel.show(popular!!)
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

    private fun initSwipeToRefreshMoe() {
        popularViewModel.refreshStateMoe.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { popularViewModel.refreshMoe() }
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
        UserManager.listeners.remove(userListener)
        requireActivity().unregisterReceiver(broadcastReceiver)
        (requireActivity() as MainActivity).removeNavigationListener(navigationListener)
        super.onDestroy()
    }
}
