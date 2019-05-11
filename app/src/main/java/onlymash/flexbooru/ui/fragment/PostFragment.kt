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
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.refreshable_list.*
import kotlinx.android.synthetic.main.search_layout.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.animation.RippleAnimation
import onlymash.flexbooru.database.*
import onlymash.flexbooru.database.dao.TagBlacklistDao
import onlymash.flexbooru.entity.*
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.entity.tag.SearchTag
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.favorite.VoteRepositoryIml
import onlymash.flexbooru.repository.post.PostRepositoryIml
import onlymash.flexbooru.repository.post.PostRepository
import onlymash.flexbooru.repository.tagfilter.TagFilterRepositoryIml
import onlymash.flexbooru.repository.tagfilter.TagFilterRepository
import onlymash.flexbooru.ui.AccountConfigActivity
import onlymash.flexbooru.ui.BrowseActivity
import onlymash.flexbooru.ui.MainActivity
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.ui.adapter.PostAdapter
import onlymash.flexbooru.ui.adapter.TagFilterAdapter
import onlymash.flexbooru.ui.viewholder.PostViewHolder
import onlymash.flexbooru.ui.viewmodel.PostViewModel
import onlymash.flexbooru.ui.viewmodel.TagBlacklistViewModel
import onlymash.flexbooru.ui.viewmodel.TagFilterViewModel
import onlymash.flexbooru.util.*
import onlymash.flexbooru.widget.AutoStaggeredGridLayoutManager
import onlymash.flexbooru.widget.search.SearchBar
import org.kodein.di.generic.instance

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
                    Constants.TYPE_SANKAKU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_SANKAKU)
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
                    else -> throw IllegalArgumentException("unknown booru type ${booru.type}")
                }
            }
        private const val SHOW_SEARCH_LAYOUT_KEY = "show_search_layout"
    }

    private val db: FlexbooruDatabase by instance()
    private val tagFilterRepositoryIml: TagFilterRepositoryIml by instance()
    private val tagBlacklistDao: TagBlacklistDao by instance()

    private lateinit var tagBlacklistViewModel: TagBlacklistViewModel
    private lateinit var postViewModel: PostViewModel
    private lateinit var glide: GlideRequests

    private lateinit var postAdapter: PostAdapter

    private lateinit var viewTransition: ViewTransition

    private var type = -1
    private lateinit var search: Search
    private lateinit var searchTag: SearchTag
    private lateinit var expandButton: View
    private var tagBlacklists: MutableList<TagBlacklist> = mutableListOf()

    override val stateChangeListener: SearchBar.StateChangeListener
        get() = object : SearchBar.StateChangeListener {
            override fun onStateChange(newState: Int, oldState: Int, animation: Boolean) {
                expandButton.rotate(135f)
                if (requireActivity() is MainActivity) toggleArrowLeftDrawable()
                when (newState) {
                    SearchBar.STATE_NORMAL -> {
                        if (search_layout.visibility == View.VISIBLE) {
                            RippleAnimation.create(searchBar.getLeftButton()).setDuration(300).start()
                            viewTransition.showView(0, false)
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
        get() = object : SearchBarHelper {
            override fun onMenuItemClick(menuItem: MenuItem) {
                if (menuItem.itemId == R.id.action_expand_tag_filter) {
                    when {
                        !searchBar.isSearchState() && search_layout.visibility != View.VISIBLE -> {
                            searchBar.enableSearchState(showIME = false, showSuggestion = false)
                            RippleAnimation.create(expandButton).setDuration(300).start()
                            viewTransition.showView(1, false)
                        }
                        else -> searchBar.setText("")
                    }
                }
            }
            override fun onApplySearch(query: String) {
                if (query.isNotEmpty() && query != search.keyword) SearchActivity.startActivity(requireContext(), query)
            }
        }

    private val voteRepo by lazy {
        VoteRepositoryIml(
            danbooruApi = danApi,
            danbooruOneApi = danOneApi,
            moebooruApi = moeApi,
            sankakuApi = sankakuApi,
            db = db,
            ioExecutor = ioExecutor
        )
    }

    private fun setSharedElement() {
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.sharedElement = list.findViewWithTag<View>(currentPostId)?.findViewById(R.id.preview)
        } else if (activity is SearchActivity) {
            activity.sharedElement = list.findViewWithTag<View>(currentPostId)?.findViewById(R.id.preview)
        }
    }

    private val itemListener: PostViewHolder.ItemListener = object : PostViewHolder.ItemListener {
        override fun onClickItem(post: PostBase?, view: View) {
            if (post == null) return
            currentPostId = post.getPostId()
            setSharedElement()
            BrowseActivity.startActivity(requireActivity(), view, post.getPostId(), post.keyword, Constants.PAGE_TYPE_POST)
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

    private val navigationListener by lazy {
        object : MainActivity.NavigationListener {
            override fun onClickPosition(position: Int) {
                showSearchBar()
                if (position == 0) list.scrollToPosition(0)
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
                list.scrollToPosition(pos + 1)
                setSharedElement()
            }
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
                    postViewModel.apply {
                        show(search, tagBlacklists)
                        refreshDan()
                    }
                }
                Constants.TYPE_MOEBOORU -> {
                    postViewModel.apply {
                        show(search, tagBlacklists)
                        refreshMoe()
                    }
                }
                Constants.TYPE_DANBOORU_ONE -> {
                    postViewModel.apply {
                        show(search, tagBlacklists)
                        refreshDanOne()
                    }
                }
                Constants.TYPE_GELBOORU -> {
                    postViewModel.apply {
                        show(search, tagBlacklists)
                        refreshGel()
                    }
                }
                Constants.TYPE_SANKAKU -> {
                    postViewModel.apply {
                        show(search, tagBlacklists)
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
                postViewModel.apply {
                    show(search, tagBlacklists)
                    refreshDan()
                }
            }
            Constants.TYPE_MOEBOORU -> {
                search.username = user.name
                search.auth_key = user.password_hash ?: ""
                postViewModel.apply {
                    show(search, tagBlacklists)
                    refreshMoe()
                }
            }
            Constants.TYPE_DANBOORU_ONE -> {
                search.username = user.name
                search.auth_key = user.password_hash ?: ""
                postViewModel.apply {
                    show(search, tagBlacklists)
                    refreshDanOne()
                }
            }
            Constants.TYPE_GELBOORU -> {
                search.username = user.name
                search.auth_key = user.api_key ?: ""
                postViewModel.apply {
                    show(search, tagBlacklists)
                    refreshGel()
                }
            }
            Constants.TYPE_SANKAKU -> {
                search.username = user.name
                search.auth_key = user.password_hash ?: ""
                postViewModel.apply {
                    show(search, tagBlacklists)
                    refreshSankaku()
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
                        limit = Settings.pageLimit)
                }
            }
            is SearchActivity -> {
                val uid = Settings.activeBooruUid
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
                            Constants.TYPE_DANBOORU_ONE,
                            Constants.TYPE_SANKAKU -> user?.password_hash ?: ""
                            else -> ""
                        },
                        limit = Settings.pageLimit
                    )
                } else {
                    activity.finish()
                }
            }
            else -> activity.finish()
        }
        activity.registerReceiver(broadcastReceiver, IntentFilter(BrowseActivity.ACTION))
        searchTag = SearchTag(
            scheme = search.scheme,
            host = search.host,
            name = "",
            order = "name",
            limit = 6,
            type = ""
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Settings.safeMode) {
            search.keyword = "rating:safe ${search.keyword}"
        }
        init()
        UserManager.listeners.add(userListener)
        searchBar.setType(type)
        searchBar.setSearchTag(searchTag)
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
        expandButton = searchBar.findViewById<View>(R.id.action_expand_tag_filter)
        postViewModel = getPostViewModel(
            PostRepositoryIml(
                db = db,
                danbooruApi = danApi,
                danbooruOneApi = danOneApi,
                moebooruApi = moeApi,
                gelbooruApi = gelApi,
                sankakuApi = sankakuApi,
                ioExecutor = ioExecutor
            )
        )
        glide = GlideApp.with(this)
        val staggeredGridLayoutManager = AutoStaggeredGridLayoutManager(
            columnSize = resources.gridWidth(),
            orientation = StaggeredGridLayoutManager.VERTICAL).apply {
                setStrategy(AutoStaggeredGridLayoutManager.STRATEGY_SUITABLE_SIZE)
            }
        postAdapter = PostAdapter(
            glide = glide,
            listener = itemListener,
            showInfoBar = Settings.showInfoBar,
            pageType = Constants.PAGE_TYPE_POST,
            retryCallback = {
                when (type) {
                    Constants.TYPE_DANBOORU -> postViewModel.retryDan()
                    Constants.TYPE_MOEBOORU -> postViewModel.retryMoe()
                    Constants.TYPE_DANBOORU_ONE -> postViewModel.retryDanOne()
                    Constants.TYPE_GELBOORU -> postViewModel.retryGel()
                    Constants.TYPE_SANKAKU -> postViewModel.retrySankaku()
                }
            })
        list.apply {
            setHasFixedSize(true)
            layoutManager = staggeredGridLayoutManager
            adapter = postAdapter
        }
        val orders =
            if (type == Constants.TYPE_SANKAKU)
                resources.getStringArray(R.array.filter_order_sankaku)
            else
                resources.getStringArray(R.array.filter_order)
        val ratings = resources.getStringArray(R.array.filter_rating)
        tagFilterAdapter = TagFilterAdapter(
            orders = orders,
            ratings = ratings,
            booruType = type) {
            val text = searchBar.getEditTextText()
            if(text.isNotEmpty()) {
                TagFilterManager.createTagFilter(TagFilter(booru_uid = Settings.activeBooruUid, name = text))
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
        tagFilterViewModel = getTagFilterViewModel(tagFilterRepositoryIml)
        tagFilterViewModel.tagsFilter.observe(this, Observer {
            tagFilterAdapter.updateData(it)
        })
        tagFilterViewModel.loadTags(Settings.activeBooruUid)
        action_search.setOnClickListener {
            val tagString = tagFilterAdapter.getSelectedTagsString()
            if (!tagString.isBlank()) {
//                searchBar.disableSearchState()
                SuggestionManager.createSuggestion(Suggestion(
                    booru_uid = Settings.activeBooruUid,
                    keyword = tagString))
                SearchActivity.startActivity(requireContext(), tagString)
            }
        }
        when (type) {
            Constants.TYPE_DANBOORU -> {
                postViewModel.postsDan.observe(this, Observer<PagedList<PostDan>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                postViewModel.networkStateDan.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                postViewModel.postsMoe.observe(this, Observer<PagedList<PostMoe>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                postViewModel.networkStateMoe.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshMoe()
            }
            Constants.TYPE_DANBOORU_ONE -> {
                postViewModel.postsDanOne.observe(this, Observer<PagedList<PostDanOne>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                postViewModel.networkStateDanOne.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDanOne()
            }
            Constants.TYPE_GELBOORU -> {
                postViewModel.postsGel.observe(this, Observer<PagedList<PostGel>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                postViewModel.networkStateGel.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshGel()
            }
            Constants.TYPE_SANKAKU -> {
                postViewModel.postsSankaku.observe(this, Observer<PagedList<PostSankaku>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                postViewModel.networkStateSankaku.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshSankaku()
            }
        }
        tagBlacklistViewModel = getViewModel(object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return TagBlacklistViewModel(tagBlacklistDao) as T
            }
        })
        tagBlacklistViewModel.tagOutcome.observe(this, Observer {
            postViewModel.show(search, it)
        })
        tagBlacklistViewModel.loadTags(Settings.activeBooruUid)
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.addNavigationListener(navigationListener)
        } else {
            searchBar.setTitleOnLongClickCallback {
                MuzeiManager.createMuzei(Muzei(booru_uid = Settings.activeBooruUid, keyword = search.keyword))
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

    private fun initSwipeToRefreshSankaku() {
        postViewModel.refreshStateSankaku.observe(this, Observer<NetworkState> {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener { postViewModel.refreshSankaku() }
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
            activity.sharedElement = null
        }
        activity.unregisterReceiver(broadcastReceiver)
        UserManager.listeners.remove(userListener)
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