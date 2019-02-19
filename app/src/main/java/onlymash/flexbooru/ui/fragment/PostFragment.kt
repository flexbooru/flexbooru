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

import android.content.*
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.flexbox.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet_tag.*
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.refreshable_list.*
import kotlinx.android.synthetic.main.search_layout.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.entity.*
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.post.PostRepository
import onlymash.flexbooru.ui.BrowseActivity
import onlymash.flexbooru.ui.MainActivity
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.ui.adapter.PostAdapter
import onlymash.flexbooru.ui.adapter.TagFilterAdapter
import onlymash.flexbooru.ui.viewholder.PostViewHolder
import onlymash.flexbooru.ui.viewmodel.PostViewModel
import onlymash.flexbooru.util.ViewTransition
import onlymash.flexbooru.util.rotate
import onlymash.flexbooru.widget.AutoStaggeredGridLayoutManager
import onlymash.flexbooru.widget.SearchBar

class PostFragment : ListFragment() {

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param booru active booru
         * @param keyword keyword of search
         * @param user account info
         * @return A new instance of fragment PostFragment.
         */
        @JvmStatic
        fun newInstance(keyword: String, booru: Booru, user: User?) =
            PostFragment().apply {
                arguments = when (booru.type) {
                    Constants.TYPE_DANBOORU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_DANBOORU)
                        putString(Constants.KEYWORD_KEY, keyword)
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
                        putString(Constants.KEYWORD_KEY, keyword)
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
        private const val TAG = "PostFragment"
    }

    private lateinit var postViewModel: PostViewModel
    private lateinit var glide: GlideRequests

    private lateinit var postAdapter: PostAdapter

    private lateinit var viewTransition: ViewTransition

    private var type = -1
    private var search: Search? = null

    override val stateChangeListener: SearchBar.StateChangeListener
        get() = object : SearchBar.StateChangeListener {
            override fun onStateChange(newState: Int, oldState: Int, animation: Boolean) {
                search_bar.findViewById<View>(R.id.action_expand_tag_filter).rotate(135f)
                if (requireActivity() is MainActivity) toggleArrowLeftDrawable()
                when (newState) {
                    SearchBar.STATE_NORMAL -> {
                        if (search_layout.visibility != View.GONE) {
                            viewTransition.showView(0, true)
                        }
                    }
                    SearchBar.STATE_SEARCH -> {

                    }
                }
            }
        }

