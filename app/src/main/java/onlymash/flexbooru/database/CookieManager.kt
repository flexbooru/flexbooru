/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru.database

import android.database.sqlite.SQLiteCantOpenDatabaseException
import com.crashlytics.android.Crashlytics
import onlymash.flexbooru.common.App
import onlymash.flexbooru.database.dao.CookieDao
import onlymash.flexbooru.entity.Cookie
import org.kodein.di.generic.instance
import java.io.IOException
import java.sql.SQLException

object CookieManager {

    private val cookieDao: CookieDao by App.app.instance()

    @Throws(SQLException::class)
    fun createCookie(cookie: Cookie): Cookie {
        cookie.uid = cookieDao.insert(cookie)
        return cookie
    }

    @Throws(IOException::class)
    fun getCookieByBooruUid(booruUid: Long): Cookie? = try {
        cookieDao.getCookieByBooruUid(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        Crashlytics.logException(ex)
        null
    }

    @Throws(SQLException::class)
    fun deleteByBooruUid(booruUid: Long) {
        cookieDao.deleteByBooruUid(booruUid)
    }
}