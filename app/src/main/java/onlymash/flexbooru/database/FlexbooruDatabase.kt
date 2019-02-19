/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import onlymash.flexbooru.App.Companion.app
import onlymash.flexbooru.Constants
import onlymash.flexbooru.database.dao.*
import onlymash.flexbooru.entity.*

@Database(entities = [
    (PostMoe::class), (PostDan::class),
    (Booru::class), (User::class),
    (PoolDan::class), (PoolMoe::class),
    (TagDan::class), (TagMoe::class),
    (ArtistDan::class), (ArtistMoe::class),
    (TagFilter::class)],
    version = 7, exportSchema = true)
@TypeConverters(Converters::class)
abstract class FlexbooruDatabase : RoomDatabase() {

    companion object {
        val instance by lazy {
            Room.databaseBuilder(app, FlexbooruDatabase::class.java, Constants.DB_FILE_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        }
        val booruDao get() = instance.booruDao()
        val userDao get() = instance.userDao()
        val tagFilterDao get() = instance.tagFilterDao()
    }

    abstract fun postDanDao(): PostDanDao

    abstract fun postMoeDao(): PostMoeDao

    abstract fun booruDao(): BooruDao

    abstract fun userDao(): UserDao

    abstract fun poolDanDao(): PoolDanDao

    abstract fun poolMoeDao(): PoolMoeDao

    abstract fun tagDanDao(): TagDanDao

    abstract fun tagMoeDao(): TagMoeDao

    abstract fun artistDanDao(): ArtistDanDao

    abstract fun artistMoeDao(): ArtistMoeDao

    abstract fun tagFilterDao(): TagFilterDao
}