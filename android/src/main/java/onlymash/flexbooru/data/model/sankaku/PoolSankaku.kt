/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.data.model.sankaku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.model.common.Pool
import onlymash.flexbooru.data.utils.toSafeUrl

@Serializable
data class PoolSankaku(
    @SerialName("artist_tags")
    val artistTags: List<TagSankaku>,
    @SerialName("author")
    val author: AuthorSankaku,
    @SerialName("cover_url")
    val coverUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("description")
    val description: String,
    @SerialName("fav_count")
    val favCount: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("is_favorited")
    val isFavorited: Boolean,
    @SerialName("is_public")
    val isPublic: Boolean,
    @SerialName("is_rating_locked")
    val isRatingLocked: Boolean,
    @SerialName("name")
    val name: String,
    @SerialName("post_count")
    val postCount: Int,
    @SerialName("rating")
    val rating: String,
    @SerialName("tags")
    val tags: List<TagSankaku>? = null,
    @SerialName("total_score")
    val totalScore: Int,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("visible_post_count")
    val visiblePostCount: Int,
    @SerialName("vote_count")
    val voteCount: Int
) {
    fun toPool(scheme: String, host: String): Pool {
        return Pool(
            booruType = BOORU_TYPE_SANKAKU,
            scheme = scheme,
            host = host,
            id = id,
            name = name,
            count = postCount,
            date = updatedAt,
            description = description,
            creatorId = author.id,
            creatorName = author.name,
            creatorAvatar = author.avatar?.toSafeUrl(scheme, host)
        )
    }
}