package onlymash.flexbooru.data.database

import onlymash.flexbooru.app.App
import onlymash.flexbooru.data.database.dao.NextDao
import onlymash.flexbooru.data.model.common.Next
import org.koin.android.ext.android.inject

object NextManager {

    private val nextDao by App.app.inject<NextDao>()

    fun create(next: Next) {
        nextDao.insert(next)
    }

    fun delete(booruUid: Long, query: String) {
        nextDao.delete(booruUid, query)
    }

    fun getNext(booruUid: Long, query: String): Next? {
        return nextDao.getNext(booruUid, query)
    }
}