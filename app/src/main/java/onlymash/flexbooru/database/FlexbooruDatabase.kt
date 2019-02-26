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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    (TagFilter::class), (Muzei::class)],
    version = 10, exportSchema = true)
@TypeConverters(Converters::class)
abstract class FlexbooruDatabase : RoomDatabase() {

    companion object {
        private val MIGRATION_8_9 by lazy {
            object : Migration(8, 9) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE `posts_moebooru`")
                    database.execSQL("CREATE TABLE IF NOT EXISTS `posts_moebooru` (`indexInResponse` INTEGER NOT NULL, `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `host` TEXT NOT NULL, `keyword` TEXT NOT NULL, `id` INTEGER NOT NULL, `tags` TEXT, `created_at` INTEGER NOT NULL, `creator_id` INTEGER NOT NULL, `author` TEXT NOT NULL, `change` INTEGER NOT NULL, `source` TEXT, `score` INTEGER NOT NULL, `md5` TEXT NOT NULL, `file_size` INTEGER NOT NULL, `file_url` TEXT, `file_ext` TEXT, `is_shown_in_index` INTEGER NOT NULL, `preview_url` TEXT NOT NULL, `preview_width` INTEGER NOT NULL, `preview_height` INTEGER NOT NULL, `actual_preview_width` INTEGER NOT NULL, `actual_preview_height` INTEGER NOT NULL, `sample_url` TEXT, `sample_width` INTEGER NOT NULL, `sample_height` INTEGER NOT NULL, `sample_file_size` INTEGER NOT NULL, `jpeg_url` TEXT, `jpeg_width` INTEGER NOT NULL, `jpeg_height` INTEGER NOT NULL, `jpeg_file_size` INTEGER NOT NULL, `rating` TEXT NOT NULL, `has_children` INTEGER NOT NULL, `parent_id` INTEGER, `status` TEXT NOT NULL, `width` INTEGER NOT NULL, `height` INTEGER NOT NULL, `is_held` INTEGER NOT NULL)")
                    database.execSQL("CREATE UNIQUE INDEX `index_posts_moebooru_host_keyword_id` ON `posts_moebooru` (`host`, `keyword`, `id`)")
                }
            }
        }
        private val MIGRATION_9_10 by lazy {
            object : Migration(9, 10) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `muzei` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `booru_uid` INTEGER NOT NULL, `keyword` TEXT, FOREIGN KEY(`booru_uid`) REFERENCES `boorus`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    database.execSQL("CREATE UNIQUE INDEX `index_muzei_booru_uid_keyword` ON `muzei` (`booru_uid`, `keyword`)")
                }
            }
        }
        val instance by lazy {
            Room.databaseBuilder(app, FlexbooruDatabase::class.java, Constants.DB_FILE_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .addMigrations(
                    MIGRATION_8_9,
                    MIGRATION_9_10
                )
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

    abstract fun muzeiDao(): MuzeiDao
}