/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.app

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.MyDatabase
import org.kodein.di.*

fun appModule(applicationContext: Context) = DI.Module("AppModule") {
    bind<Context>() with singleton { applicationContext }
    bind<SharedPreferences>() with provider { PreferenceManager.getDefaultSharedPreferences(instance()) }
    bind() from singleton { MyDatabase(instance()) }
    bind() from singleton { instance<MyDatabase>().booruDao() }
    bind() from singleton { instance<MyDatabase>().tagFilterDao() }
    bind() from singleton { instance<MyDatabase>().muzeiDao() }
    bind() from singleton { instance<MyDatabase>().postDao() }
    bind() from singleton { instance<MyDatabase>().historyDao() }
    bind() from singleton { BooruApis() }
}