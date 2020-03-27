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

package onlymash.flexbooru.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import onlymash.flexbooru.common.Values.DB_FILE_NAME
import onlymash.flexbooru.data.model.autocomplete.Suggestion
import onlymash.flexbooru.data.model.common.*
import onlymash.flexbooru.data.database.dao.*

@Database(
    entities = [
        (Booru::class), (User::class), (Post::class),
        (Suggestion::class), (TagFilter::class), (TagBlacklist::class),
        (Muzei::class), (Cookie::class)
    ],
    version = 1,
    exportSchema = true)
@TypeConverters(MyConverters::class)
abstract class MyDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var instance: MyDatabase? = null
        private val LOCK = Any()
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }
        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, MyDatabase::class.java, DB_FILE_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }

    abstract fun booruDao(): BooruDao
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao

    abstract fun suggestionDao(): SuggestionDao
    abstract fun tagFilterDao(): TagFilterDao
    abstract fun tagBlacklistDao(): TagBlacklistDao

    abstract fun cookieDao(): CookieDao
    abstract fun muzeiDao(): MuzeiDao
}