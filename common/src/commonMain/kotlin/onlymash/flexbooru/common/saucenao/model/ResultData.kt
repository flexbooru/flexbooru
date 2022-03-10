/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

package onlymash.flexbooru.common.saucenao.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResultData(
    
    @SerialName("ext_urls")
    var extUrls: List<String>? = null,
    
    @SerialName("title")
    var title: String? = null,

    @SerialName("pixiv_id")
    var pixivId: Int? = null,
    @SerialName("seiga_id")
    var seigaId: Int? = null,
    @SerialName("member_name")
    var memberName: String? = null,
    @SerialName("member_id")
    var memberId: Int? = null,

    @SerialName("anidb_aid")
    var anidbAid: Int? = null,

    @SerialName("danbooru_id")
    var danbooruId: Int? = null,
    @SerialName("gelbooru_id")
    var gelbooruId: Int? = null,
    @SerialName("sankaku_id")
    var sankakuId: Int? = null,
    @SerialName("characters")
    var characters: String? = null,

    @SerialName("material")
    var material: String? = null,

    @SerialName("source")
    var source: String? = null,
    @SerialName("year")
    var year: String? = null,
    @SerialName("part")
    var part: String? = null,

    @SerialName("da_id")
    var daId: Int? = null,

    @SerialName("author_name")
    var author_name: String? = null,
    @SerialName("author_url")
    var author_url: String? = null,

    @SerialName("est_time")
    var estTime: String? = null,

    @SerialName("eng_name")
    var engName: String? = null,

    @SerialName("jp_name")
    var jpName: String? = null
)