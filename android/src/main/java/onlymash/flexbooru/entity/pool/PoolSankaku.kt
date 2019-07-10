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

package onlymash.flexbooru.entity.pool

import com.google.gson.annotations.SerializedName
import onlymash.flexbooru.entity.common.SankakuAuthor
import onlymash.flexbooru.entity.common.SankakuTag

data class PoolSankaku(
    @SerializedName("artist_tags")
    val artistTags: List<SankakuTag>,
    @SerializedName("author")
    val author: SankakuAuthor,
    @SerializedName("cover_url")
    val coverUrl: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("description_en")
    val descriptionEn: String?,
    @SerializedName("description_ja")
    val descriptionJa: String?,
    @SerializedName("fav_count")
    val favCount: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("is_favorited")
    val isFavorited: Boolean,
    @SerializedName("is_public")
    val isPublic: Boolean,
    @SerializedName("is_rating_locked")
    val isRatingLocked: Boolean,
    @SerializedName("locale")
    val locale: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("name_en")
    val nameEn: String?,
    @SerializedName("name_ja")
    val nameJa: String?,
    @SerializedName("parent_id")
    val parentId: Int? = null,
    @SerializedName("post_count")
    val post_count: Int,
    @SerializedName("rating")
    val rating: String,
    @SerializedName("tags")
    val tags: List<SankakuTag>? = null,
    @SerializedName("total_score")
    val totalScore: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("visible_post_count")
    val visiblePostCount: Int,
    @SerializedName("vote_count")
    val voteCount: Int
) : PoolBase() {

    override fun getPoolId(): Int = id

    override fun getPoolName(): String = name

    override fun getPostCount(): Int = post_count

    override fun getPoolDate(): CharSequence = updatedAt

    override fun getPoolDescription(): String = description

    override fun getCreatorId(): Int = author.id

    override fun getCreatorName(): String? = author.name

}