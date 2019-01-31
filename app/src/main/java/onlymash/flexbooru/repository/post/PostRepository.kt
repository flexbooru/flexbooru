package onlymash.flexbooru.repository.post

import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.model.Search
import onlymash.flexbooru.repository.Listing

interface PostRepository {

    fun getDanbooruPosts(search: Search): Listing<PostDan>

    fun getMoebooruPosts(search: Search): Listing<PostMoe>

}