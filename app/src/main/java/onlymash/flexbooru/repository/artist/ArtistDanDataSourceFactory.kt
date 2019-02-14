package onlymash.flexbooru.repository.artist

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.entity.ArtistDan
import onlymash.flexbooru.entity.SearchArtist
import java.util.concurrent.Executor

class ArtistDanDataSourceFactory(
    private val danbooruApi: DanbooruApi,
    private val search: SearchArtist,
    private val retryExecutor: Executor) : DataSource.Factory<Int, ArtistDan>() {
    val sourceLiveData = MutableLiveData<ArtistDanDataSource>()
    override fun create(): DataSource<Int, ArtistDan> {
        val source = ArtistDanDataSource(danbooruApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}