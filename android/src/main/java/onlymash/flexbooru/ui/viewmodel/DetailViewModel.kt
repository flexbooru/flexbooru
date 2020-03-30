package onlymash.flexbooru.ui.viewmodel

import androidx.paging.toLiveData
import onlymash.flexbooru.data.database.dao.PostDao

class DetailViewModel(postDao: PostDao, booruUid: Long, query: String) : ScopeViewModel() {

    val posts = postDao.getPosts(booruUid, query).toLiveData(pageSize = 1)

}