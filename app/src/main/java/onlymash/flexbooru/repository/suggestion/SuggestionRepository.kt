package onlymash.flexbooru.repository.suggestion

import onlymash.flexbooru.entity.tag.TagBase
import onlymash.flexbooru.entity.tag.SearchTag

interface SuggestionRepository {
    fun fetchSuggestions(type: Int, search: SearchTag): MutableList<TagBase>?
}