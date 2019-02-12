package onlymash.flexbooru.repository.post

import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.PostMoe
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.repository.Listing

interface PostRepository {

    fun getDanPosts(search: Search): Listing<PostDan>

    fun getMoePosts(search: Search): Listing<PostMoe>

}