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

package onlymash.flexbooru.exoplayer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.android.exoplayer2.database.DatabaseProvider
import onlymash.flexbooru.common.App

class DefaultDatabaseProvider(context: Context) : SQLiteOpenHelper(context, "ExoPlayer", null, 1), DatabaseProvider {

    companion object {
        private var databaseProvider: DatabaseProvider? = null
        fun databaseProvider(): DatabaseProvider {
            if (databaseProvider == null) {
                databaseProvider = DefaultDatabaseProvider(App.app)
            }
            return databaseProvider!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    @Synchronized
    override fun close() {
        super.close()
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        return super.getReadableDatabase()
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        return super.getWritableDatabase()
    }
}