package onlymash.flexbooru.repository.artist

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.ArtistDan
import onlymash.flexbooru.entity.ArtistMoe
import onlymash.flexbooru.entity.SearchArtist
import onlymash.flexbooru.repository.Listing
import java.util.concurrent.Executor

class ArtistData(private val danbooruApi: DanbooruApi,
               private val moebooruApi: MoebooruApi,
               private val networkExecutor: Executor
) : ArtistRepository {


    @MainThread
    override fun getDanArtists(search: SearchArtist): Listing<ArtistDan> {
        val sourceFactory = ArtistDanDataSourceFactory(danbooruApi = danbooruApi, search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = search.limit,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { artistDanDataSource ->
            artistDanDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { artistDanDataSource ->
                artistDanDataSource.networkState
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
    override fun getMoeArtists(search: SearchArtist): Listing<ArtistMoe> {
        val sourceFactory = ArtistMoeDataSourceFactory(moebooruApi = moebooruApi, search = search, retryExecutor = networkExecutor)
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 25,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { artistMoeDataSource ->
            artistMoeDataSource.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { artistMoeDataSource ->
                artistMoeDataSource.networkState
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