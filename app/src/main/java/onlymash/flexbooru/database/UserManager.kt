package onlymash.flexbooru.database

import android.database.sqlite.SQLiteCantOpenDatabaseException
import onlymash.flexbooru.model.User
import java.io.IOException
import java.sql.SQLException

object UserManager {

    interface Listener {
        fun onAdd(user: User)
        fun onDelete(uid: Long)
        fun onUpdate(user: User)
    }

    var listeners: MutableList<Listener> = mutableListOf()

    @Throws(SQLException::class)
    fun createUser(user: User): User {
        user.uid = 0
        user.uid = FlexbooruDatabase.userDao.insert(user)
        return user
    }

    @Throws(SQLException::class)
    fun updateUser(user: User): Boolean = FlexbooruDatabase.userDao.update(user) == 1

    @Throws(IOException::class)
    fun getUser(booruUid: Long): User? = try {
        FlexbooruDatabase.userDao.getUser(booruUid)
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        ex.printStackTrace()
        null
    }

    @Throws(SQLException::class)
    fun deleteUser(uid: Long) = FlexbooruDatabase.userDao.delete(uid)

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