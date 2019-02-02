package onlymash.flexbooru.database

import android.database.sqlite.SQLiteCantOpenDatabaseException
import onlymash.flexbooru.model.Booru
import java.io.IOException
import java.sql.SQLException

object BooruManager {
    interface Listener {
        fun onAdd(booru: Booru)
        fun onRemove(booruUid: Long)
    }
    var listener: Listener? = null

    @Throws(SQLException::class)
    fun createBooru(booru: Booru): Booru {
        booru.uid = 0
        booru.uid = FlexbooruDatabase.booruDao.insert(booru)
        listener?.onAdd(booru)
        return booru
    }

    @Throws(SQLException::class)
    fun updateBooru(booru: Booru) = check(FlexbooruDatabase.booruDao.update(booru) == 1)

    @Throws(IOException::class)
    fun getBooru(scheme: String, host: String): Booru? = try {
        FlexbooruDatabase.booruDao[scheme, host]
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }

    @Throws(SQLException::class)
    fun deleteBooru(uid: Long) {
        check(FlexbooruDatabase.booruDao.delete(uid) == 1)
        listener?.onRemove(uid)
    }

    @Throws(SQLException::class)
    fun deleteAll() = FlexbooruDatabase.booruDao.deleteAll()


    @Throws(IOException::class)
    fun isNotEmpty(): Boolean = try {
        FlexbooruDatabase.booruDao.isNotEmpty()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        false
    }

    @Throws(IOException::class)
    fun getAllBoorus(): MutableList<Booru>? = try {
        FlexbooruDatabase.booruDao.getAll()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }
}