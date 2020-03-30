package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.ViewModel
import onlymash.flexbooru.data.database.dao.PostDao

class ShortcutViewModel(postDao: PostDao, booruUid: Long, postId: Int) : ViewModel() {

    val post = postDao.getFirstPostLiveData(booruUid, postId)

}