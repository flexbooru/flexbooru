/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru.repository.suggestion

import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.api.*
import onlymash.flexbooru.api.url.*
import onlymash.flexbooru.entity.tag.TagBase
import onlymash.flexbooru.entity.tag.SearchTag
import java.io.IOException

class SuggestionRepositoryImpl(private val danbooruApi: DanbooruApi,
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