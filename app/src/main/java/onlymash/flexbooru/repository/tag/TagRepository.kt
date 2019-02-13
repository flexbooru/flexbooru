package onlymash.flexbooru.repository.tag

import onlymash.flexbooru.entity.SearchTag
import onlymash.flexbooru.entity.TagDan
import onlymash.flexbooru.entity.TagMoe
import onlymash.flexbooru.repository.Listing

interface TagRepository {
    fun getDanTags(search: SearchTag): Listing<TagDan>
    fun getMoeTags(search: SearchTag): Listing<TagMoe>
}