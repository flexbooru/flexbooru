/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

import androidx.preference.PreferenceManager
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.MyDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module


val appModules = module {
    single { PreferenceManager.getDefaultSharedPreferences(androidApplication()) }
    single { MyDatabase(androidApplication()) }
    single { get<MyDatabase>().booruDao() }
    single { get<MyDatabase>().postDao() }
    single { get<MyDatabase>().tagFilterDao() }
    single { get<MyDatabase>().muzeiDao() }
    single { get<MyDatabase>().historyDao() }
    single { get<MyDatabase>().nextDao() }
    single { BooruApis() }
}