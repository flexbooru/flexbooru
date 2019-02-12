package onlymash.flexbooru.repository.pool

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.entity.PoolDan
import onlymash.flexbooru.entity.Search
import java.util.concurrent.Executor

class PoolDanDataSourceFactory(
    private val danbooruApi: DanbooruApi,
    private val search: Search,
    private val retryExecutor: Executor) : DataSource.Factory<Int, PoolDan>() {
    val sourceLiveData = MutableLiveData<PoolDanDataSource>()
    override fun create(): DataSource<Int, PoolDan> {
        val source = PoolDanDataSource(danbooruApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}