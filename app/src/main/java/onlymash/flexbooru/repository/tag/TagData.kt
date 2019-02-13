package onlymash.flexbooru.repository.tag

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.TagDan
import onlymash.flexbooru.entity.TagMoe
import onlymash.flexbooru.entity.SearchTag
import onlymash.flexbooru.repository.Listing
import java.util.concurrent.Executor

class TagData(private val danbooruApi: DanbooruApi,
               private val moebooruApi: MoebooruApi,
               private val networkExecutor: Executor
) : TagRepository {


    @MainThread
    override fun getDanTags(search: SearchTag): Listing<TagDan> {
        val sourceFactory = TagDanDataSourceFactory(danbooruApi = danbooruApi, search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = search.limit,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { tagDanDataSource ->
            tagDanDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { tagDanDataSource ->
                tagDanDataSource.networkState
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
    override fun getMoeTags(search: SearchTag): Listing<TagMoe> {
        val sourceFactory = TagMoeDataSourceFactory(moebooruApi = moebooruApi, search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = search.limit,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { tagMoeDataSource ->
            tagMoeDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { tagMoeDataSource ->
                tagMoeDataSource.networkState
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