package onlymash.flexbooru.repository.account

import onlymash.flexbooru.model.User

interface FindUserListener {
    fun onSuccess(user: User)
    fun onFailed(msg: String)
}