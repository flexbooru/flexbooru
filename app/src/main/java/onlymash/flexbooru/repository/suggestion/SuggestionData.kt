package onlymash.flexbooru.repository.suggestion

import onlymash.flexbooru.Constants
import onlymash.flexbooru.api.*
import onlymash.flexbooru.api.url.*
import onlymash.flexbooru.entity.tag.TagBase
import onlymash.flexbooru.entity.tag.SearchTag
import java.io.IOException

class SuggestionData(private val danbooruApi: DanbooruApi,
                     private val moebooruApi: MoebooruApi,
                     private val danbooruOneApi: DanbooruOneApi,
                     private val gelbooruApi: GelbooruApi,
                     private val sankakuApi: SankakuApi) : SuggestionRepository {

    override fun fetchSuggestions(type: Int, search: SearchTag): MutableList<TagBase>? =
        when (type) {
            Constants.TYPE_DANBOORU -> fetchDanTags(search)
            Constants.TYPE_MOEBOORU -> fetchMoeTags(search)
            Constants.TYPE_DANBOORU_ONE -> fetchDanOneTags(search)
            Constants.TYPE_GELBOORU -> fetchGelTags(search)
            Constants.TYPE_SANKAKU -> fetchSankakuTags(search)
            else -> null
        }

    @Suppress("UNCHECKED_CAST")
    private fun fetchDanTags(search: SearchTag): MutableList<TagBase>? = try {
        danbooruApi.getTags(DanUrlHelper.getTagUrl(search, 1))
            .execute().body() as? MutableList<TagBase>
    } catch (_: IOException) {
        null
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchMoeTags(search: SearchTag): MutableList<TagBase>? = try {
        moebooruApi.getTags(MoeUrlHelper.getTagUrl(search, 1))
            .execute().body() as? MutableList<TagBase>
    } catch (_: IOException) {
        null
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchDanOneTags(search: SearchTag): MutableList<TagBase>? = try {
        danbooruOneApi.getTags(DanOneUrlHelper.getTagUrl(search, 1))
            .execute().body() as? MutableList<TagBase>
    } catch (_: IOException) {
        null
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchGelTags(search: SearchTag): MutableList<TagBase>? = try {
        gelbooruApi.getTags(GelUrlHelper.getTagUrl(search, 1))
            .execute().body()?.tags as? MutableList<TagBase>
    } catch (_: IOException) {
        null
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchSankakuTags(search: SearchTag): MutableList<TagBase>? = try {
        sankakuApi.getTags(SankakuUrlHelper.getTagUrl(search, 1))
            .execute().body() as? MutableList<TagBase>
    } catch (_: IOException) {
        null
    }
}