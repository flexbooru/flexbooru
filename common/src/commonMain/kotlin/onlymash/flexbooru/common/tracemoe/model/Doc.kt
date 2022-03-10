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

package onlymash.flexbooru.common.tracemoe.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Doc(
    @SerialName("anilist_id")
    val anilistId: Int,
    @SerialName("anime")
    var anime: String? = null,
//    @SerialName("episode")
//    val episode: String,
    @SerialName("filename")
    var filename: String? = null,
    @SerialName("from")
    val from: Float,
    @SerialName("to")
    val to: Float,
    @SerialName("at")
    val at: Float,
    @SerialName("is_adult")
    var isAdult: Boolean = false,
    @SerialName("mal_id")
    var malId: Int? = null,
    @SerialName("season")
    var season: String? = null,
    @SerialName("similarity")
    val similarity: Double,
    @SerialName("synonyms")
    var synonyms: List<String> = emptyList(),
    @SerialName("synonyms_chinese")
    var synonymsChinese: List<String> = emptyList(),
    @SerialName("title")
    var title: String? = null,
    @SerialName("title_chinese")
    var titleChinese: String? = null,
    @SerialName("title_english")
    var titleEnglish: String? = null,
    @SerialName("title_native")
    var titleNative: String? = null,
    @SerialName("title_romaji")
    var titleRomaji: String? = null,
    @SerialName("tokenthumb")
    var tokenthumb: String? = null
)