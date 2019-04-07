package onlymash.flexbooru.database

import android.database.sqlite.SQLiteCantOpenDatabaseException
import com.crashlytics.android.Crashlytics
import onlymash.flexbooru.entity.Cookie
import java.io.IOException
import java.sql.SQLException

object CookieManager {

    @Throws(SQLException::class)
    fun createCookie(cookie: Cookie): Cookie {
        cookie.uid = FlexbooruDatabase.cookieDao.insert(cookie)
        return cookie
    }

    @Throws(IOException::class)
    fun getCookieByBooruUid(booruUid: Long): Cookie? = try {
        FlexbooruDatabase.cookieDao.getCookieByBooruUid(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        Crashlytics.logException(ex)
        null
    }
}