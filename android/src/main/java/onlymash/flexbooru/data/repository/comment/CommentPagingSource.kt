package onlymash.flexbooru.data.repository.comment

import androidx.paging.PagingSource
import androidx.paging.PagingState
import okhttp3.HttpUrl
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.data.action.ActionComment
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.model.common.Comment

class CommentPagingSource(
    private val action: ActionComment,
    private val booruApis: BooruApis
) : PagingSource<Int, Comment>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Comment> {
        val page = params.key ?: return LoadResult.Page(
            data = listOf(),
            prevKey = null,
            nextKey = null
        )
        return when (action.booru.type) {
            Values.BOORU_TYPE_DAN -> getDanComments(action, page)
            Values.BOORU_TYPE_DAN1 -> getDan1Comments(action, page)
            Values.BOORU_TYPE_MOE -> getMoeComments(action, page)
            Values.BOORU_TYPE_SANKAKU -> getSankakuComments(action, page)
            else -> getGelComments(action, page)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Comment>): Int {
        return if (action.booru.type == Values.BOORU_TYPE_GEL || action.booru.type == Values.BOORU_TYPE_GEL_LEGACY) 0 else 1
    }

    private fun ActionComment.getDanUrl(page: Int): HttpUrl =
        if (postId > 0) getDanPostCommentUrl(page) else getDanPostsCommentUrl(page)

    private fun ActionComment.getDan1Url(page: Int): HttpUrl =
        when {
            postId > 0 -> getDan1PostCommentUrl(page)
            query.isNotEmpty() -> getDan1PostsCommentSearchUrl(page)
            else -> getDan1PostsCommentIndexUrl(page)
        }

    private fun ActionComment.getMoeUrl(page: Int): HttpUrl =
        when {
            postId > 0 -> getMoePostCommentUrl(page)
            else -> getMoePostsCommentUrl(page)
        }

    private fun ActionComment.getGelUrl(page: Int): HttpUrl =
        if (postId > 0) getGelPostCommentUrl(page) else getGelPostsCommentUrl(page)

    private suspend fun getDanComments(action: ActionComment, page: Int): LoadResult<Int, Comment> {
        return try {
            val response =  booruApis.danApi.getComments(action.getDanUrl(page))
            if (response.isSuccessful) {
                val comments = response.body()?.map { it.toComment() } ?: listOf()
                LoadResult.Page(
                    data = comments,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (comments.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getDan1Comments(action: ActionComment, page: Int): LoadResult<Int, Comment> {
        return try {
            val response =  booruApis.dan1Api.getComments(action.getDan1Url(page))
            if (response.isSuccessful) {
                val comments = response.body()?.map { it.toComment() } ?: listOf()
                LoadResult.Page(
                    data = comments,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (comments.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getMoeComments(action: ActionComment, page: Int): LoadResult<Int, Comment> {
        return try {
            val response =  booruApis.moeApi.getComments(action.getMoeUrl(page))
            if (response.isSuccessful) {
                val comments = response.body()?.map { it.toComment() } ?: listOf()
                LoadResult.Page(
                    data = comments,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (comments.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getSankakuComments(action: ActionComment, page: Int): LoadResult<Int, Comment> {
        return when {
            action.postId > 0 -> getSankakuPostComments(action, page)
            else -> getSankakuPostsComments(action, page)
        }
    }

    private suspend fun getSankakuPostComments(action: ActionComment, page: Int): LoadResult<Int, Comment> {
        return try {
            val scheme = action.booru.scheme
            val host = action.booru.host
            val response =  booruApis.sankakuApi.getPostComments(
                url = action.getSankakuPostCommentUrl(page),
                auth = action.booru.user?.getAuth.toString()
            )
            if (response.isSuccessful) {
                val comments = response.body()?.map { it.toComment(scheme, host) } ?: listOf()
                LoadResult.Page(
                    data = comments,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (comments.isNotEmpty()) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getSankakuPostsComments(action: ActionComment, page: Int): LoadResult<Int, Comment> {
        return try {
            val scheme = action.booru.scheme
            val host = action.booru.host
            val response =  booruApis.sankakuApi.getPostsComments(
                url = action.getSankakuPostsCommentUrl(page),
                auth = action.booru.user?.getAuth.toString()
            )
            if (response.isSuccessful) {
                val posts = response.body() ?: listOf()
                val comments = mutableListOf<Comment>()
                posts.forEach { comment ->
                    if (comment.comments.isNotEmpty()) {
                        comments.add(comment.comments[0].toComment(scheme, host))
                    }
                }
                LoadResult.Page(
                    data = comments,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (comments.isNotEmpty()) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getGelComments(action: ActionComment, page: Int): LoadResult<Int, Comment> {
        return try {
            val response =  booruApis.gelApi.getComments(action.getGelUrl(page))
            if (response.isSuccessful) {
                val comments = response.body()?.comments?.map { it.toComment() } ?: listOf()
                LoadResult.Page(
                    data = comments,
                    prevKey = if (page > 0) page - 1 else null,
                    nextKey = if (comments.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}