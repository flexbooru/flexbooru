package onlymash.flexbooru.repository.popular

import onlymash.flexbooru.model.Popular
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.repository.Listing

interface PopularRepository {

    fun getDanPopular(popular: Popular): Listing<PostDan>

    fun getMoePopular(popular: Popular): Listing<PostMoe>
}