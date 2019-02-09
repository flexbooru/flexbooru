package onlymash.flexbooru.repository.account

import android.os.Handler
import onlymash.flexbooru.Constants
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.getDanbooruUserUrl
import onlymash.flexbooru.api.getMoebooruUserUrl
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.model.User
import java.io.IOException
import java.util.concurrent.Executor

class UserFinder(private val danbooruApi: DanbooruApi,
                 private val moebooruApi: MoebooruApi,
                 private val ioExecutor: Executor) {

    private var findUserListener: FindUserListener? = null

    fun setFindUserListener(listener: FindUserListener) {
        findUserListener = listener
    }

    private val uiHandler = Handler()

    fun findUser(username: String, booru: Booru) {
        when (booru.type) {
            Constants.TYPE_DANBOORU -> findDanUser(username, booru)
            Constants.TYPE_MOEBOORU -> findMoeUser(username, booru)
        }
    }

    private fun findMoeUser(username: String, booru: Booru) {
        ioExecutor.execute {
            var msg = ""
            val request = moebooruApi.getUsers(getMoebooruUserUrl(username, booru))
            try {
                val response = request.execute()
                val data = response.body()
                val users = data?: mutableListOf()
                var user: User? = null
                users.forEach {
                    if (it.name == username) {
                        user = it
                        return@forEach
                    }
                }
                if (user != null) {
                    uiHandler.post {
                        findUserListener?.onSuccess(user!!)
                    }
                } else {
                    msg = "User not found!"
                }
            } catch (ioException: IOException) {
                msg = ioException.message ?: "unknown error"
            } finally {
                if (msg.isNotEmpty()) {
                    uiHandler.post {
                        findUserListener?.onFailed(msg)
                    }
                }
            }
        }
    }

    private fun findDanUser(username: String, booru: Booru) {
        ioExecutor.execute {
            var msg = ""
            val request = danbooruApi.getUsers(getDanbooruUserUrl(username, booru))
            try {
                val response = request.execute()
                val data = response.body()
                val users = data?: mutableListOf()
                var user: User? = null
                users.forEach {
                    if (it.name == username) {
                        user = it
                        return@forEach
                    }
                }
                if (user != null) {
                    uiHandler.post {
                        findUserListener?.onSuccess(user!!)
                    }
                } else {
                    msg = "User not found!"
                }
            } catch (ioException: IOException) {
                msg = ioException.message ?: "unknown error"
            } finally {
                if (msg.isNotEmpty()) {
                    uiHandler.post {
                        findUserListener?.onFailed(msg)
                    }
                }
            }
        }
    }
}