package onlymash.flexbooru.repository.pool

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.entity.pool.PoolSankaku
import onlymash.flexbooru.entity.Search
import java.util.concurrent.Executor

//sankaku pools data source factory
class PoolSankakuDataSourceFactory(
    private val sankakuApi: SankakuApi,
    private val search: Search,
    private val retryExecutor: Executor) : DataSource.Factory<Int, PoolSankaku>() {
    //source livedata
    val sourceLiveData = MutableLiveData<PoolSankakuDataSource>()
    override fun create(): DataSource<Int, PoolSankaku> {
        val source = PoolSankakuDataSource(sankakuApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}