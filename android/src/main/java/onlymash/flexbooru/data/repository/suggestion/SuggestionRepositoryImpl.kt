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

package onlymash.flexbooru.data.repository.suggestion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.action.ActionTag
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.model.common.Tag


class SuggestionRepositoryImpl(private val booruApis: BooruApis) : SuggestionRepository {

    override suspend fun fetchSuggestions(action: ActionTag): List<Tag>? {
        return when (action.booru.type) {
            BOORU_TYPE_DAN -> getDanTags(action)
            BOORU_TYPE_DAN1 -> getDan1Tags(action)
            BOORU_TYPE_MOE -> getMoeTags(action)
            BOORU_TYPE_GEL -> getGelTags(action)
            else -> getSankakuTags(action)
        }
    }

    private suspend fun getDanTags(action: ActionTag): List<Tag>? {
        return withContext(Dispatchers.IO) {
            try {
                booruApis.danApi.getTags(action.getDanTagsUrl(1)).body()?.map { it.toTag() }
            } catch (_: Exception) {
                null
            }
        }
    }

    private suspend fun getDan1Tags(action: ActionTag): List<Tag>? {
        return withContext(Dispatchers.IO) {
            try {
                booruApis.dan1Api.getTags(action.getDan1TagsUrl(1)).body()?.map { it.toTag() }
            } catch (_: Exception) {
                null
            }
        }
    }

    private suspend fun getMoeTags(action: ActionTag): List<Tag>? {
        return withContext(Dispatchers.IO) {
            try {
                booruApis.moeApi.getTags(action.getMoeTagsUrl(1)).body()?.map { it.toTag() }
            } catch (_: Exception) {
                null
            }
        }
    }

    private suspend fun getGelTags(action: ActionTag): List<Tag>? {
        return withContext(Dispatchers.IO) {
            try {
                booruApis.gelApi.getTags(action.getGelTagsUrl(0)).body()?.tags?.map { it.toTag() }
            } catch (_: Exception) {
                null
            }
        }
    }

    private suspend fun getSankakuTags(action: ActionTag): List<Tag>? {
        return withContext(Dispatchers.IO) {
            try {
                booruApis.sankakuApi.getTags(action.getSankakuTagsUrl(1)).body()?.map { it.toTag() }
            } catch (_: Exception) {
                null
            }
        }
    }
}