package onlymash.flexbooru.repository.popular

import onlymash.flexbooru.entity.SearchPopular
import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.PostMoe
import onlymash.flexbooru.repository.Listing

interface PopularRepository {

    fun getDanPopular(popular: SearchPopular): Listing<PostDan>

    fun getMoePopular(popular: SearchPopular): Listing<PostMoe>
}