package onlymash.flexbooru.data.repository.tag

import androidx.paging.PagingSource
import androidx.paging.PagingState
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.data.action.ActionTag
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.model.common.Tag

class TagPagingSource(
    private val action: ActionTag,
    private val booruApis: BooruApis
) : PagingSource<Int, Tag>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Tag> {
        val page = params.key ?: return LoadResult.Page(
            data = listOf(),
            prevKey = null,
            nextKey = null
        )
        return when (action.booru.type) {
            Values.BOORU_TYPE_DAN -> getDanTags(action, page)
            Values.BOORU_TYPE_DAN1 -> getDan1Tags(action, page)
            Values.BOORU_TYPE_MOE -> getMoeTags(action, page)
            Values.BOORU_TYPE_SANKAKU -> getSankakuTags(action, page)
            else -> getGelTags(action, page)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Tag>): Int {
        return if (action.booru.type == Values.BOORU_TYPE_GEL) 0 else 1
    }

    private suspend fun getDanTags(action: ActionTag, page: Int): LoadResult<Int, Tag> {
        return try {
            val response =  booruApis.danApi.getTags(action.getDanTagsUrl(page))
            if (response.isSuccessful) {
                val tags = response.body()?.map { it.toTag() } ?: listOf()
                LoadResult.Page(
                    data = tags,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (tags.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getDan1Tags(action: ActionTag, page: Int): LoadResult<Int, Tag> {
        return try {
            val response =  booruApis.dan1Api.getTags(action.getDan1TagsUrl(page))
            if (response.isSuccessful) {
                val tags = response.body()?.map { it.toTag() } ?: listOf()
                LoadResult.Page(
                    data = tags,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (tags.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getMoeTags(action: ActionTag, page: Int): LoadResult<Int, Tag> {
        return try {
            val response =  booruApis.moeApi.getTags(action.getMoeTagsUrl(page))
            if (response.isSuccessful) {
                val tags = response.body()?.map { it.toTag() } ?: listOf()
                LoadResult.Page(
                    data = tags,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (tags.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getSankakuTags(action: ActionTag, page: Int): LoadResult<Int, Tag> {
        return try {
            val response =  booruApis.sankakuApi.getTags(action.getSankakuTagsUrl(page))
            if (response.isSuccessful) {
                val tags = response.body()?.map { it.toTag() } ?: listOf()
                LoadResult.Page(
                    data = tags,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (tags.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getGelTags(action: ActionTag, page: Int): LoadResult<Int, Tag> {
        return if (action.booru.type == Values.BOORU_TYPE_GEL_LEGACY) {
            try {
                val response =  booruApis.gelApi.getTagsLegacy(action.getGelTagsUrl(page))
                if (response.isSuccessful) {
                    val tags = response.body()?.tags?.map { it.toTag() } ?: listOf()
                    LoadResult.Page(
                        data = tags,
                        prevKey = if (page > 0) page - 1 else null,
                        nextKey = if (tags.size == action.limit) page + 1 else null
                    )
                } else {
                    LoadResult.Error(Throwable("code: ${response.code()}"))
                }
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        } else {
            try {
                val response =  booruApis.gelApi.getTags(action.getGelTagsUrl(page))
                if (response.isSuccessful) {
                    val tags = response.body()?.tags?.map { it.toTag() } ?: listOf()
                    LoadResult.Page(
                        data = tags,
                        prevKey = if (page > 0) page - 1 else null,
                        nextKey = if (tags.size == action.limit) page + 1 else null
                    )
                } else {
                    LoadResult.Error(Throwable("code: ${response.code()}"))
                }
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }
    }
}