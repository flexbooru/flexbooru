package onlymash.flexbooru.repository.popular

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.SearchPopular
import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.PostMoe
import onlymash.flexbooru.repository.Listing
import java.util.concurrent.Executor

class PopularData(
    private val danbooruApi: DanbooruApi,
    private val moebooruApi: MoebooruApi,
    private val db: FlexbooruDatabase,
    private val networkExecutor: Executor) : PopularRepository {


    @MainThread
    override fun getDanPopular(popular: SearchPopular): Listing<PostDan> {
        val sourceFactory = PopularDanDataSourceFactory(danbooruApi, db, popular, networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 20,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { popularMoeDataSource ->
            popularMoeDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { popularDanDataSource ->
                popularDanDataSource.networkState
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
    override fun getMoePopular(popular: SearchPopular): Listing<PostMoe> {
        val sourceFactory = PopularMoeDataSourceFactory(moebooruApi, db, popular, networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 40,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { popularMoeDataSource ->
            popularMoeDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { popularMoeDataSource ->
                popularMoeDataSource.networkState
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