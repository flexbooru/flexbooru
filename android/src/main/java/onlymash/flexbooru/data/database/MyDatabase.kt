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

package onlymash.flexbooru.data.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import onlymash.flexbooru.app.Values.DB_FILE_NAME
import onlymash.flexbooru.data.model.common.*
import onlymash.flexbooru.data.database.dao.*

@Database(
    entities = [
        (Booru::class), (Post::class), (TagFilter::class),
        (Muzei::class), (History::class), (Next::class)
    ],
    version = 9,
    autoMigrations = [
        AutoMigration (
            from = 7,
            to = 8
        ),
        AutoMigration (
            from = 8,
            to = 9,
            spec = MyMigration.DeleteCookiesMigrationSpec::class
        )
    ],
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
                .addMigrations(
                    MyMigration(1, 2),
                    MyMigration(2, 3),
                    MyMigration(3, 4),
                    MyMigration(4, 5),
                    MyMigration(5, 6),
                    MyMigration(6, 7)
                )
                .setQueryExecutor(Dispatchers.IO.asExecutor())
                .setTransactionExecutor(Dispatchers.IO.asExecutor())
                .build()
    }

    abstract fun booruDao(): BooruDao
    abstract fun postDao(): PostDao

    abstract fun tagFilterDao(): TagFilterDao
    abstract fun muzeiDao(): MuzeiDao
    abstract fun historyDao(): HistoryDao
    abstract fun nextDao(): NextDao
}