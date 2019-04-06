package onlymash.flexbooru.repository.popular

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.post.SearchPopular
import onlymash.flexbooru.entity.post.PostSankaku
import java.util.concurrent.Executor

/**
 * Sankaku popular posts data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI. See the Listing creation
 * in the Repository class.
 */
class PopularSankakuDataSourceFactory(
    private val sankakuApi: SankakuApi,
    private val db: FlexbooruDatabase,
    private val popular: SearchPopular,
    private val retryExecutor: Executor) : DataSource.Factory<Int, PostSankaku>(){
    //source livedata
    val sourceLiveData = MutableLiveData<PopularSankakuDataSource>()
    override fun create(): DataSource<Int, PostSankaku> {
        val source = PopularSankakuDataSource(sankakuApi, db, popular, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}