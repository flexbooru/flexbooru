package onlymash.flexbooru.repository.tag

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.GelbooruApi
import onlymash.flexbooru.entity.tag.SearchTag
import onlymash.flexbooru.entity.tag.TagGel
import java.util.concurrent.Executor

/**
 * Gelbooru tags data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI. See the Listing creation
 * in the Repository class.
 */
class TagGelDataSourceFactory(
    private val gelbooruApi: GelbooruApi,
    private val search: SearchTag,
    private val retryExecutor: Executor) : DataSource.Factory<Int, TagGel>() {
    //source livedata
    val sourceLiveData = MutableLiveData<TagGelDataSource>()
    override fun create(): DataSource<Int, TagGel> {
        val source = TagGelDataSource(gelbooruApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}