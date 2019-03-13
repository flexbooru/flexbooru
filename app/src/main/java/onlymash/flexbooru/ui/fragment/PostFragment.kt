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

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.flexbox.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.refreshable_list.*
import kotlinx.android.synthetic.main.search_layout.*
import onlymash.flexbooru.*
import onlymash.flexbooru.R
import onlymash.flexbooru.database.*
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.entity.*
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.post.PostRepository
import onlymash.flexbooru.repository.tagfilter.TagFilterRepository
import onlymash.flexbooru.ui.AccountConfigActivity
import onlymash.flexbooru.ui.BrowseActivity
import onlymash.flexbooru.ui.MainActivity
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.ui.adapter.PostAdapter
import onlymash.flexbooru.ui.adapter.TagFilterAdapter
import onlymash.flexbooru.ui.viewholder.PostViewHolder
import onlymash.flexbooru.ui.viewmodel.PostViewModel
import onlymash.flexbooru.ui.viewmodel.TagFilterViewModel
import onlymash.flexbooru.util.ViewTransition
import onlymash.flexbooru.util.downloadPost
import onlymash.flexbooru.util.gridWidth
import onlymash.flexbooru.util.rotate
import onlymash.flexbooru.widget.AutoStaggeredGridLayoutManager
import onlymash.flexbooru.widget.search.SearchBar

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
        fun newInstance(keyword: String = "", booru: Booru, user: User?) =
            PostFragment().apply {
                arguments = when (booru.type) {
                    Constants.TYPE_DANBOORU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_DANBOORU)
                        putString(Constants.KEYWORD_KEY, keyword)
                        if (user != null) {
                            putInt(Constants.USER_ID_KEY, user.id)
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
                            putInt(Constants.USER_ID_KEY, user.id)
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
                        putString(Constants.KEYWORD_KEY, keyword)
                        if (user != null) {
                            putInt(Constants.USER_ID_KEY, user.id)
                            putString(Constants.USERNAME_KEY, user.name)
                            putString(Constants.AUTH_KEY, user.password_hash)
                        } else {
                            putString(Constants.USERNAME_KEY, "")
                            putString(Constants.AUTH_KEY, "")
                        }
                    }
                    Constants.TYPE_GELBOORU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_GELBOORU)
                        putString(Constants.KEYWORD_KEY, keyword)
                        if (user != null) {
                            putInt(Constants.USER_ID_KEY, user.id)
                            putString(Constants.USERNAME_KEY, user.name)
                            putString(Constants.AUTH_KEY, user.api_key)
                        } else {
                            putString(Constants.USERNAME_KEY, "")
                            putString(Constants.AUTH_KEY, "")
                        }
                    }
                    else -> throw IllegalArgumentException("unknown booru type ${booru.type}")
                }
            }
        private const val SHOW_SEARCH_LAYOUT_KEY = "show_search_layout"
    }

    private lateinit var postViewModel: PostViewModel
    private lateinit var glide: GlideRequests

    private lateinit var postAdapter: PostAdapter

    private lateinit var viewTransition: ViewTransition

    private var type = -1
    private lateinit var search: Search

    override val stateChangeListener: SearchBar.StateChangeListener
        get() = object : SearchBar.StateChangeListener {
            override fun onStateChange(newState: Int, oldState: Int, animation: Boolean) {
                searchBar.findViewById<View>(R.id.action_expand_tag_filter).rotate(135f)
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

    private lateinit var tagFilterAdapter: TagFilterAdapter
    private lateinit var tagFilterViewModel: TagFilterViewModel

    override val searchBarHelper: SearchBarHelper
        get() = object : ListFragment.SearchBarHelper {
            override fun onMenuItemClick(menuItem: MenuItem) {
                if (menuItem.itemId == R.id.action_expand_tag_filter) {
                    when {
                        !searchBar.isSearchState() && search_layout.visibility == View.GONE -> {
                            searchBar.enableSearchState(showIME = false, showSuggestion = false)
                            viewTransition.showView(1, true)
                        }
                        else -> searchBar.setText("")
                    }
                }
            }
            override fun onApplySearch(query: String) {
                if (!query.isEmpty() && query != search.keyword) SearchActivity.startActivity(requireContext(), query)
            }
        }

    private val voteRepo by lazy { ServiceLocator.instance().getVoteRepository() }

    private fun setSharedElement() {
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.sharedElement = list.findViewWithTag<View>(currentPostId)?.findViewById(R.id.preview)
        } else if (activity is SearchActivity) {
            activity.sharedElement = list.findViewWithTag<View>(currentPostId)?.findViewById(R.id.preview)
        }
    }

    private val itemListener: PostViewHolder.ItemListener = object : PostViewHolder.ItemListener {
        override fun onClickItem(post: BasePost?, view: View) {
            if (post == null) return
            currentPostId = post.getPostId()
            setSharedElement()
            BrowseActivity.startActivity(requireActivity(), view, post.getPostId(), post.keyword, Constants.PAGE_TYPE_POST)
        }

        override fun onLongClickItem(post: BasePost?) {
            if (post == null) return
            val id = post.getPostId()
            val context = requireContext()
            AlertDialog.Builder(context)
                .setTitle("Post $id")
                .setItems(context.resources.getTextArray(R.array.post_item_action)) { _, which ->
                    when (which) {
                        0 -> {
                            if (ContextCompat.checkSelfPermission(context,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED) {
                                Snackbar.make(list, context.getString(R.string.msg_download_requires_storage_permission), Snackbar.LENGTH_LONG).show()
                                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    )) {

                                } else {
                                    ActivityCompat.requestPermissions(requireActivity(),  arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ), 1)
                                }
                            } else {
                                context.downloadPost(post)
                            }
                        }
                        1 -> {
                            if (type == Constants.TYPE_GELBOORU) {
                                Snackbar.make(list, getString(R.string.msg_not_supported), Snackbar.LENGTH_SHORT).show()
                                return@setItems
                            }
                            if (search.auth_key.isEmpty()) {
                                requireActivity().startActivity(Intent(requireActivity(), AccountConfigActivity::class.java))
                            } else {
                                val vote = Vote(
                                    scheme = search.scheme,
                                    host = search.host,
                                    post_id = id,
                                    score = 3,
                                    username = search.username,
                                    auth_key = search.auth_key
                                )
                                when (post) {
                                    is PostDan -> voteRepo.addDanFav(vote, post)
                                    is PostMoe -> voteRepo.voteMoePost(vote)
                                    is PostDanOne -> voteRepo.addDanOneFav(vote, post)
                                }
                            }
                        }
                    }
                }
                .create()
                .show()
        }
    }

    private val navigationListener by lazy {
        object : MainActivity.NavigationListener {
            override fun onClickPosition(position: Int) {
                if (position == 0) list.smoothScrollToPosition(0)
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
            if (pos >= 0 && search.keyword == key) {
                currentPostId = bundle.getInt(BrowseActivity.EXT_POST_ID_KEY, currentPostId)
                list.smoothScrollToPosition(pos + 1)
                setSharedElement()
            }
        }
    }

    private val userListener = object : UserManager.Listener {
        override fun onAdd(user: User) {
            updateUserInfoAndRefresh(user)
        }
        override fun onDelete(user: User) {
            if (user.booru_uid != Settings.instance().activeBooruUid) return
            search.username = ""
            search.auth_key = ""
            when (type) {
                Constants.TYPE_DANBOORU -> {
                    postViewModel.apply {
                        show(search)
                        refreshDan()
                    }
                }
                Constants.TYPE_MOEBOORU -> {
                    postViewModel.apply {
                        show(search)
                        refreshMoe()
                    }
                }
                Constants.TYPE_DANBOORU_ONE -> {
                    postViewModel.apply {
                        show(search)
                        refreshDanOne()
                    }
                }
                Constants.TYPE_GELBOORU -> {
                    postViewModel.apply {
                        show(search)
                        refreshGel()
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
                postViewModel.apply {
                    show(search)
                    refreshDan()
                }
            }
            Constants.TYPE_MOEBOORU -> {
                search.username = user.name
                search.auth_key = user.password_hash ?: ""
                postViewModel.apply {
                    show(search)
                    refreshMoe()
                }
            }
            Constants.TYPE_DANBOORU_ONE -> {
                search.username = user.name
                search.auth_key = user.password_hash ?: ""
                postViewModel.apply {
                    show(search)
                    refreshDanOne()
                }
            }
            Constants.TYPE_GELBOORU -> {
                search.username = user.name
                search.auth_key = user.api_key ?: ""
                postViewModel.apply {
                    show(search)
                    refreshGel()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity()
        when (activity) {
            is MainActivity -> {
                arguments?.let {
                    type = it.getInt(Constants.TYPE_KEY, Constants.TYPE_UNKNOWN)
                    search = Search(
                        scheme = it.getString(Constants.SCHEME_KEY, ""),
                        host = it.getString(Constants.HOST_KEY, ""),
                        keyword = it.getString(Constants.KEYWORD_KEY, ""),
                        user_id = it.getInt(Constants.USER_ID_KEY, -1),
                        username = it.getString(Constants.USERNAME_KEY, ""),
                        auth_key = it.getString(Constants.AUTH_KEY, ""),
                        limit = Settings.instance().pageLimit)
                }
            }
            is SearchActivity -> {
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
                            Constants.TYPE_DANBOORU,
                            Constants.TYPE_GELBOORU -> user?.api_key ?: ""
                            Constants.TYPE_MOEBOORU,
                            Constants.TYPE_DANBOORU_ONE -> user?.password_hash ?: ""
                            else -> ""
                        },
                        limit = Settings.instance().pageLimit
                    )
                } else {
                    activity.finish()
                }
            }
            else -> activity.finish()
        }
        activity.registerReceiver(broadcastReceiver, IntentFilter(BrowseActivity.ACTION))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Settings.instance().safeMode) {
            search.keyword = "rating:safe ${search.keyword}"
        }
        init()
        UserManager.listeners.add(userListener)
    }

    private fun init() {
        viewTransition = ViewTransition(swipe_refresh, search_layout)
        if (requireActivity() !is MainActivity) {
            leftDrawable.progress = 1f
            val keyword = search.keyword
            searchBar.setTitle(keyword)
            searchBar.setText(keyword)
        }
        searchBar.setEditTextHint(getString(R.string.search_bar_hint_search_posts))
        searchBar.setMenu(menuId = R.menu.post, menuInflater = requireActivity().menuInflater)
        postViewModel = getPostViewModel(ServiceLocator.instance().getPostRepository())
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
            pageType = Constants.PAGE_TYPE_POST,
            retryCallback = {
                when (type) {
                    Constants.TYPE_DANBOORU -> postViewModel.retryDan()
                    Constants.TYPE_MOEBOORU -> postViewModel.retryMoe()
                    Constants.TYPE_DANBOORU_ONE -> postViewModel.retryDanOne()
                    Constants.TYPE_GELBOORU -> postViewModel.retryGel()
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
        val orders = resources.getStringArray(R.array.filter_order)
        val ratings = resources.getStringArray(R.array.filter_rating)
        tagFilterAdapter = TagFilterAdapter(
            orders = orders,
            ratings = ratings,
            booruType = type) {
            val text = searchBar.getEditTextText()
            if(text.isNotEmpty()) {
                TagFilterManager.createTagFilter(TagFilter(booru_uid = Settings.instance().activeBooruUid, name = text))
            }
        }
        tags_filter_list.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexWrap = FlexWrap.WRAP
                flexDirection = FlexDirection.ROW
                alignItems = AlignItems.STRETCH
            }
            adapter = tagFilterAdapter
        }
        tagFilterViewModel = getTagFilterViewModel(ServiceLocator.instance().getTagFilterDataSource())
        tagFilterViewModel.tagsFilter.observe(this, Observer {
            tagFilterAdapter.updateData(it)
        })
        tagFilterViewModel.loadTags(Settings.instance().activeBooruUid)
        action_search.setOnClickListener {
            val tagString = tagFilterAdapter.getSelectedTagsString()
            if (!tagString.isBlank()) {
                searchBar.disableSearchState()
                SuggestionManager.createSuggestion(Suggestion(
                    booru_uid = Settings.instance().activeBooruUid,
                    keyword = tagString))
                SearchActivity.startActivity(requireContext(), tagString)
            }
        }
        when (type) {
            Constants.TYPE_DANBOORU -> {
                postViewModel.postsDan.observe(this, Observer<PagedList<PostDan>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<BasePost>)
                })
                postViewModel.networkStateDan.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                postViewModel.postsMoe.observe(this, Observer<PagedList<PostMoe>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<BasePost>)
                })
                postViewModel.networkStateMoe.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshMoe()
            }
            Constants.TYPE_DANBOORU_ONE -> {
                postViewModel.postsDanOne.observe(this, Observer<PagedList<PostDanOne>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<BasePost>)
                })
                postViewModel.networkStateDanOne.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDanOne()
            }
            Constants.TYPE_GELBOORU -> {
                postViewModel.postsGel.observe(this, Observer<PagedList<PostGel>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<BasePost>)
                })
                postViewModel.networkStateGel.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshGel()
            }
        }
        postViewModel.show(search)
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.addNavigationListener(navigationListener)
        } else {
            searchBar.setTitleOnLongClickCallback {
                MuzeiManager.createMuzei(Muzei(booru_uid = Settings.instance().activeBooruUid, keyword = search.keyword))
                Snackbar.make(searchBar, getString(R.string.post_add_search_to_muzei, search.keyword), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun initSwipeToRefreshDanOne() {
        postViewModel.refreshStateDanOne.observe(this, Observer<NetworkState> {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener { postViewModel.refreshDanOne() }
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

    private fun initSwipeToRefreshGel() {
        postViewModel.refreshStateGel.observe(this, Observer<NetworkState> {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener { postViewModel.refreshGel() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getPostViewModel(repo: PostRepository): PostViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PostViewModel(repo) as T
            }
        })[PostViewModel::class.java]
    }

    @Suppress("UNCHECKED_CAST")
    private fun getTagFilterViewModel(repo: TagFilterRepository): TagFilterViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return TagFilterViewModel(repo) as T
            }
        })[TagFilterViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.removeNavigationListener(navigationListener)
        }
        UserManager.listeners.remove(userListener)
        requireActivity().unregisterReceiver(broadcastReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SHOW_SEARCH_LAYOUT_KEY, search_layout.visibility == View.VISIBLE)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getBoolean(SHOW_SEARCH_LAYOUT_KEY, false)?.let {
            if (it) viewTransition.showView(1, true)
        }
    }
}