    private val tagFilterAdapter: TagFilterAdapter by lazy { TagFilterAdapter() }
    private val flexboxLayoutManager: FlexboxLayoutManager by lazy {
        FlexboxLayoutManager(requireContext()).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            alignItems = AlignItems.STRETCH
        }
    }

    override val searchBarHelper: SearchBarHelper
        get() = object : ListFragment.SearchBarHelper {
            override fun onMenuItemClick(menuItem: MenuItem) {
                if (menuItem.itemId == R.id.action_expand_tag_filter) {
                    when {
                        !search_bar.isSearchState() && search_layout.visibility == View.GONE -> {
                            search_bar.enableSearchState(showIME = false)
                            viewTransition.showView(1, true)
                            tags_filter_list.apply {
                                layoutManager = flexboxLayoutManager
                                if (adapter == null) {
                                    adapter = tagFilterAdapter
                                }
                            }
                        }
                        else -> search_bar.setText("")
                    }
                }
            }
            override fun onApplySearch(query: String) {
                if (query != search!!.keyword) SearchActivity.startActivity(requireContext(), query)
            }
        }

    private val itemListener: PostViewHolder.ItemListener = object : PostViewHolder.ItemListener {
        override fun onClickDanItem(post: PostDan, view: View) {
            currentPostId = post.id
            val intent = Intent(requireContext(), BrowseActivity::class.java)
                .apply {
                    putExtra(Constants.ID_KEY, post.id)
                    putExtra(Constants.HOST_KEY, post.host)
                    putExtra(Constants.TYPE_KEY, Constants.TYPE_DANBOORU)
                    putExtra(Constants.KEYWORD_KEY, post.keyword)
                }
            val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(requireActivity(), view, String.format(getString(R.string.post_transition_name), post.id))
            startActivity(intent, options.toBundle())
        }
        override fun onClickMoeItem(post: PostMoe, view: View) {
            currentPostId = post.id
            val intent = Intent(requireContext(), BrowseActivity::class.java)
                .apply {
                    putExtra(Constants.ID_KEY, post.id)
                    putExtra(Constants.HOST_KEY, post.host)
                    putExtra(Constants.TYPE_KEY, Constants.TYPE_MOEBOORU)
                    putExtra(Constants.KEYWORD_KEY, post.keyword)
                }
            val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(requireActivity(), view, String.format(getString(R.string.post_transition_name), post.id))
            startActivity(intent, options.toBundle())
        }
    }

    private var navigationListener: MainActivity.NavigationListener? = null

    private var currentPostId = 0

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            if (search == null) return
            val bundle= intent.extras ?: return
            val pos = bundle.getInt(BrowseActivity.EXT_POST_POSITION_KEY, -1)
            val key = bundle.getString(BrowseActivity.EXT_POST_KEYWORD_KEY)
            if (pos >= 0 && search!!.keyword == key) {
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
            search!!.username = ""
            search!!.auth_key = ""
            when (type) {
                Constants.TYPE_DANBOORU -> {
                    postViewModel.apply {
                        show(search!!)
                        refreshDan()
                    }
                }
                Constants.TYPE_MOEBOORU -> {
                    postViewModel.apply {
                        show(search!!)
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
                search!!.username = user.name
                search!!.auth_key = user.api_key ?: ""
                postViewModel.apply {
                    show(search!!)
                    refreshDan()
                }
            }
            Constants.TYPE_MOEBOORU -> {
                search!!.username = user.name
                search!!.auth_key = user.password_hash ?: ""
                postViewModel.apply {
                    show(search!!)
                    refreshMoe()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity()
        if (activity is MainActivity) {
            arguments?.let {
                type = it.getInt(Constants.TYPE_KEY, Constants.TYPE_UNKNOWN)
                search = Search(
                    scheme = it.getString(Constants.SCHEME_KEY, ""),
                    host = it.getString(Constants.HOST_KEY, ""),
                    keyword = it.getString(Constants.KEYWORD_KEY, ""),
                    username = it.getString(Constants.USERNAME_KEY, ""),
                    auth_key = it.getString(Constants.AUTH_KEY, ""),
                    limit = Settings.instance().pageSize)
            }
            activity.setPostExitSharedElementCallback(sharedElementCallback)
        } else if (activity is SearchActivity){
            val uid = Settings.instance().activeBooruUid
            val booru = BooruManager.getBooruByUid(uid)
            val user = UserManager.getUserByBooruUid(uid)
            if (booru != null) {
                type = booru.type
                search = Search(
                    scheme = booru.scheme,
                    host = booru.host,
                    keyword = activity.keyword,
                    username = user?.name ?: "",
                    auth_key = when (type) {
                        Constants.TYPE_DANBOORU -> user?.api_key ?: ""
                        Constants.TYPE_MOEBOORU -> user?.password_hash ?: ""
                        else -> ""
                    },
                    limit = Settings.instance().pageSize
                )
            } else {
                activity.finish()
            }
            activity.setExitSharedElementCallback(sharedElementCallback)
        }
        activity.registerReceiver(broadcastReceiver, IntentFilter(BrowseActivity.ACTION))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (search == null) return
        if (Settings.instance().safeMode) {
            search!!.keyword = "rating:safe ${search!!.keyword}"
        }
        init()
        UserManager.listeners.add(userListener)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setExitSharedElementCallback(sharedElementCallback)
    }

    private fun init() {
        viewTransition = ViewTransition(swipe_refresh, search_layout)
        if (requireActivity() !is MainActivity) {
            leftDrawable.progress = 1f
            val keyword = search!!.keyword
            search_bar.setTitle(keyword)
            search_bar.setText(keyword)
        }
        search_bar.setEditTextHint(getString(R.string.search_bar_hint_search_posts))
        search_bar.setMenu(menuId = R.menu.post, menuInflater = requireActivity().menuInflater)
        postViewModel = getPostViewModel(ServiceLocator.instance().getPostRepository())
        glide = GlideApp.with(this)
        val staggeredGridLayoutManager = AutoStaggeredGridLayoutManager(
            columnSize = resources.getDimensionPixelSize(R.dimen.post_item_width),
            orientation = StaggeredGridLayoutManager.VERTICAL).apply {
                setStrategy(AutoStaggeredGridLayoutManager.STRATEGY_SUITABLE_SIZE)
            }
        postAdapter = PostAdapter(
            glide = glide,
            listener = itemListener,
            retryCallback = {
                when (type) {
                    Constants.TYPE_DANBOORU -> postViewModel.retryDan()
                    Constants.TYPE_MOEBOORU -> postViewModel.retryMoe()
                }
            })
        list.apply {
            setHasFixedSize(true)
            layoutManager = staggeredGridLayoutManager
            adapter = postAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> glide.resumeRequests()
                        else -> glide.pauseRequests()
                    }
                }
            })
        }
        when (type) {
            Constants.TYPE_DANBOORU -> {
                postViewModel.postsDan.observe(this, Observer<PagedList<PostDan>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<Any>)
                })
                postViewModel.networkStateDan.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                postViewModel.postsMoe.observe(this, Observer<PagedList<PostMoe>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<Any>)
                })
                postViewModel.networkStateMoe.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshMoe()
            }
        }
        postViewModel.show(search!!)
        val activity = requireActivity()
        if (activity is MainActivity) {
            navigationListener = object : MainActivity.NavigationListener {
                override fun onClickPosition(position: Int) {
                    if (position == 0) list.smoothScrollToPosition(0)
                }
            }
            activity.addNavigationListener(navigationListener!!)
        }
    }

    private fun initSwipeToRefreshDan() {
        postViewModel.refreshStateDan.observe(this, Observer<NetworkState> {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener { postViewModel.refreshDan() }
    }

    private fun initSwipeToRefreshMoe() {
        postViewModel.refreshStateMoe.observe(this, Observer<NetworkState> {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener { postViewModel.refreshMoe() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getPostViewModel(repo: PostRepository): PostViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PostViewModel(repo) as T
            }
        })[PostViewModel::class.java]
    }

    override fun onDestroy() {
        UserManager.listeners.remove(userListener)
        requireActivity().unregisterReceiver(broadcastReceiver)
        if (navigationListener != null)
            (requireActivity() as MainActivity).removeNavigationListener(navigationListener!!)
        super.onDestroy()
    }
}