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

package onlymash.flexbooru.data.model.danbooru
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.utils.getDanDateMillis

@Serializable
data class PostDanE621(
    @SerialName("created_at")
    val createdAt: String?,
    @SerialName("description")
    val description: String,
    @SerialName("fav_count")
    val favCount: Int,
    @SerialName("file")
    val file: FileInfo,
    @SerialName("id")
    val id: Int,
    @SerialName("is_favorited")
    val isFavorited: Boolean,
    @SerialName("preview")
    val preview: FileInfo,
    @SerialName("rating")
    val rating: String,
    @SerialName("sample")
    val sample: FileInfo,
    @SerialName("score")
    val score: Score,
    @SerialName("sources")
    val sources: List<String>,
    @SerialName("tags")
    val tags: Tags,
    @SerialName("updated_at")
    val updatedAt: String?,
    @SerialName("uploader_id")
    val uploaderId: Int
) {
    @Serializable
    data class FileInfo(
        @SerialName("url")
        val url: String?,
        @SerialName("width")
        val width: Int,
        @SerialName("height")
        val height: Int,
        @SerialName("size")
        val size: Int = 0,
        @SerialName("ext")
        val ext: String? = null
    )

    @Serializable
    data class Score(
        @SerialName("down")
        val down: Int,
        @SerialName("total")
        val total: Int,
        @SerialName("up")
        val up: Int
    )

    @Serializable
    data class Tags(
        @SerialName("artist")
        val artist: List<String>,
        @SerialName("character")
        val character: List<String>,
        @SerialName("copyright")
        val copyright: List<String>,
        @SerialName("general")
        val general: List<String>,
        @SerialName("invalid")
        val invalid: List<String>,
        @SerialName("lore")
        val lore: List<String>,
        @SerialName("meta")
        val meta: List<String>,
        @SerialName("species")
        val species: List<String>
    )

    fun toPost(booruUid: Long, query: String, index: Int) = Post(
        booruUid = booruUid,
        query = query,
        index = index,
        id = id,
        width = file.width,
        height = file.height,
        size = file.size,
        score = score.total,
        rating = rating,
        isFavored = isFavorited,
        time = createdAt?.getDanDateMillis(),
        tags = getTags(),
        preview = preview.url ?: "",
        sample = sample.url ?: "",
        medium = sample.url ?: "",
        origin = file.url ?: "",
        pixivId = -1,
        source = sources.toString(),
        uploader = User(id = uploaderId, name = "null")
    )

    private fun getTags(): List<TagBase> {
        val tagsGeneral = tags.general.map { TagBase(name = it, category = 0) }
        val tagsCharacter = tags.character.map { TagBase(name = it, category = 1) }
        val tagsCopyright = tags.copyright.map { TagBase(name = it, category = 2) }
        val tagsMeta = tags.meta.map { TagBase(name = it, category = 3) }
        val tagsArtist = tags.artist.map { TagBase(name = it, category = 4) }
        val tagsLore = tags.lore.map { TagBase(name = it, category = 5) }
        val tagsSpecies = tags.species.map { TagBase(name = it, category = 6) }
        val tagsInvalid = tags.invalid.map { TagBase(name = it, category = 7) }
        return tagsArtist + tagsMeta + tagsCopyright + tagsCharacter +
                tagsGeneral + tagsLore + tagsSpecies + tagsInvalid
    }
}
