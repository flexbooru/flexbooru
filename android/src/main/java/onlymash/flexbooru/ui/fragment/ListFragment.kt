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

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.empty_list_network_state.*
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.api.*
import onlymash.flexbooru.entity.tag.SearchTag
import onlymash.flexbooru.extension.toVisibility
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.Status
import onlymash.flexbooru.repository.suggestion.SuggestionRepository
import onlymash.flexbooru.repository.suggestion.SuggestionRepositoryImpl
import onlymash.flexbooru.ui.MainActivity
import onlymash.flexbooru.ui.viewmodel.SuggestionViewModel
import onlymash.flexbooru.widget.search.SearchBar
import onlymash.flexbooru.widget.search.SearchBarMover
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.erased.instance
import java.util.concurrent.Executor

abstract class ListFragment : Fragment(), KodeinAware {

    override val kodein by kodein()

    internal val danApi: DanbooruApi by instance()
    internal val danOneApi: DanbooruOneApi by instance()
    internal val moeApi: MoebooruApi by instance()
    internal val gelApi: GelbooruApi by instance()
    internal val sankakuApi: SankakuApi by instance()
    internal val ioExecutor: Executor by instance()

    internal lateinit var searchBar: SearchBar
    internal lateinit var leftDrawable: DrawerArrowDrawable
    internal lateinit var notSupported: AppCompatTextView
    private lateinit var searchBarMover: SearchBarMover

    interface SearchBarHelper {
        fun onMenuItemClick(menuItem: MenuItem)
        fun onApplySearch(query: String)
    }

    abstract val searchBarHelper: SearchBarHelper

    abstract val stateChangeListener: SearchBar.StateChangeListener

    private val suggestionViewModel by lazy {
        getSuggestionViewModel(
            SuggestionRepositoryImpl(
                danbooruApi = danApi,
                moebooruApi = moeApi,
                danbooruOneApi = danOneApi,
                gelbooruApi = gelApi,
                sankakuApi = sankakuApi
            )
        )
    }

    private val helper = object : SearchBar.Helper {
        override fun onLeftButtonClick() {
            val activity = requireActivity()
            if (activity is MainActivity) {
                activity.drawer.openDrawer()
            } else if (activity !is MainActivity) {
                activity.onBackPressed()
            }
        }

        override fun onMenuItemClick(menuItem: MenuItem) {
            searchBarHelper.onMenuItemClick(menuItem)
        }

        override fun onClickTitle() {

        }

        override fun onSearchEditTextClick() {

        }

        override fun onApplySearch(query: String) {
            searchBarHelper.onApplySearch(query)
        }

        override fun onSearchEditTextBackPressed() {

        }

        override fun onFetchSuggestionOnline(type: Int, search: SearchTag) {
            suggestionViewModel.fetchSuggestionsOnline(type, search)
        }
    }

    internal fun toggleArrowLeftDrawable() {
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

    private val sbMoverHelper: SearchBarMover.Helper
        get() = object : SearchBarMover.Helper {
            override val validRecyclerView: RecyclerView
                get() = list

            override fun isValidView(recyclerView: RecyclerView): Boolean {
                return searchBar.getState() == SearchBar.STATE_NORMAL && recyclerView == list
            }

            override fun forceShowSearchBar(): Boolean {
                return searchBar.getState() == SearchBar.STATE_SEARCH
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchBar = view.findViewById(R.id.search_bar)
        notSupported = view.findViewById(R.id.not_supported)
        initSwipeRefresh()
        initSearchBar()
        retry_button_empty.setOnClickListener {
            retry()
        }
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
        leftDrawable = DrawerArrowDrawable(requireContext())
        searchBar.apply {
            setLeftDrawable(leftDrawable)
            setHelper(helper)
            setStateChangeListener(stateChangeListener)
        }
        searchBarMover = SearchBarMover(sbMoverHelper, searchBar, list)
        suggestionViewModel.loadSuggestions(Settings.activeBooruUid).observe(this, Observer {
            searchBar.updateSuggestions(it)
        })
        suggestionViewModel.suggestionsOnline.observe(this, Observer {
            searchBar.updateOnlineSuggestions(it)
        })
    }

    @Suppress("UNCHECKED_CAST")
    private fun getSuggestionViewModel(repo: SuggestionRepository): SuggestionViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return SuggestionViewModel(repo) as T
            }
        })[SuggestionViewModel::class.java]
    }

    internal fun showSearchBar() {
        searchBarMover.showSearchBar(true)
    }

    internal fun handleNetworkState(state: NetworkState, itemCount: Int) {
        val isEmpty = itemCount == 2 && state != NetworkState.LOADED
        progress_bar_container.toVisibility(isEmpty)
        progress_bar_empty.toVisibility(state.status == Status.RUNNING && isEmpty)
        retry_button_empty.toVisibility(state.status == Status.FAILED && isEmpty)
        error_msg_empty.toVisibility(state.msg != null && isEmpty)
        error_msg_empty.text = state.msg
    }

    abstract fun retry()

    abstract val isUnsupported: Boolean
}