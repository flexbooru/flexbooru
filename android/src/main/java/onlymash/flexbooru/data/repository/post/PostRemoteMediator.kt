package onlymash.flexbooru.data.repository.post

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL_LEGACY
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.app.Values.PAGE_TYPE_POPULAR
import onlymash.flexbooru.data.action.ActionPost
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.api.DanbooruApi
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.database.MyDatabase
import onlymash.flexbooru.data.model.common.Next
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.moebooru.PostMoe
import onlymash.flexbooru.data.model.sankaku.RefreshTokenBody
import retrofit2.HttpException

class PostRemoteMediator(
    private val action: ActionPost,
    private val db: MyDatabase,
    private val booruApis: BooruApis
) : RemoteMediator<Int,  Post>() {

    private val isFavored = action.isFavoredQuery()
    private var lastResponseSize = action.limit
    private val postDao = db.postDao()
    private val nextIndex: Int
        get() = db.postDao().getNextIndex(action.booru.uid, action.query)
    private fun nextPage(nextIndex: Int): Int {
        return when (action.booru.type) {
            in arrayOf(BOORU_TYPE_GEL, BOORU_TYPE_GEL_LEGACY) -> nextIndex/action.limit
            else -> nextIndex/action.limit + 1
        }
    }
    private val nextSankakuKey: String?
        get() = db.nextDao().getNext(action.booru.uid, action.query)?.next

    override suspend fun initialize(): InitializeAction {
        return if (Settings.autoRefresh)
            InitializeAction.LAUNCH_INITIAL_REFRESH
        else
            InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Post>): MediatorResult {
        val posts = when (loadType) {
            LoadType.REFRESH -> {
                try {
                    fetchPosts(0, null)
                } catch (e: Exception) {
                    return MediatorResult.Error(e)
                }
            }
            LoadType.PREPEND -> {
                return MediatorResult.Success(endOfPaginationReached = true)
            }
            LoadType.APPEND -> {
                val index = nextIndex
                if (!hasMore() && index != 0) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                try {
                    fetchPosts(index, nextSankakuKey)
                } catch (e: Exception) {
                    return MediatorResult.Error(e)
                }
            }
        }
        try {
            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    postDao.deletePosts(action.booru.uid, action.query)
                }
                postDao.insert(posts)
            }
        } catch (_: Exception) { }
        return MediatorResult.Success(endOfPaginationReached = !hasMore())
    }

    private fun hasMore(): Boolean {
        return when {
            action.booru.type == BOORU_TYPE_SANKAKU -> !nextSankakuKey.isNullOrEmpty()
            action.pageType == PAGE_TYPE_POPULAR -> false
            else -> lastResponseSize == action.limit
        }
    }

    private suspend fun fetchPosts(nextIndex: Int, next: String?): List<Post> {
        return when (action.booru.type) {
            BOORU_TYPE_DAN -> {
                if (action.booru.host in DanbooruApi.E621_HOSTS) {
                    fetchPostsDanE621(nextPage(nextIndex), nextIndex)
                } else {
                    fetchPostsDan(nextPage(nextIndex), nextIndex)
                }
            }
            BOORU_TYPE_DAN1 -> fetchPostsDan1(nextPage(nextIndex), nextIndex)
            BOORU_TYPE_MOE -> fetchPostsMoe(nextPage(nextIndex), nextIndex)
            BOORU_TYPE_SANKAKU -> fetchPostsSankaku(next, nextIndex)
            BOORU_TYPE_GEL -> fetchPostsGel(nextPage(nextIndex), nextIndex)
            BOORU_TYPE_GEL_LEGACY -> fetchPostsGelLegacy(nextPage(nextIndex), nextIndex)
            else -> fetchPostsShimmie(nextPage(nextIndex), nextIndex)
        }
    }

    private suspend fun fetchPostsDan(page: Int, indexInNext: Int): List<Post> {
        val response = booruApis.danApi.getPosts(action.getDanPostsUrl(page))
        val raw = response.body() ?: listOf()
        lastResponseSize = raw.size
        val posts = raw.filter { it.id != -1 }
        return posts.mapIndexed { index, postDan ->
            postDan.toPost(
                booruUid = action.booru.uid,
                query = action.query,
                scheme = action.booru.scheme,
                host = action.booru.host,
                index = indexInNext + index,
                isFavored = isFavored
            )
        }
    }

    private suspend fun fetchPostsDanE621(page: Int, indexInNext: Int): List<Post> {
        val response = booruApis.danApi.getPostsE621(action.getDanPostsUrl(page))
        val raw = response.body()?.posts ?: listOf()
        lastResponseSize = raw.size
        val posts = raw.filter { it.id != -1 }
        return posts.mapIndexed { index, post ->
            post.toPost(
                booruUid = action.booru.uid,
                query = action.query,
                index = indexInNext + index
            )
        }
    }

    private suspend fun fetchPostsDan1(page: Int, indexInNext: Int): List<Post> {
        val response = booruApis.dan1Api.getPosts(action.getDan1PostsUrl(page))
        val raw = response.body()?.toMutableList()
        lastResponseSize = raw?.size ?: 0
        return raw?.mapIndexed { index, post ->
            post.toPost(
                booruUid = action.booru.uid,
                query = action.query,
                scheme = action.booru.scheme,
                host = action.booru.host,
                index = indexInNext + index,
                isFavored = isFavored
            )
        } ?: listOf()

    }

    private suspend fun fetchPostsMoe(page: Int, indexInNext: Int): List<Post> {
        if (action.pageType == Values.PAGE_TYPE_POSTS) {
            val votes: Map<String, Int>
            val tagTypes: Map<String, String>
            val posts: List<PostMoe>
            if (action.booru.host == "lolibooru.moe") {
                val response = booruApis.moeApi.getPostsLoli(action.getPostsMoeUrl(page)).body()
                if (response == null) {
                    lastResponseSize = 0
                    return emptyList()
                }
                votes = mapOf()
                tagTypes = response.tags
                posts = response.posts
            } else {
                val response = booruApis.moeApi.getPosts(action.getPostsMoeUrl(page)).body()
                if (response == null) {
                    lastResponseSize = 0
                    return emptyList()
                }
                votes = response.votes
                tagTypes = response.tags
                posts = response.posts
            }
            lastResponseSize = posts.size
            return posts.mapIndexed { index, postMoe ->
                postMoe.toPost(
                    booruUid = action.booru.uid,
                    query = action.query,
                    scheme = action.booru.scheme,
                    host = action.booru.host,
                    index = indexInNext + index,
                    tagTypes = tagTypes,
                    votes = votes
                )
            }
        } else {
            val posts = booruApis.moeApi.getPostsPopular(action.getPopularMoeUrl()).body()
            if (posts == null) {
                lastResponseSize = 0
                return emptyList()
            }
            lastResponseSize = posts.size
            return posts.mapIndexed { index, postMoe ->
                postMoe.toPost(
                    booruUid = action.booru.uid,
                    query = action.query,
                    scheme = action.booru.scheme,
                    host = action.booru.host,
                    index = indexInNext + index,
                    tagTypes = mapOf(),
                    votes = mapOf()
                )
            }
        }
    }

    private suspend fun fetchPostsGel(page: Int, indexInNext: Int): List<Post> {
        val response = booruApis.gelApi.getPosts(action.getGelPostsUrl(page))
        val raw = response.body()?.posts?.toMutableList()
        lastResponseSize = raw?.size ?: 0
        return raw?.mapIndexed { index, post ->
            post.toPost(
                booruUid = action.booru.uid,
                query = action.query,
                scheme = action.booru.scheme,
                host = action.booru.host,
                index = indexInNext + index,
                isFavored = isFavored
            )
        } ?: listOf()
    }

    private suspend fun fetchPostsGelLegacy(page: Int, indexInNext: Int): List<Post> {
        val response = booruApis.gelApi.getPostsLegacy(action.getGelPostsUrl(page))
        val raw = response.body()?.posts?.toMutableList()
        lastResponseSize = raw?.size ?: 0
        return raw?.mapIndexed { index, post ->
            post.toPost(
                booruUid = action.booru.uid,
                query = action.query,
                scheme = action.booru.scheme,
                host = action.booru.host,
                index = indexInNext + index
            )
        } ?: listOf()
    }

    private suspend fun fetchPostsShimmie(page: Int, indexInNext: Int): List<Post> {
        val response = booruApis.shimmieApi.getPosts(action.getShimmiePostsUrl(page))
        val data = response.body()
        val posts = when {
            data?.post != null -> {
                data.post.mapIndexed { index, post ->
                    post.toPost(
                        booruUid = action.booru.uid,
                        query = action.query,
                        scheme = action.booru.scheme,
                        host = action.booru.host,
                        index = indexInNext + index
                    )
                }
            }
            data?.tag != null -> {
                data.tag.mapIndexed { index, post ->
                    post.toPost(
                        booruUid = action.booru.uid,
                        query = action.query,
                        scheme = action.booru.scheme,
                        host = action.booru.host,
                        index = indexInNext + index
                    )
                }
            }
            else -> listOf()
        }
        lastResponseSize = posts.size
        return posts
    }

    private suspend fun fetchPostsSankaku(next: String?, indexInNext: Int): List<Post> {
        val url = if (next.isNullOrEmpty()) action.getSankakuPostsUrl() else action.getSankakuPostsUrlNext(next)
        val auth = action.booru.user?.getAuth
        val response = if (auth.isNullOrBlank())
            booruApis.sankakuApi.getPosts(url)
        else
            booruApis.sankakuApi.getPostsAuth(url, auth)
        if (response.code() == 401) {
            refreshSankakuToken()
            throw(HttpException(response))
        }
        val data = response.body()
        val raw = data?.posts
        lastResponseSize = raw?.size ?: 0
        db.nextDao().insert(Next(booruUid = action.booru.uid, query = action.query, next = data?.meta?.next))
        return raw?.mapIndexed { index, post ->
            post.toPost(
                booruUid = action.booru.uid,
                query = action.query,
                scheme = action.booru.scheme,
                host = action.booru.host,
                index = indexInNext + index
            )
        } ?: listOf()
    }

    private suspend fun refreshSankakuToken() {
        val refreshToken = action.booru.user?.refreshToken
        if (refreshToken != null) {
            try {
                val user = booruApis.sankakuApi
                    .refreshToken(action.booru.getSankakuTokenUrl(), RefreshTokenBody(refreshToken))
                    .body()?.toUser()
                if (user != null) {
                    action.booru.user = user
                    BooruManager.updateBooru(action.booru)
                }
            } catch (_: Exception) {

            }
        }
    }
}
