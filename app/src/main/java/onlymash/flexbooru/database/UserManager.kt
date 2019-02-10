package onlymash.flexbooru.database

import android.database.sqlite.SQLiteCantOpenDatabaseException
import onlymash.flexbooru.model.User
import java.io.IOException
import java.sql.SQLException

object UserManager {

    interface Listener {
        fun onAdd(user: User)
        fun onDelete(user: User)
        fun onUpdate(user: User)
    }

    var listeners: MutableList<Listener> = mutableListOf()

    @Throws(SQLException::class)
    fun createUser(user: User): User {
        user.uid = 0
        user.uid = FlexbooruDatabase.userDao.insert(user)
        if (user.uid >= 0) {
            listeners.forEach {
                it.onAdd(user)
            }
        }
        return user
    }

    @Throws(SQLException::class)
    fun updateUser(user: User): Boolean {
        val result = FlexbooruDatabase.userDao.update(user) == 1
        if (result) {
            listeners.forEach {
                it.onUpdate(user)
            }
        }
        return result
    }

    @Throws(IOException::class)
    fun getUserByBooruUid(booruUid: Long): User? = try {
        FlexbooruDatabase.userDao.getUserByBooruUid(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }

    @Throws(IOException::class)
    fun getUserByUserUid(uid: Long): User? = try {
        FlexbooruDatabase.userDao.getUserByUserUid(uid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }

    @Throws(SQLException::class)
    fun deleteUser(user: User) {
        if (FlexbooruDatabase.userDao.delete(user.uid) == 1) {
            listeners.forEach {
                it.onDelete(user)
            }
        }
    }

    @Throws(SQLException::class)
    fun deleteAll() = FlexbooruDatabase.userDao.deleteAll()

    @Throws(IOException::class)
    fun getAllUsers(): MutableList<User>? = try {
        FlexbooruDatabase.userDao.getAll()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }
}