package onlymash.flexbooru.repository.tag

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.entity.TagDan
import onlymash.flexbooru.entity.SearchTag
import java.util.concurrent.Executor

class TagDanDataSourceFactory(
    private val danbooruApi: DanbooruApi,
    private val search: SearchTag,
    private val retryExecutor: Executor) : DataSource.Factory<Int, TagDan>() {
    val sourceLiveData = MutableLiveData<TagDanDataSource>()
    override fun create(): DataSource<Int, TagDan> {
        val source = TagDanDataSource(danbooruApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}