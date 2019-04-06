package onlymash.flexbooru.repository.tag

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.entity.tag.SearchTag
import onlymash.flexbooru.entity.tag.TagSankaku
import java.util.concurrent.Executor

class TagSankakuDataSourceFactory(
    private val sankakuApi: SankakuApi,
    private val search: SearchTag,
    private val retryExecutor: Executor
) : DataSource.Factory<Int, TagSankaku>() {
    //source livedata
    val sourceLiveData = MutableLiveData<TagSankakuDataSource>()
    override fun create(): DataSource<Int, TagSankaku> {
        val source = TagSankakuDataSource(sankakuApi, search, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}