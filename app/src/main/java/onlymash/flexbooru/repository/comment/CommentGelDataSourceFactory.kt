package onlymash.flexbooru.repository.comment


import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import onlymash.flexbooru.api.GelbooruApi
import onlymash.flexbooru.entity.comment.CommentAction
import onlymash.flexbooru.entity.comment.CommentGel
import java.util.concurrent.Executor

//Gelbooru comment data source factory
class CommentGelDataSourceFactory(
    private val gelbooruApi: GelbooruApi,
    private val commentAction: CommentAction,
    private val retryExecutor: Executor
) : DataSource.Factory<Int, CommentGel>() {
    //source livedata
    val sourceLiveData = MutableLiveData<CommentGelDataSource>()
    override fun create(): DataSource<Int, CommentGel> {
        val source = CommentGelDataSource(gelbooruApi, commentAction, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}