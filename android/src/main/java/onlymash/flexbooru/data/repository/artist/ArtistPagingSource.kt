package onlymash.flexbooru.data.repository.artist

import androidx.paging.PagingSource
import androidx.paging.PagingState
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.data.action.ActionArtist
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.model.common.Artist

class ArtistPagingSource(
    private val action: ActionArtist,
    private val booruApis: BooruApis
) : PagingSource<Int, Artist>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Artist> {
        val page = params.key
        return if (page != null && page > 0) {
            getArtists(action, page)
        } else {
            LoadResult.Page(
                data = listOf(),
                prevKey = null,
                nextKey = null
            )
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Artist>): Int {
         return 1
    }

    private suspend fun getArtists(action: ActionArtist, page: Int): LoadResult<Int, Artist> {
        if (action.booru.type == Values.BOORU_TYPE_DAN) {
            return getArtistsDan(action, page)
        }
        return try {
            val response =  when (action.booru.type) {
                Values.BOORU_TYPE_DAN1 -> booruApis.dan1Api.getArtists(action.getDan1ArtistsUrl(page))
                else -> booruApis.moeApi.getArtists(action.getMoeArtistsUrl(page))
            }
            if (response.isSuccessful) {
                val artists = response.body() ?: listOf()
                LoadResult.Page(
                    data = artists,
                    prevKey = page - 1,
                    nextKey = if (artists.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getArtistsDan(action: ActionArtist, page: Int): LoadResult<Int, Artist> {
        return try {
            val response =  booruApis.danApi.getArtists(action.getDanArtistsUrl(page))
            if (response.isSuccessful) {
                val artists = response.body()?.map { it.toArtist() } ?: listOf()
                LoadResult.Page(
                    data = artists,
                    prevKey = page - 1,
                    nextKey = if (artists.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}