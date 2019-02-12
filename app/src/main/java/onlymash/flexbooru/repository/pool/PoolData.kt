package onlymash.flexbooru.repository.pool

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.PoolDan
import onlymash.flexbooru.entity.PoolMoe
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.repository.Listing
import java.util.concurrent.Executor

class PoolData(private val danbooruApi: DanbooruApi,
               private val moebooruApi: MoebooruApi,
               private val networkExecutor: Executor
) : PoolRepository {


    @MainThread
    override fun getDanPools(search: Search): Listing<PoolDan> {
        val sourceFactory = PoolDanDataSourceFactory(danbooruApi = danbooruApi, search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = search.limit,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { poolDanDataSource ->
            poolDanDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { poolDanDataSource ->
                poolDanDataSource.networkState
            },
            retry = {
                sourceFactory.sourceLiveData.value?.retryAllFailed()
            },
            refresh = {
                sourceFactory.sourceLiveData.value?.invalidate()
            },
            refreshState = refreshState
        )
    }

    @MainThread
    override fun getMoePools(search: Search): Listing<PoolMoe> {
        val sourceFactory = PoolMoeDataSourceFactory(moebooruApi = moebooruApi, search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 20,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { poolMoeDataSource ->
            poolMoeDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { poolMoeDataSource ->
                poolMoeDataSource.networkState
            },
            retry = {
                sourceFactory.sourceLiveData.value?.retryAllFailed()
            },
            refresh = {
                sourceFactory.sourceLiveData.value?.invalidate()
            },
            refreshState = refreshState
        )
    }
}