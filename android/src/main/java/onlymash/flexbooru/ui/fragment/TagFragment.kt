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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.pageLimit
import onlymash.flexbooru.app.Values.Tags
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL_LEGACY
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.action.ActionTag
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.repository.tag.TagRepositoryImpl
import onlymash.flexbooru.extension.asMergedLoadStates
import onlymash.flexbooru.ui.adapter.StateAdapter
import onlymash.flexbooru.ui.adapter.TagAdapter
import onlymash.flexbooru.ui.base.SearchBarFragment
import onlymash.flexbooru.ui.viewmodel.TagViewModel
import onlymash.flexbooru.ui.viewmodel.getTagViewModel

private const val ORDER_DATE = "date"
private const val ORDER_NAME = "name"
private const val ORDER_COUNT = "count"

class TagFragment : SearchBarFragment() {

    private var action: ActionTag? = null

    private lateinit var tagViewModel: TagViewModel
    private lateinit var tagAdapter: TagAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tagViewModel = getTagViewModel(TagRepositoryImpl(booruApis))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getSearchBarHint(): CharSequence =
        getString(R.string.search_bar_hint_search_tags)

    override fun onSearchBarViewCreated(view: View, savedInstanceState: Bundle?) {
        setSearchBarTitle(getString(R.string.title_tags))
        tagAdapter = TagAdapter()
        mainList.apply {
            layoutManager = LinearLayoutManager(this@TagFragment.requireContext(), RecyclerView.VERTICAL, false)
            adapter = tagAdapter.withLoadStateFooter(StateAdapter(tagAdapter))
        }
        lifecycleScope.launchWhenCreated {
            tagViewModel.tags.collectLatest {
                tagAdapter.submitData(it)
            }
        }
        tagAdapter.addLoadStateListener { loadStates ->
            swipeRefresh.isRefreshing = loadStates.source.refresh is LoadState.Loading
            progressBarHorizontal.isVisible = loadStates.source.append is LoadState.Loading
            updateState(loadStates.source.refresh)
        }
        lifecycleScope.launchWhenCreated {
            tagAdapter.loadStateFlow
                .asMergedLoadStates()
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collect { mainList.scrollToPosition(0) }
        }
        swipeRefresh.setOnRefreshListener {
            tagAdapter.refresh()
        }
    }

    override fun retry() {
        tagAdapter.refresh()
    }

    override fun onBooruLoaded(booru: Booru?) {
        super.onBooruLoaded(booru)
        if (booru == null) {
            action = null
            return
        }
        if (action == null) {
            action = ActionTag(
                booru = booru,
                limit = tagLimit(booru.type),
                order = ORDER_COUNT
            )
            setSearchBarMenu(when (booru.type) {
                BOORU_TYPE_DAN1 -> R.menu.tag_dan_one
                BOORU_TYPE_DAN -> R.menu.tag_dan
                BOORU_TYPE_MOE -> R.menu.tag_moe
                in arrayOf(BOORU_TYPE_GEL, BOORU_TYPE_GEL_LEGACY) -> R.menu.tag_gel
                else -> R.menu.tag_sankaku
            })
        } else {
            action?.let {
                it.booru = booru
                it.limit = tagLimit(booru.type)
            }
        }
        action?.let {
            if (tagViewModel.show(it)) {
                tagAdapter.refresh()
            }
        }
    }

    private fun tagLimit(booruType: Int): Int {
        return when (booruType) {
            BOORU_TYPE_MOE, BOORU_TYPE_DAN1 -> 20
            else -> pageLimit
        }
    }

    override fun onApplySearch(query: String) {
        super.onApplySearch(query)
        action?.let {
            it.query = query
            tagViewModel.show(it)
            tagAdapter.refresh()
        }
    }

    private fun updateActionAndRefresh(action: ActionTag) {
        tagViewModel.show(action)
        tagAdapter.refresh()
    }

    override fun onMenuItemClick(menuItem: MenuItem) {
        super.onMenuItemClick(menuItem)
        when (menuItem.itemId) {
            R.id.action_tag_order_date -> {
                action?.let {
                    it.order = ORDER_DATE
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_order_name -> {
                action?.let {
                    it.order = ORDER_NAME
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_order_count -> {
                action?.let {
                    it.order = ORDER_COUNT
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_all -> {
                action?.let {
                    it.type = Tags.TYPE_ALL
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_general -> {
                action?.let {
                    it.type = Tags.TYPE_GENERAL
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_artist -> {
                action?.let {
                    it.type = Tags.TYPE_ARTIST
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_copyright -> {
                action?.let {
                    it.type = Tags.TYPE_COPYRIGHT
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_character -> {
                action?.let {
                    it.type = Tags.TYPE_CHARACTER
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_circle -> {
                action?.let {
                    it.type = Tags.TYPE_CIRCLE
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_faults -> {
                action?.let {
                    it.type = Tags.TYPE_FAULTS
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_meta -> {
                action?.let {
                    it.type = Tags.TYPE_META
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_model -> {
                action?.let {
                    it.type = Tags.TYPE_MODEL
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_photo_set -> {
                action?.let {
                    it.type = Tags.TYPE_PHOTO_SET
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_meta_sankaku -> {
                action?.let {
                    it.type = Tags.TYPE_META_SANKAKU
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_genre -> {
                action?.let {
                    it.type = Tags.TYPE_GENRE
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_studio -> {
                action?.let {
                    it.type = Tags.TYPE_STUDIO
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_tag_type_medium -> {
                action?.let {
                    it.type = Tags.TYPE_MEDIUM
                    updateActionAndRefresh(it)
                }
            }
        }
    }
}