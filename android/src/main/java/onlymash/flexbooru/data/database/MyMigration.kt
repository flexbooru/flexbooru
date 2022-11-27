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

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class MyMigration(startVersion: Int, endVersion: Int) : Migration(startVersion, endVersion) {
    override fun migrate(database: SupportSQLiteDatabase) {
        when {
            startVersion == 1 && endVersion == 2 -> {
                database.execSQL("ALTER TABLE  `boorus` ADD COLUMN `path` TEXT")
            }
            startVersion == 2 && endVersion == 3 -> {
                database.execSQL("CREATE TABLE IF NOT EXISTS `history` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `booru_uid` INTEGER NOT NULL, `query` TEXT NOT NULL, FOREIGN KEY(`booru_uid`) REFERENCES `boorus`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_history_booru_uid_query` ON `history` (`booru_uid`, `query`)")
            }
            startVersion == 3 && endVersion == 4 -> {
                database.execSQL("DROP TABLE IF EXISTS `posts`")
                database.execSQL("CREATE TABLE IF NOT EXISTS `posts` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `booru_uid` INTEGER NOT NULL, `index` INTEGER NOT NULL, `query` TEXT NOT NULL, `id` INTEGER NOT NULL, `width` INTEGER NOT NULL, `height` INTEGER NOT NULL, `size` INTEGER NOT NULL, `score` INTEGER NOT NULL, `rating` TEXT NOT NULL, `is_favored` INTEGER NOT NULL, `time` INTEGER, `tags` TEXT NOT NULL, `preview` TEXT NOT NULL, `sample` TEXT NOT NULL, `medium` TEXT NOT NULL, `origin` TEXT NOT NULL, `pixiv_id` INTEGER, `source` TEXT, `uploader` TEXT NOT NULL, FOREIGN KEY(`booru_uid`) REFERENCES `boorus`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_posts_booru_uid_query_id` ON `posts` (`booru_uid`, `query`, `id`)")
            }
            startVersion == 4 && endVersion == 5 -> {
                database.execSQL("DROP TABLE IF EXISTS `cookies`")
            }
            startVersion == 5 && endVersion == 6 -> {
                database.execSQL("CREATE TABLE IF NOT EXISTS `next` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `booru_uid` INTEGER NOT NULL, `query` TEXT NOT NULL, `next` TEXT, FOREIGN KEY(`booru_uid`) REFERENCES `boorus`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_next_booru_uid_query` ON `next` (`booru_uid`, `query`)")
            }
            startVersion == 6 && endVersion == 7 -> {
                database.execSQL("ALTER TABLE `boorus` ADD COLUMN `auth` TEXT")
            }
        }
    }

    @DeleteTable(tableName = "cookies")
    class DeleteCookiesMigrationSpec : AutoMigrationSpec
}