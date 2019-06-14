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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.database.dao.*
import onlymash.flexbooru.entity.*
import onlymash.flexbooru.entity.post.*

@Database(entities = [
    (PostMoe::class), (PostDan::class), (PostDanOne::class), (PostGel::class), (PostSankaku::class),
    (Booru::class), (User::class), (Suggestion::class), (Cookie::class),
    (TagFilter::class), (Muzei::class), (TagBlacklist::class)],
    version = 23, exportSchema = true)
@TypeConverters(Converters::class)
abstract class FlexbooruDatabase : RoomDatabase() {

    companion object {
        private val MIGRATION_10_11 by lazy {
            object : Migration(10, 11) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE `muzei`")
                    database.execSQL("CREATE TABLE IF NOT EXISTS `muzei` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `booru_uid` INTEGER NOT NULL, `keyword` TEXT NOT NULL, FOREIGN KEY(`booru_uid`) REFERENCES `boorus`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    database.execSQL("CREATE UNIQUE INDEX `index_muzei_booru_uid_keyword` ON `muzei` (`booru_uid`, `keyword`)")
                }
            }
        }
        private val MIGRATION_11_12 by lazy {
            object : Migration(11, 12) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE `posts_danbooru`")
                    database.execSQL("CREATE TABLE IF NOT EXISTS `posts_danbooru` (`indexInResponse` INTEGER NOT NULL, `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `host` TEXT NOT NULL, `keyword` TEXT NOT NULL, `id` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `uploader_id` INTEGER NOT NULL, `score` INTEGER NOT NULL, `source` TEXT NOT NULL, `md5` TEXT, `last_comment_bumped_at` TEXT, `rating` TEXT NOT NULL, `image_width` INTEGER NOT NULL, `image_height` INTEGER NOT NULL, `tag_string` TEXT NOT NULL, `is_note_locked` INTEGER NOT NULL, `fav_count` INTEGER NOT NULL, `file_ext` TEXT, `last_noted_at` TEXT, `is_rating_locked` INTEGER NOT NULL, `parent_id` INTEGER, `has_children` INTEGER NOT NULL, `approver_id` INTEGER, `tag_count_general` INTEGER NOT NULL, `tag_count_artist` INTEGER NOT NULL, `tag_count_character` INTEGER NOT NULL, `tag_count_copyright` INTEGER NOT NULL, `file_size` INTEGER NOT NULL, `is_status_locked` INTEGER NOT NULL, `pool_string` TEXT, `up_score` INTEGER NOT NULL, `down_score` INTEGER NOT NULL, `is_pending` INTEGER NOT NULL, `is_flagged` INTEGER NOT NULL, `is_deleted` INTEGER NOT NULL, `tag_count` INTEGER NOT NULL, `updated_at` TEXT NOT NULL, `is_banned` INTEGER NOT NULL, `pixiv_id` INTEGER NOT NULL, `last_commented_at` TEXT, `has_active_children` INTEGER NOT NULL, `bit_flags` INTEGER NOT NULL, `tag_count_meta` INTEGER NOT NULL, `uploader_name` TEXT NOT NULL, `has_large` INTEGER NOT NULL, `has_visible_children` INTEGER NOT NULL, `children_ids` TEXT, `is_favorited` INTEGER NOT NULL, `tag_string_general` TEXT, `tag_string_character` TEXT, `tag_string_copyright` TEXT, `tag_string_artist` TEXT, `tag_string_meta` TEXT, `file_url` TEXT, `large_file_url` TEXT, `preview_file_url` TEXT)")
                    database.execSQL("CREATE UNIQUE INDEX `index_posts_danbooru_host_keyword_id` ON `posts_danbooru` (`host`, `keyword`, `id`)")
                }
            }
        }
        private val MIGRATION_12_13 by lazy {
            object : Migration(12, 13) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `tags_filter_tmp` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `booru_uid` INTEGER NOT NULL, `name` TEXT NOT NULL, `type` INTEGER NOT NULL, FOREIGN KEY(`booru_uid`) REFERENCES `boorus`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    database.execSQL("INSERT INTO `tags_filter_tmp` SELECT `uid`, `booru_uid`, `name`, `type` FROM `tags_filter`")
                    database.execSQL("DROP TABLE `tags_filter`")
                    database.execSQL("ALTER TABLE `tags_filter_tmp` RENAME TO `tags_filter`")
                    database.execSQL("CREATE UNIQUE INDEX `index_tags_filter_booru_uid_name` ON `tags_filter` (`booru_uid`, `name`)")
                }
            }
        }
        private val MIGRATION_13_14 by lazy {
            object : Migration(13, 14) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `posts_danbooru_one` (`indexInResponse` INTEGER NOT NULL, `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `host` TEXT NOT NULL, `keyword` TEXT NOT NULL, `id` INTEGER NOT NULL, `status` TEXT NOT NULL, `creator_id` INTEGER NOT NULL, `preview_width` INTEGER NOT NULL, `source` TEXT, `author` TEXT NOT NULL, `width` INTEGER NOT NULL, `score` INTEGER NOT NULL, `preview_height` INTEGER NOT NULL, `has_comments` INTEGER NOT NULL, `sample_width` INTEGER NOT NULL, `has_children` INTEGER NOT NULL, `sample_url` TEXT, `file_url` TEXT, `parent_id` INTEGER, `sample_height` INTEGER NOT NULL, `md5` TEXT NOT NULL, `tags` TEXT NOT NULL, `change` INTEGER NOT NULL, `has_notes` INTEGER NOT NULL, `rating` TEXT NOT NULL, `height` INTEGER NOT NULL, `preview_url` TEXT NOT NULL, `file_size` INTEGER NOT NULL, `created_at` TEXT NOT NULL)")
                    database.execSQL("CREATE UNIQUE INDEX `index_posts_danbooru_one_host_keyword_id` ON `posts_danbooru_one` (`host`, `keyword`, `id`)")
                }
            }
        }
        private val MIGRATION_14_15 by lazy {
            object : Migration(14, 15) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE `artists_moebooru`")
                    database.execSQL("DROP TABLE `pools_moebooru`")
                    database.execSQL("DROP TABLE `tags_moebooru`")
                    database.execSQL("DROP TABLE `artists_danbooru`")
                    database.execSQL("DROP TABLE `pools_danbooru`")
                    database.execSQL("DROP TABLE `tags_danbooru`")

                    database.execSQL("DELETE FROM `posts_danbooru`")
                    database.execSQL("DELETE FROM `posts_danbooru_one`")
                    database.execSQL("DELETE FROM `posts_moebooru`")
                    database.execSQL("ALTER TABLE `posts_danbooru` ADD COLUMN `scheme` TEXT NOT NULL DEFAULT('http')")
                    database.execSQL("ALTER TABLE `posts_danbooru_one` ADD COLUMN `scheme` TEXT NOT NULL DEFAULT('http')")
                    database.execSQL("ALTER TABLE `posts_moebooru` ADD COLUMN `scheme` TEXT NOT NULL DEFAULT('http')")
                }
            }
        }
        private val MIGRATION_15_16 by lazy {
            object : Migration(15, 16) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE `posts_danbooru`")
                    database.execSQL("CREATE TABLE IF NOT EXISTS `posts_danbooru` (`indexInResponse` INTEGER NOT NULL, `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `scheme` TEXT NOT NULL, `host` TEXT NOT NULL, `keyword` TEXT NOT NULL, `id` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `uploader_id` INTEGER NOT NULL, `score` INTEGER NOT NULL, `source` TEXT NOT NULL, `md5` TEXT, `last_comment_bumped_at` TEXT, `rating` TEXT NOT NULL, `image_width` INTEGER NOT NULL, `image_height` INTEGER NOT NULL, `tag_string` TEXT NOT NULL, `is_note_locked` INTEGER NOT NULL, `fav_count` INTEGER NOT NULL, `file_ext` TEXT, `last_noted_at` TEXT, `is_rating_locked` INTEGER NOT NULL, `parent_id` INTEGER, `has_children` INTEGER NOT NULL, `approver_id` INTEGER, `tag_count_general` INTEGER NOT NULL, `tag_count_artist` INTEGER NOT NULL, `tag_count_character` INTEGER NOT NULL, `tag_count_copyright` INTEGER NOT NULL, `file_size` INTEGER NOT NULL, `is_status_locked` INTEGER NOT NULL, `pool_string` TEXT, `up_score` INTEGER NOT NULL, `down_score` INTEGER NOT NULL, `is_pending` INTEGER NOT NULL, `is_flagged` INTEGER NOT NULL, `is_deleted` INTEGER NOT NULL, `tag_count` INTEGER NOT NULL, `updated_at` TEXT, `is_banned` INTEGER NOT NULL, `pixiv_id` INTEGER NOT NULL, `last_commented_at` TEXT, `has_active_children` INTEGER NOT NULL, `bit_flags` INTEGER NOT NULL, `tag_count_meta` INTEGER NOT NULL, `uploader_name` TEXT NOT NULL, `has_large` INTEGER NOT NULL, `has_visible_children` INTEGER NOT NULL, `children_ids` TEXT, `is_favorited` INTEGER NOT NULL, `tag_string_general` TEXT, `tag_string_character` TEXT, `tag_string_copyright` TEXT, `tag_string_artist` TEXT, `tag_string_meta` TEXT, `file_url` TEXT, `large_file_url` TEXT, `preview_file_url` TEXT)")
                    database.execSQL("CREATE UNIQUE INDEX `index_posts_danbooru_host_keyword_id` ON `posts_danbooru` (`host`, `keyword`, `id`)")
                }
            }
        }
        private val MIGRATION_16_17 by lazy {
            object : Migration(16, 17) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `suggestions` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `booru_uid` INTEGER NOT NULL, `keyword` TEXT NOT NULL, FOREIGN KEY(`booru_uid`) REFERENCES `boorus`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    database.execSQL("CREATE UNIQUE INDEX `index_suggestions_booru_uid_keyword` ON `suggestions` (`booru_uid`, `keyword`)")
                }
            }
        }
        private val MIGRATION_17_18 by lazy {
            object : Migration(17, 18) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `posts_gelbooru` (`id` INTEGER NOT NULL, `height` INTEGER NOT NULL, `score` INTEGER NOT NULL, `file_url` TEXT NOT NULL, `parent_id` TEXT NOT NULL, `sample_url` TEXT NOT NULL, `sample_width` INTEGER NOT NULL, `sample_height` INTEGER NOT NULL, `preview_url` TEXT NOT NULL, `rating` TEXT NOT NULL, `tags` TEXT NOT NULL, `width` INTEGER NOT NULL, `change` INTEGER NOT NULL, `md5` TEXT NOT NULL, `creator_id` INTEGER NOT NULL, `has_children` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `status` TEXT NOT NULL, `source` TEXT NOT NULL, `has_notes` INTEGER NOT NULL, `has_comments` INTEGER NOT NULL, `preview_width` INTEGER NOT NULL, `preview_height` INTEGER NOT NULL, `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `indexInResponse` INTEGER NOT NULL, `scheme` TEXT NOT NULL, `host` TEXT NOT NULL, `keyword` TEXT NOT NULL)")
                    database.execSQL("CREATE UNIQUE INDEX `index_posts_gelbooru_host_keyword_id` ON `posts_gelbooru` (`host`, `keyword`, `id`)")
                }
            }
        }
        private val MIGRATION_18_19 by lazy {
            object : Migration(18, 19) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DROP TABLE `posts_gelbooru`")
                    database.execSQL("CREATE TABLE IF NOT EXISTS `posts_gelbooru` (`id` INTEGER NOT NULL, `height` INTEGER NOT NULL, `score` TEXT NOT NULL, `file_url` TEXT NOT NULL, `sample_url` TEXT NOT NULL, `sample_width` INTEGER NOT NULL, `sample_height` INTEGER NOT NULL, `preview_url` TEXT NOT NULL, `rating` TEXT NOT NULL, `tags` TEXT NOT NULL, `width` INTEGER NOT NULL, `change` INTEGER NOT NULL, `md5` TEXT NOT NULL, `creator_id` INTEGER NOT NULL, `has_children` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `status` TEXT NOT NULL, `source` TEXT NOT NULL, `has_notes` INTEGER NOT NULL, `has_comments` INTEGER NOT NULL, `preview_width` INTEGER NOT NULL, `preview_height` INTEGER NOT NULL, `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `indexInResponse` INTEGER NOT NULL, `scheme` TEXT NOT NULL, `host` TEXT NOT NULL, `keyword` TEXT NOT NULL)")
                    database.execSQL("CREATE UNIQUE INDEX `index_posts_gelbooru_host_keyword_id` ON `posts_gelbooru` (`host`, `keyword`, `id`)")
                }
            }
        }
        private val MIGRATION_19_20 by lazy {
            object : Migration(19, 20) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `posts_sankaku` (`author` TEXT NOT NULL, `change` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `fav_count` INTEGER NOT NULL, `file_size` INTEGER NOT NULL, `file_type` TEXT, `file_url` TEXT NOT NULL, `has_children` INTEGER NOT NULL, `has_comments` INTEGER NOT NULL, `has_notes` INTEGER NOT NULL, `height` INTEGER NOT NULL, `id` INTEGER NOT NULL, `in_visible_pool` INTEGER NOT NULL, `is_favorited` INTEGER NOT NULL, `is_premium` INTEGER NOT NULL, `md5` TEXT NOT NULL, `parent_id` INTEGER, `preview_height` INTEGER NOT NULL, `preview_url` TEXT NOT NULL, `preview_width` INTEGER NOT NULL, `rating` TEXT, `recommended_posts` INTEGER NOT NULL, `recommended_score` INTEGER NOT NULL, `sample_height` INTEGER NOT NULL, `sample_url` TEXT NOT NULL, `sample_width` INTEGER NOT NULL, `source` TEXT, `status` TEXT NOT NULL, `tags` TEXT NOT NULL, `total_score` INTEGER NOT NULL, `vote_count` INTEGER NOT NULL, `width` INTEGER NOT NULL, `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `indexInResponse` INTEGER NOT NULL, `scheme` TEXT NOT NULL, `host` TEXT NOT NULL, `keyword` TEXT NOT NULL)")
                    database.execSQL("CREATE UNIQUE INDEX `index_posts_sankaku_host_keyword_id` ON `posts_sankaku` (`host`, `keyword`, `id`)")
                }
            }
        }
        private val MIGRATION_20_21 by lazy {
            object : Migration(20, 21) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE  `users` ADD COLUMN `avatar_url` TEXT")
                }
            }
        }
        private val MIGRATION_21_22 by lazy {
            object : Migration(21, 22) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `cookies` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `booru_uid` INTEGER NOT NULL, `cookie` TEXT, FOREIGN KEY(`booru_uid`) REFERENCES `boorus`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    database.execSQL("CREATE UNIQUE INDEX `index_cookies_booru_uid` ON `cookies` (`booru_uid`)")
                }
            }
        }
        private val MIGRATION_22_23 by lazy {
            object : Migration(22, 23) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    val createSql = "CREATE TABLE IF NOT EXISTS `tag_blacklist` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `booru_uid` INTEGER NOT NULL, `tag` TEXT NOT NULL, FOREIGN KEY(`booru_uid`) REFERENCES `boorus`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                    database.execSQL(createSql)
                    database.execSQL("CREATE UNIQUE INDEX `index_tag_blacklist_booru_uid_tag` ON `tag_blacklist` (`booru_uid`, `tag`)")
                }
            }
        }
        @Volatile
        private var instance: FlexbooruDatabase? = null
        private val LOCK = Any()
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }
        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, FlexbooruDatabase::class.java, Constants.DB_FILE_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .addMigrations(
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13,
                    MIGRATION_13_14,
                    MIGRATION_14_15,
                    MIGRATION_15_16,
                    MIGRATION_16_17,
                    MIGRATION_17_18,
                    MIGRATION_18_19,
                    MIGRATION_19_20,
                    MIGRATION_20_21,
                    MIGRATION_21_22,
                    MIGRATION_22_23
                )
                .build()
    }

    abstract fun postDanOneDao(): PostDanOneDao
    abstract fun postDanDao(): PostDanDao
    abstract fun postMoeDao(): PostMoeDao
    abstract fun postGelDao(): PostGelDao
    abstract fun postSankakuDao(): PostSankakuDao

    abstract fun booruDao(): BooruDao
    abstract fun userDao(): UserDao

    abstract fun suggestionDao(): SuggestionDao

    abstract fun tagFilterDao(): TagFilterDao
    abstract fun muzeiDao(): MuzeiDao
    abstract fun cookieDao(): CookieDao
    abstract fun tagBlacklistDao(): TagBlacklistDao
}