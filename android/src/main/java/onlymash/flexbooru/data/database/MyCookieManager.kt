package onlymash.flexbooru.data.database

import android.database.sqlite.SQLiteCantOpenDatabaseException
import onlymash.flexbooru.app.App
import onlymash.flexbooru.data.database.dao.CookieDao
import onlymash.flexbooru.data.model.common.Cookie
import org.kodein.di.instance
import java.io.IOException
import java.sql.SQLException

object MyCookieManager {

    private val cookieDao by App.app.instance<CookieDao>()

    fun createCookie(cookie: Cookie): Cookie {
        cookie.uid = 0
        val uid = cookieDao.insert(cookie)
        if (uid >= 0) {
            cookie.uid = uid
        }
        return cookie
    }

    fun updateCookie(cookie: Cookie): Boolean = cookieDao.update(cookie) == 1

    @Throws(IOException::class)
    fun getCookieByHost(host: String): Cookie? = try {
        cookieDao.getCookie(host)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        null
    }

    fun deleteCookie(uid: Long) = cookieDao.delete(uid) == 1
}