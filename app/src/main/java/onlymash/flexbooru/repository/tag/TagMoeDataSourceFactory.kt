package onlymash.flexbooru.repository.tag

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.entity.SearchTag
import onlymash.flexbooru.entity.TagMoe
import java.util.concurrent.Executor

class TagMoeDataSourceFactory(
    private val moebooruApi: MoebooruApi,
    private val search: SearchTag,
    private val retryExecutor: Executor) : DataSource.Factory<Int, TagMoe>() {
    val sourceLiveData = MutableLiveData<TagMoeDataSource>()
    override fun create(): DataSource<Int, TagMoe> {
        val source = TagMoeDataSource(moebooruApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}