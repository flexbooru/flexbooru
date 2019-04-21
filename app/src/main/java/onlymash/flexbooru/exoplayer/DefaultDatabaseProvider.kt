package onlymash.flexbooru.exoplayer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.android.exoplayer2.database.DatabaseProvider
import onlymash.flexbooru.App

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