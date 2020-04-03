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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import onlymash.flexbooru.common.Values.DB_FILE_NAME
import onlymash.flexbooru.data.model.common.*
import onlymash.flexbooru.data.database.dao.*

@Database(
    entities = [
        (Booru::class), (Post::class), (TagFilter::class),
        (Muzei::class), (Cookie::class)
    ],
    version = 2,
    exportSchema = true)
@TypeConverters(MyConverters::class)
abstract class MyDatabase : RoomDatabase() {

    companion object {
        private val MIGRATION_1_2 by lazy {
            object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE  `boorus` ADD COLUMN `path` TEXT")
                }
            }
        }
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
                .addMigrations(MIGRATION_1_2)
                .build()
    }

    abstract fun booruDao(): BooruDao
    abstract fun postDao(): PostDao

    abstract fun tagFilterDao(): TagFilterDao
    abstract fun cookieDao(): CookieDao
    abstract fun muzeiDao(): MuzeiDao
}