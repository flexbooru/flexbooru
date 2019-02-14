package onlymash.flexbooru.repository.artist

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.SearchArtist
import onlymash.flexbooru.entity.ArtistMoe
import java.util.concurrent.Executor

class ArtistMoeDataSourceFactory(
    private val moebooruApi: MoebooruApi,
    private val search: SearchArtist,
    private val retryExecutor: Executor) : DataSource.Factory<Int, ArtistMoe>() {
    val sourceLiveData = MutableLiveData<ArtistMoeDataSource>()
    override fun create(): DataSource<Int, ArtistMoe> {
        val source = ArtistMoeDataSource(moebooruApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}