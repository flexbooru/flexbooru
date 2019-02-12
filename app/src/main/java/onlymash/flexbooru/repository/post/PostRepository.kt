package onlymash.flexbooru.repository.post

import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.PostMoe
import onlymash.flexbooru.entity.SearchPost
import onlymash.flexbooru.repository.Listing

interface PostRepository {

    fun getDanPosts(search: SearchPost): Listing<PostDan>

    fun getMoePosts(search: SearchPost): Listing<PostMoe>

}