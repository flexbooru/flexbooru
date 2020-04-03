package onlymash.flexbooru.data.repository.post

import androidx.paging.PagedList
import androidx.paging.PagingRequestHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.common.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.common.Values.PAGE_TYPE_POPULAR
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.action.ActionPost
import onlymash.flexbooru.data.api.DanbooruApi
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.createStatusLiveData
import java.util.concurrent.Executor

class PostBoundaryCallback(
    private val action: ActionPost,
    private val booruApis: BooruApis,
    private val scope: CoroutineScope,
    private val handleResponse: (List<Post>) -> Unit,
    ioExecutor: Executor
) : PagedList.BoundaryCallback<Post>() {

    private val isFavored = action.isFavoredQuery()

    //PagingRequestHelper
    val helper = PagingRequestHelper(ioExecutor)
    //network state
    val networkState = helper.createStatusLiveData()

    //last response posts size
    var lastResponseSize = action.limit

    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { callback ->
            val page = when (action.booru.type) {
                BOORU_TYPE_GEL -> 0
                else -> 1
            }
            createCallback(page, 0, callback)
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: Post) {
        if (action.pageType == PAGE_TYPE_POPULAR &&
            action.booru.type != BOORU_TYPE_SANKAKU) {
            return
        }
        val indexInNext = itemAtEnd.index + 1
        val limit = action.limit
        if (lastResponseSize == limit) {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) { callback ->
                val page = when (action.booru.type) {
                    BOORU_TYPE_GEL -> indexInNext/limit
                    else -> indexInNext/limit + 1
                }
                createCallback(page, indexInNext, callback)
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Post) {

    }

    private fun createCallback(page: Int, indexInNext: Int, callback: PagingRequestHelper.Request.Callback) {
        scope.launch {
            when (val result = when (action.booru.type) {
                BOORU_TYPE_DAN -> {
                    if (action.booru.host in DanbooruApi.E621_HOSTS) {
                        getPostsDanE621(page, indexInNext)
                    } else {
                        getPostsDan(page, indexInNext)
                    }
                }
                BOORU_TYPE_DAN1 -> getPostsDan1(page, indexInNext)
                BOORU_TYPE_MOE -> getPostsMoe(page, indexInNext)
                BOORU_TYPE_GEL -> getPostsGel(page, indexInNext)
                BOORU_TYPE_SHIMMIE -> getPostsShimmie(page, indexInNext)
                else -> getPostsSankaku(page, indexInNext)
            }) {
                is NetResult.Success -> {
                    callback.recordSuccess()
                    insertItemsIntoDb(result.data)
                }
                is NetResult.Error -> callback.recordFailure(Throwable(result.errorMsg))
            }
        }
    }

    private suspend fun insertItemsIntoDb(posts: List<Post>) {
        lastResponseSize = posts.size
        withContext(Dispatchers.IO) {
            handleResponse(posts)
        }
    }

    private suspend fun getPostsDan(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.getPosts(
                    action.getDanPostsUrl(page))
                if (response.isSuccessful) {
                    val posts = response.body()?.mapIndexed { index, postDan ->
                        postDan.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = indexInNext + index
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

    private suspend fun getPostsDanE621(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.danApi.getPostsE621(
                    action.getDanPostsUrl(page))
                if (response.isSuccessful) {
                    val posts = response.body()?.posts?.mapIndexed { index, postDan ->
                        postDan.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            index = indexInNext + index
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

    private suspend fun getPostsDan1(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.dan1Api.getPosts(
                    action.getDan1PostsUrl(page))
                if (response.isSuccessful) {
                    val posts = response.body()?.mapIndexed { index, postDan1 ->
                        postDan1.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = indexInNext + index,
                            isFavored = isFavored
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

    private suspend fun getPostsMoe(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.moeApi.getPosts(
                    action.getMoePostsUrl(page))
                if (response.isSuccessful) {
                    val posts = response.body()?.mapIndexed { index, postMoe ->
                        postMoe.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = indexInNext + index,
                            isFavored = isFavored
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

    private suspend fun getPostsGel(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.gelApi.getPosts(
                    action.getGelPostsUrl(page))
                if (response.isSuccessful) {
                    val posts = response.body()?.posts?.mapIndexed { index, postGel ->
                        postGel.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = indexInNext + index
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

    private suspend fun getPostsSankaku(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.sankakuApi.getPosts(
                    action.getSankakuPostsUrl(page))
                if (response.isSuccessful) {
                    val posts = response.body()?.mapIndexed { index, postSankaku ->
                        postSankaku.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = indexInNext + index,
                            isFavored = isFavored
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

    private suspend fun getPostsShimmie(page: Int, indexInNext: Int): NetResult<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = booruApis.shimmieApi.getPosts(
                    action.getShimmiePostsUrl(page))
                if (response.isSuccessful) {
                    val posts = response.body()?.posts?.mapIndexed { index, postShimmie ->
                        postShimmie.toPost(
                            booruUid = action.booru.uid,
                            query = action.query,
                            scheme = action.booru.scheme,
                            host = action.booru.host,
                            index = indexInNext + index
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