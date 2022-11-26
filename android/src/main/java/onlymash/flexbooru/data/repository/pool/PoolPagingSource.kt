package onlymash.flexbooru.data.repository.pool

import androidx.paging.PagingSource
import androidx.paging.PagingState
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.data.action.ActionPool
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.model.common.Pool

class PoolPagingSource(
    private val action: ActionPool,
    private val booruApis: BooruApis
) : PagingSource<Int, Pool>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pool> {
        val page = params.key ?: return LoadResult.Page(
            data = listOf(),
            prevKey = null,
            nextKey = null
        )
        return when (action.booru.type) {
           Values.BOORU_TYPE_DAN -> getDanPools(action, page)
           Values.BOORU_TYPE_DAN1 -> getDan1Pools(action, page)
           Values.BOORU_TYPE_MOE -> getMoePools(action, page)
           else -> getSankakuPools(action, page)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Pool>): Int {
        return 1
    }

    private suspend fun getDanPools(action: ActionPool, page: Int): LoadResult<Int, Pool> {
        return try {
            val response =  booruApis.danApi.getPools(action.getDanPoolsUrl(page))
            if (response.isSuccessful) {
                val pools = response.body()?.map { it.toPool(action.booru.scheme, action.booru.host) } ?: listOf()
                LoadResult.Page(
                    data = pools,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (pools.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getDan1Pools(action: ActionPool, page: Int): LoadResult<Int, Pool> {
        return try {
            val response =  booruApis.dan1Api.getPools(action.getDan1PoolsUrl(page))
            if (response.isSuccessful) {
                val pools = response.body()?.map { it.toPool(action.booru.scheme, action.booru.host) } ?: listOf()
                LoadResult.Page(
                    data = pools,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (pools.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getMoePools(action: ActionPool, page: Int): LoadResult<Int, Pool> {
        return try {
            val response =  booruApis.moeApi.getPools(action.getMoePoolsUrl(page))
            if (response.isSuccessful) {
                val pools = response.body()?.map { it.toPool(action.booru.scheme, action.booru.host) } ?: listOf()
                LoadResult.Page(
                    data = pools,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (pools.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getSankakuPools(action: ActionPool, page: Int): LoadResult<Int, Pool> {
        return try {
            val response =  booruApis.sankakuApi.getPools(action.getSankakuPoolsUrl(page))
            if (response.isSuccessful) {
                val pools = response.body()?.map { it.toPool(action.booru.scheme, action.booru.host) } ?: listOf()
                LoadResult.Page(
                    data = pools,
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (pools.size == action.limit) page + 1 else null
                )
            } else {
                LoadResult.Error(Throwable("code: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}