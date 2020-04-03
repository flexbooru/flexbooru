package onlymash.flexbooru.data.repository.post

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import kotlinx.coroutines.*
import onlymash.flexbooru.common.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.action.ActionPost
import onlymash.flexbooru.data.api.DanbooruApi
import onlymash.flexbooru.data.database.MyDatabase
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.repository.Listing
import onlymash.flexbooru.data.repository.NetworkState
import onlymash.flexbooru.extension.NetResult
import java.util.concurrent.Executor

class PostRepositoryImpl(
    private val db: MyDatabase,
    private val booruApis: BooruApis,
    private val ioExecutor: Executor
) : PostRepository {

    private var postBoundaryCallback: PostBoundaryCallback? = null

    private fun insertResultIntoDb(posts: List<Post>) {
        try {
            db.postDao().insert(posts)
        } catch (_: SQLiteConstraintException) {}
    }

    override fun getPosts(
        scope: CoroutineScope,
        action: ActionPost
    ): Listing<Post> {
        postBoundaryCallback = PostBoundaryCallback(
            action = action,
            booruApis = booruApis,
            scope = scope,
            handleResponse = this::insertResultIntoDb,
            ioExecutor = ioExecutor
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshPosts(action, scope)
        }
        val livePagedList = db.postDao()
            .getPosts(booruUid = action.booru.uid, query = action.query)
            .toLiveData(
                config = Config(
                    pageSize = action.limit,
                    enablePlaceholders = true,
                    maxSize = 150
                ),
                boundaryCallback = postBoundaryCallback
            )
        return Listing(
            pagedList = livePagedList,
            networkState = postBoundaryCallback!!.networkState,
            retry = { postBoundaryCallback!!.helper.retryAllFailed() },
            refresh = { refreshTrigger.value = null },
            refreshState = refreshState
        )
    }

    private fun refreshPosts(
        action: ActionPost,
        scope: CoroutineScope
    ): LiveData<NetworkState> {
        postBoundaryCallback?.lastResponseSize = action.limit
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        scope.launch {
            when(val result = when(action.booru.type) {
                BOORU_TYPE_DAN -> {
                    if (action.booru.host in DanbooruApi.E621_HOSTS) {
                        refreshE621(action)
                    } else {
                        refreshDan(action)
                    }
                }
                BOORU_TYPE_DAN1 -> refreshDan1(action)
                BOORU_TYPE_MOE -> refreshMoe(action)
                BOORU_TYPE_GEL -> refreshGel(action)
                BOORU_TYPE_SHIMMIE -> refreshShimmie(action)
                else -> refreshSankaku(action)
            }) {
                is NetResult.Error -> {
                    networkState.value = NetworkState.error(result.errorMsg)
                }
                is NetResult.Success -> {
                    networkState.postValue(NetworkState.LOADED)
                    withContext(Dispatchers.IO) {
                        db.runInTransaction {
                            db.postDao().deletePosts(booruUid = action.booru.uid, query = action.query)
                            insertResultIntoDb(result.data)
                        }
                    }
                }
            }
        }
        return networkState
    }

    private suspend fun refreshDan(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.getPosts(action.getDanPostsUrl(1))
                if (response.isSuccessful) {
                    val posts = response.body()?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = index
                        )
                    } ?: listOf()
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun refreshE621(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.getPostsE621(action.getDanPostsUrl(1))
                if (response.isSuccessful) {
                    val posts = response.body()?.posts?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            index = index
                        )
                    } ?: listOf()
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun refreshDan1(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.dan1Api.getPosts(action.getDan1PostsUrl(1))
                if (response.isSuccessful) {
                    val posts = response.body()?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = index,
                            isFavored = action.isFavoredQuery()
                        )
                    } ?: listOf()
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun refreshMoe(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.moeApi.getPosts(action.getMoePostsUrl(1))
                if (response.isSuccessful) {
                    val posts = response.body()?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = index,
                            isFavored = action.isFavoredQuery()
                        )
                    } ?: listOf()
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun refreshGel(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.gelApi.getPosts(action.getGelPostsUrl(0))
                if (response.isSuccessful) {
                    val posts = response.body()?.posts?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = index
                        )
                    } ?: listOf()
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun refreshSankaku(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.sankakuApi.getPosts(action.getSankakuPostsUrl(1))
                if (response.isSuccessful) {
                    val posts = response.body()?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = index,
                            isFavored = action.isFavoredQuery()
                        )
                    } ?: listOf()
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    private suspend fun refreshShimmie(action: ActionPost): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.shimmieApi.getPosts(action.getShimmiePostsUrl(1))
                if (response.isSuccessful) {
                    val posts = response.body()?.posts?.mapIndexed { index, post ->
                        post.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = index
                        )
                    } ?: listOf()
                    NetResult.Success(posts)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }
}