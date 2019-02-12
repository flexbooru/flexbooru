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
    (ArtistDan::class), (ArtistMoe::class)],
    version = 5, exportSchema = false)
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
}