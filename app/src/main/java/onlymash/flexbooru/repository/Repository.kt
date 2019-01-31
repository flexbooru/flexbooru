package onlymash.flexbooru.repository

import okhttp3.HttpUrl
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.model.Search

interface Repository {

    fun getDanbooruPosts(search: Search): Listing<PostDan>

    fun getMoebooruPosts(search: Search): Listing<PostMoe>

}