package onlymash.flexbooru.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import onlymash.flexbooru.App.Companion.app
import onlymash.flexbooru.Constants
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe

@Database(entities = [(PostMoe::class), (PostDan::class), (Booru::class)], version = 2, exportSchema = false)
abstract class FlexbooruDatabase : RoomDatabase() {

    companion object {
        val instance by lazy {
            Room.databaseBuilder(app, FlexbooruDatabase::class.java, Constants.DB_FILE_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        }
        val booruDao get() = instance.booruDao()
    }

    abstract fun postDanDao(): PostDanDao

    abstract fun postMoeDao(): PostMoeDao

    abstract fun booruDao(): BooruDao
}