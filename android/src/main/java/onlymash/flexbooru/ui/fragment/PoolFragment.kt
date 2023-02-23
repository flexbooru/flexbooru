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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.isOrderSuccess
import onlymash.flexbooru.app.Settings.pageLimit
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.action.ActionPool
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.repository.pool.PoolRepositoryImpl
import onlymash.flexbooru.extension.asMergedLoadStates
import onlymash.flexbooru.extension.launchUrl
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.activity.AccountConfigActivity
import onlymash.flexbooru.ui.activity.PurchaseActivity
import onlymash.flexbooru.ui.adapter.PoolAdapter
import onlymash.flexbooru.ui.adapter.StateAdapter
import onlymash.flexbooru.ui.base.PathActivity
import onlymash.flexbooru.ui.base.SearchBarFragment
import onlymash.flexbooru.ui.viewmodel.PoolViewModel
import onlymash.flexbooru.ui.viewmodel.getPoolViewModel

class PoolFragment : SearchBarFragment() {

    private var action: ActionPool? = null

    private lateinit var poolViewModel: PoolViewModel
    private lateinit var poolAdapter: PoolAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        poolViewModel = getPoolViewModel(PoolRepositoryImpl(booruApis))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getSearchBarHint(): CharSequence =
        getString(R.string.search_bar_hint_search_pools)

    override fun onSearchBarViewCreated(view: View, savedInstanceState: Bundle?) {
        setSearchBarTitle(getString(R.string.title_pools))
        poolAdapter = PoolAdapter(GlideApp.with(this),
            downloadPoolCallback = { poolId ->
                action?.booru?.let {
                    handlePoolDownload(poolId, it)
                }
        })
        mainList.apply {
            layoutManager = LinearLayoutManager(this@PoolFragment.requireContext(), RecyclerView.VERTICAL, false)
            adapter = poolAdapter.withLoadStateFooter(StateAdapter(poolAdapter))
        }
        lifecycleScope.launch {
            poolViewModel.pools.collectLatest {
                poolAdapter.submitData(it)
            }
        }
        poolAdapter.addLoadStateListener { loadStates ->
            swipeRefresh.isRefreshing = loadStates.source.refresh is LoadState.Loading
            progressBarHorizontal.isVisible = loadStates.source.append is LoadState.Loading
            updateState(loadStates.source.refresh)
        }
        lifecycleScope.launch {
            poolAdapter.loadStateFlow
                .asMergedLoadStates()
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collect { mainList.scrollToPosition(0) }
        }
        swipeRefresh.setOnRefreshListener {
            poolAdapter.refresh()
        }
    }

    override fun retry() {
        poolAdapter.refresh()
    }

    override fun onBooruLoaded(booru: Booru?) {
        super.onBooruLoaded(booru)
        if (booru == null) {
            action = null
            return
        }
        if (action == null) {
            action = ActionPool(
                booru = booru,
                limit = poolLimit(booru.type)
            )
        } else {
            action?.let {
                it.booru = booru
                it.limit = poolLimit(booru.type)
            }
        }
        action?.let {
            if (poolViewModel.show(it)) {
                poolAdapter.refresh()
            }
        }
    }

    private fun poolLimit(booruType: Int): Int {
        return when (booruType) {
            BOORU_TYPE_MOE, BOORU_TYPE_DAN1 -> 20
            else -> pageLimit
        }
    }

    override fun onApplySearch(query: String) {
        super.onApplySearch(query)
        action?.let {
            it.query = query
            if (poolViewModel.show(it)) {
                poolAdapter.refresh()
            }
        }
    }

    private fun handlePoolDownload(poolId: Int, booru: Booru) {
        if (booru.type != BOORU_TYPE_MOE) {
            return
        }
        val activity = activity as? PathActivity ?: return
        AlertDialog.Builder(activity)
            .setTitle("Pool $poolId")
            .setItems(activity.resources.getStringArray(R.array.pool_item_action)) { _, which ->
                if (!isOrderSuccess) {
                    startActivity(Intent(activity, PurchaseActivity::class.java))
                    return@setItems
                }
                if (booru.user == null) {
                    startActivity(Intent(activity, AccountConfigActivity::class.java))
                    return@setItems
                }
                when (which) {
                    0 -> downloadPool(booru, poolId, "jpeg")
                    1 -> downloadPool(booru, poolId, "png")
                }
            }
            .create()
            .show()
    }

    private fun downloadPool(booru: Booru, poolId: Int, imageType: String) {
        val url = HttpUrl.Builder()
            .scheme(booru.scheme)
            .host(booru.host)
            .addPathSegments("/pool/zip/$poolId")
            .addQueryParameter(imageType, "1")
            .addQueryParameter("login", booru.user?.name)
            .addQueryParameter("password_hash", booru.user?.token)
            .build()
        context?.launchUrl(url.toString())
    }
}