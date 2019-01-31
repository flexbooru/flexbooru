package onlymash.flexbooru.repository.popular

import onlymash.flexbooru.model.Popular
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.repository.Listing

interface PopularRepository {

    fun getDanbooruPopular(popular: Popular): Listing<PostDan>

    fun getMoebooruPopular(popular: Popular): Listing<PostMoe>
}