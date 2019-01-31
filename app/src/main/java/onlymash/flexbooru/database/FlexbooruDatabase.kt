package onlymash.flexbooru.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe

@Database(entities = [(PostMoe::class), (PostDan::class)], version = 1, exportSchema = false)
abstract class FlexbooruDatabase : RoomDatabase() {

    companion object {
        fun create(context: Context): FlexbooruDatabase {
            val databaseBuilder = Room.databaseBuilder(context, FlexbooruDatabase::class.java, "Flexbooru.db")
            return databaseBuilder.fallbackToDestructiveMigration().build()
        }
    }

    abstract fun postDanDao(): PostDanDao

    abstract fun postMoeDao(): PostMoeDao
}