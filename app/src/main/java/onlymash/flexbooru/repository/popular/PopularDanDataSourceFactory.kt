package onlymash.flexbooru.repository.popular

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.model.Popular
import onlymash.flexbooru.model.PostDan
import java.util.concurrent.Executor

class PopularDanDataSourceFactory(
    private val danbooruApi: DanbooruApi,
    private val db: FlexbooruDatabase,
    private val popular: Popular,
    private val retryExecutor: Executor) : DataSource.Factory<Int, PostDan>(){

    val sourceLiveData = MutableLiveData<PopularDanDataSource>()

    override fun create(): DataSource<Int, PostDan> {
        val source = PopularDanDataSource(danbooruApi, db, popular, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}