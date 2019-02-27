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

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.R
import onlymash.flexbooru.ui.MainActivity
import onlymash.flexbooru.widget.SearchBar
import onlymash.flexbooru.widget.SearchBarMover

abstract class ListFragment : Fragment() {

    internal lateinit var leftDrawable: DrawerArrowDrawable
    private lateinit var searchBarMover: SearchBarMover

    interface SearchBarHelper {
        fun onMenuItemClick(menuItem: MenuItem)
        fun onApplySearch(query: String)
    }

    abstract val searchBarHelper: SearchBarHelper

    abstract val stateChangeListener: SearchBar.StateChangeListener

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
                return search_bar?.getState() == SearchBar.STATE_NORMAL && recyclerView == list
            }

            override fun forceShowSearchBar(): Boolean {
                return search_bar?.getState() == SearchBar.STATE_SEARCH
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSwipeRefresh()
        initSearchBar()
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
        search_bar.apply {
            setLeftDrawable(leftDrawable)
            setHelper(helper)
            setStateChangeListener(stateChangeListener)
        }
        searchBarMover = SearchBarMover(sbMoverHelper, search_bar, list)
    }
}