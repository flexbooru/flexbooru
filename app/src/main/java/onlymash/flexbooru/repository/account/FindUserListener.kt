package onlymash.flexbooru.repository.account

import onlymash.flexbooru.entity.User

interface FindUserListener {
    fun onSuccess(user: User)
    fun onFailed(msg: String)
}