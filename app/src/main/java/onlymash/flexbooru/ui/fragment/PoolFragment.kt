/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
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

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.entity.pool.PoolBase
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.pool.PoolRepositoryIml
import onlymash.flexbooru.repository.pool.PoolRepository
import onlymash.flexbooru.ui.*
import onlymash.flexbooru.ui.adapter.PoolAdapter
import onlymash.flexbooru.ui.viewholder.PoolViewHolder
import onlymash.flexbooru.ui.viewmodel.PoolViewModel
import onlymash.flexbooru.worker.DownloadWorker
import onlymash.flexbooru.widget.search.SearchBar

class PoolFragment : ListFragment() {

    companion object {
        private const val TAG = "PoolFragment"

        @JvmStatic
        fun newInstance(booru: Booru, user: User?) =
            PoolFragment().apply {
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
    }

    private var type = -1
    private lateinit var search: Search

    override val stateChangeListener: SearchBar.StateChangeListener
        get() = object : SearchBar.StateChangeListener {
            override fun onStateChange(newState: Int, oldState: Int, animation: Boolean) {
                toggleArrowLeftDrawable()
            }
        }
    override val searchBarHelper: SearchBarHelper
        get() = object : SearchBarHelper {
            override fun onMenuItemClick(menuItem: MenuItem) {}
            override fun onApplySearch(query: String) {
                if (type < 0) return
                search.keyword = query
                poolViewModel.show(search)
                swipe_refresh.isRefreshing = true
                when (type) {
                    Constants.TYPE_DANBOORU -> poolViewModel.refreshDan()
                    Constants.TYPE_MOEBOORU -> poolViewModel.refreshMoe()
                    Constants.TYPE_DANBOORU_ONE -> poolViewModel.refreshDanOne()
                    Constants.TYPE_SANKAKU -> poolViewModel.refreshSankaku()
                }
            }
        }

    private lateinit var poolViewModel: PoolViewModel
    private lateinit var poolAdapter: PoolAdapter

    private val itemListener = object : PoolViewHolder.ItemListener {
        override fun onClickUserAvatar(id: Int, name: String?, avatar: String?) {
            startActivity(Intent(requireContext(), AccountActivity::class.java).apply {
                putExtra(AccountActivity.USER_ID_KEY, id)
                putExtra(AccountActivity.USER_NAME_KEY, name)
                putExtra(AccountActivity.USER_AVATAR_KEY, avatar)
            })
        }

        override fun onClickItem(keyword: String) {
            SearchActivity.startActivity(requireContext(), keyword)
        }
    }

    private val userListener = object : UserManager.Listener {
        override fun onAdd(user: User) {
            updateUserInfoAndRefresh(user)
        }
        override fun onDelete(user: User) {
            if (user.booru_uid != Settings.activeBooruUid) return
            search.username = ""
            search.auth_key = ""
            when (type) {
                Constants.TYPE_DANBOORU -> {
                    poolViewModel.apply {
                        show(search)
                        refreshDan()
                    }
                }
                Constants.TYPE_MOEBOORU -> {
                    poolViewModel.apply {
                        show(search)
                        refreshMoe()
                    }
                }
                Constants.TYPE_DANBOORU_ONE -> {
                    poolViewModel.apply {
                        show(search)
                        refreshDanOne()
                    }
                }
                Constants.TYPE_SANKAKU -> {
                    poolViewModel.apply {
                        show(search)
                        refreshSankaku()
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
                search.username = user.name
                search.auth_key = user.api_key ?: ""
                poolViewModel.apply {
                    show(search)
                    refreshDan()
                }
            }
            Constants.TYPE_MOEBOORU -> {
                search.username = user.name
                search.auth_key = user.password_hash ?: ""
                poolViewModel.apply {
                    show(search)
                    refreshMoe()
                }
            }
            Constants.TYPE_DANBOORU_ONE -> {
                search.username = user.name
                search.auth_key = user.password_hash ?: ""
                poolViewModel.apply {
                    show(search)
                    refreshDanOne()
                }
            }
            Constants.TYPE_SANKAKU -> {
                search.username = user.name
                search.auth_key = user.password_hash ?: ""
                poolViewModel.apply {
                    show(search)
                    refreshSankaku()
                }
            }
        }
    }

    private val navigationListener = object : MainActivity.NavigationListener {
        override fun onClickPosition(position: Int) {
            if (position == 2) {
                list.smoothScrollToPosition(0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arg = arguments ?: throw RuntimeException("arg is null")
        type = arg.getInt(Constants.TYPE_KEY, Constants.TYPE_UNKNOWN)
        if (type < 0) return
        search = Search(
            scheme = arg.getString(Constants.SCHEME_KEY, ""),
            host = arg.getString(Constants.HOST_KEY, ""),
            keyword = "",
            username = arg.getString(Constants.USERNAME_KEY, ""),
            auth_key = arg.getString(Constants.AUTH_KEY, ""),
            limit = Settings.pageLimit)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchBar.setTitle(R.string.title_pools)
        searchBar.setEditTextHint(getString(R.string.search_bar_hint_search_pools))
        if (type < 0) {
            list.visibility = View.GONE
            swipe_refresh.visibility = View.GONE
            notSupported.visibility = View.VISIBLE
            return
        }
        poolViewModel = getPoolViewModel(
            PoolRepositoryIml(
                danbooruApi = danApi,
                danbooruOneApi = danOneApi,
                moebooruApi = moeApi,
                sankakuApi = sankakuApi,
                networkExecutor = ioExecutor
            )
        )
        val glide = GlideApp.with(this)
        poolAdapter = PoolAdapter(
            glide = glide,
            listener = itemListener,
            longClickCallback = {
                val context = requireContext()
                AlertDialog.Builder(context)
                    .setTitle("Pool ${it.id}")
                    .setItems(context.resources.getStringArray(R.array.pool_item_action)) { _, which ->
                        if (!Settings.isOrderSuccess) {
                            startActivity(Intent(context, PurchaseActivity::class.java))
                            return@setItems
                        }
                        if (search.username.isEmpty() || search.auth_key.isEmpty()) {
                            startActivity(Intent(context, AccountConfigActivity::class.java))
                            return@setItems
                        }
                        when (which) {
                            0 -> {
                                DownloadWorker.downloadPool(
                                    activity = requireActivity(),
                                    pool = it,
                                    type = DownloadWorker.POOL_DOWNLOAD_TYPE_JPGS,
                                    username = search.username,
                                    passwordHash = search.auth_key
                                    )
                            }
                            1 -> {
                                DownloadWorker.downloadPool(
                                    activity = requireActivity(),
                                    pool = it,
                                    type = DownloadWorker.POOL_DOWNLOAD_TYPE_PNGS,
                                    username = search.username,
                                    passwordHash = search.auth_key
                                )
                            }
                        }
                    }
                    .create()
                    .show()
            },
            retryCallback = {
                when (type) {
                    Constants.TYPE_DANBOORU -> poolViewModel.retryDan()
                    Constants.TYPE_MOEBOORU -> poolViewModel.retryMoe()
                    Constants.TYPE_DANBOORU_ONE -> poolViewModel.retryDanOne()
                    Constants.TYPE_SANKAKU -> poolViewModel.retrySankaku()
                }
            }
        )
        list.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = poolAdapter
        }
        when (type) {
            Constants.TYPE_DANBOORU -> {
                poolViewModel.poolsDan.observe(this, Observer { pools ->
                    @Suppress("UNCHECKED_CAST")
                    poolAdapter.submitList(pools as PagedList<PoolBase>)
                })
                poolViewModel.networkStateDan.observe(this, Observer { networkState ->
                    poolAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                poolViewModel.poolsMoe.observe(this, Observer { pools ->
                    @Suppress("UNCHECKED_CAST")
                    poolAdapter.submitList(pools as PagedList<PoolBase>)
                })
                poolViewModel.networkStateMoe.observe(this, Observer { networkState ->
                    poolAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshMoe()
            }
            Constants.TYPE_DANBOORU_ONE -> {
                poolViewModel.poolsDanOne.observe(this, Observer { pools ->
                    @Suppress("UNCHECKED_CAST")
                    poolAdapter.submitList(pools as PagedList<PoolBase>)
                })
                poolViewModel.networkStateDanOne.observe(this, Observer { networkState ->
                    poolAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDanOne()
            }
            Constants.TYPE_SANKAKU -> {
                poolViewModel.poolsSankaku.observe(this, Observer { pools ->
                    @Suppress("UNCHECKED_CAST")
                    poolAdapter.submitList(pools as PagedList<PoolBase>)
                })
                poolViewModel.networkStateSankaku.observe(this, Observer { networkState ->
                    poolAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshSankaku()
            }
        }
        poolViewModel.show(search = search)
        UserManager.listeners.add(userListener)
        (requireActivity() as MainActivity).addNavigationListener(navigationListener)
    }

    private fun initSwipeToRefreshDan() {
        poolViewModel.refreshStateDan.observe(this, Observer<NetworkState> {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { poolViewModel.refreshDan() }
    }

    private fun initSwipeToRefreshDanOne() {
        poolViewModel.refreshStateDanOne.observe(this, Observer<NetworkState> {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { poolViewModel.refreshDanOne() }
    }

    private fun initSwipeToRefreshMoe() {
        poolViewModel.refreshStateMoe.observe(this, Observer<NetworkState> {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { poolViewModel.refreshMoe() }
    }

    private fun initSwipeToRefreshSankaku() {
        poolViewModel.refreshStateSankaku.observe(this, Observer<NetworkState> {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { poolViewModel.refreshSankaku() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getPoolViewModel(repo: PoolRepository): PoolViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PoolViewModel(repo) as T
            }
        })[PoolViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        if (type < 0) return
        UserManager.listeners.remove(userListener)
        (requireActivity() as MainActivity).removeNavigationListener(navigationListener)
    }
}