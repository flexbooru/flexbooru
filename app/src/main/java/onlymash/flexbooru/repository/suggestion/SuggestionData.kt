package onlymash.flexbooru.repository.suggestion

import onlymash.flexbooru.Constants
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.GelbooruApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.url.DanOneUrlHelper
import onlymash.flexbooru.api.url.DanUrlHelper
import onlymash.flexbooru.api.url.GelUrlHelper
import onlymash.flexbooru.api.url.MoeUrlHelper
import onlymash.flexbooru.entity.tag.BaseTag
import onlymash.flexbooru.entity.tag.SearchTag
import java.io.IOException

class SuggestionData(private val danbooruApi: DanbooruApi,
                     private val moebooruApi: MoebooruApi,
                     private val danbooruOneApi: DanbooruOneApi,
                     private val gelbooruApi: GelbooruApi) : SuggestionRepository {

    override fun fetchSuggestions(type: Int, search: SearchTag): MutableList<BaseTag>? =
        when (type) {
            Constants.TYPE_DANBOORU -> fetchDanTags(search)
            Constants.TYPE_MOEBOORU -> fetchMoeTags(search)
            Constants.TYPE_DANBOORU_ONE -> fetchDanOneTags(search)
            Constants.TYPE_GELBOORU -> fetchGelTags(search)
            else -> null
        }

    @Suppress("UNCHECKED_CAST")
    private fun fetchDanTags(search: SearchTag): MutableList<BaseTag>? = try {
        danbooruApi.getTags(DanUrlHelper.getTagUrl(search, 1))
            .execute().body() as? MutableList<BaseTag>
    } catch (_: IOException) {
        null
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchMoeTags(search: SearchTag): MutableList<BaseTag>? = try {
        moebooruApi.getTags(MoeUrlHelper.getTagUrl(search, 1))
            .execute().body() as? MutableList<BaseTag>
    } catch (_: IOException) {
        null
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchDanOneTags(search: SearchTag): MutableList<BaseTag>? = try {
        danbooruOneApi.getTags(DanOneUrlHelper.getTagUrl(search, 1))
            .execute().body() as? MutableList<BaseTag>
    } catch (_: IOException) {
        null
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchGelTags(search: SearchTag): MutableList<BaseTag>? = try {
        gelbooruApi.getTags(GelUrlHelper.getTagUrl(search, 1))
            .execute().body()?.tags as? MutableList<BaseTag>
    } catch (_: IOException) {
        null
    }
}