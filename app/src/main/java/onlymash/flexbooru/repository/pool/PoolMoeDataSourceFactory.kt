package onlymash.flexbooru.repository.pool

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.PoolMoe
import onlymash.flexbooru.entity.Search
import java.util.concurrent.Executor

class PoolMoeDataSourceFactory(
    private val moebooruApi: MoebooruApi,
    private val search: Search,
    private val retryExecutor: Executor) : DataSource.Factory<Int, PoolMoe>() {
    val sourceLiveData = MutableLiveData<PoolMoeDataSource>()
    override fun create(): DataSource<Int, PoolMoe> {
        val source = PoolMoeDataSource(moebooruApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}