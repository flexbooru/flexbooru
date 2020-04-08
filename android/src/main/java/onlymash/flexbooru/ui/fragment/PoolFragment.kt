/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.isOrderSuccess
import onlymash.flexbooru.common.Settings.pageLimit
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.action.ActionPool
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.repository.NetworkState
import onlymash.flexbooru.data.repository.pool.PoolRepositoryImpl
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.activity.AccountConfigActivity
import onlymash.flexbooru.ui.activity.PurchaseActivity
import onlymash.flexbooru.ui.adapter.PoolAdapter
import onlymash.flexbooru.ui.viewmodel.PoolViewModel
import onlymash.flexbooru.ui.viewmodel.getPoolViewModel
import onlymash.flexbooru.worker.DownloadWorker

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSearchBarTitle(getString(R.string.title_pools))
        poolAdapter = PoolAdapter(GlideApp.with(this),
            downloadPoolCallback = { poolId ->
                action?.booru?.let {
                    handlePoolDownload(poolId, it)
                }
        }) {
            poolViewModel.retry()
        }
        mainList.apply {
            layoutManager = LinearLayoutManager(this@PoolFragment.context, RecyclerView.VERTICAL, false)
            adapter = poolAdapter
        }
        poolViewModel.pools.observe(viewLifecycleOwner, Observer { poolList ->
            poolList?.let {
                poolAdapter.submitList(it)
            }
        })
        poolViewModel.networkState.observe(viewLifecycleOwner, Observer {
            poolAdapter.setNetworkState(it)
        })
        poolViewModel.refreshState.observe(viewLifecycleOwner, Observer {
            if (it != NetworkState.LOADING) {
                swipeRefresh.isRefreshing = false
            }
        })
        swipeRefresh.setOnRefreshListener {
            poolViewModel.refresh()
        }
    }

    override fun onBooruLoaded(booru: Booru?) {
        if (booru == null) {
            action = null
            poolViewModel.show(null)
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
        poolViewModel.show(action)
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
            poolViewModel.show(action)
            poolViewModel.refresh()
        }
    }

    private fun handlePoolDownload(poolId: Int, booru: Booru) {
        if (booru.type != BOORU_TYPE_MOE) {
            return
        }
        val activity = activity ?: return
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
                    0 -> {
                        DownloadWorker.downloadPool(activity, poolId, DownloadWorker.POOL_DOWNLOAD_TYPE_JPGS, booru)
                    }
                    1 -> {
                        DownloadWorker.downloadPool(activity, poolId, DownloadWorker.POOL_DOWNLOAD_TYPE_PNGS, booru)
                    }
                }
            }
            .create()
            .show()
    }
}