package onlymash.flexbooru.database

import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.util.Log
import onlymash.flexbooru.model.Booru
import java.io.IOException
import java.sql.SQLException

object BooruManager {

    interface Listener {
        fun onAdd(booru: Booru)
        fun onDelete(booruUid: Long)
        fun onUpdate(booru: Booru)
    }

    var listeners: MutableList<Listener> = mutableListOf()

    @Throws(SQLException::class)
    fun createBooru(booru: Booru): Booru {
        booru.uid = 0
        val uid = FlexbooruDatabase.booruDao.insert(booru)
        if (uid >= 0) {
            booru.uid = uid
            listeners.forEach {
                it.onAdd(booru)
            }
        }
        return booru
    }

    @Throws(SQLException::class)
    fun updateBooru(booru: Booru): Boolean {
        val result = FlexbooruDatabase.booruDao.update(booru) == 1
        if (result) {
            listeners.forEach {
                it.onUpdate(booru)
            }
        }
        return result
    }

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
        if (FlexbooruDatabase.booruDao.delete(uid) == 1) {
            listeners.forEach {
                it.onDelete(uid)
            }
        }
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