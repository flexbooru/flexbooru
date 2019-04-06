package onlymash.flexbooru.repository.comment

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.entity.comment.CommentAction
import onlymash.flexbooru.entity.comment.CommentSankaku
import java.util.concurrent.Executor

class CommentSankakuDataSourceFactory(
    private val sankakuApi: SankakuApi,
    private val commentAction: CommentAction,
    private val retryExecutor: Executor
) : DataSource.Factory<Int, CommentSankaku>() {
    //source livedata
    val sourceLiveData = MutableLiveData<CommentSankakuDataSource>()
    override fun create(): DataSource<Int, CommentSankaku> {
        val source = CommentSankakuDataSource(sankakuApi, commentAction, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}