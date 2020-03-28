package onlymash.flexbooru.data.repository.post

import kotlinx.coroutines.CoroutineScope
import onlymash.flexbooru.data.action.ActionPost
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.repository.Listing

interface PostRepository {

    fun getPosts(
        scope: CoroutineScope,
        action: ActionPost
    ): Listing<Post>
}