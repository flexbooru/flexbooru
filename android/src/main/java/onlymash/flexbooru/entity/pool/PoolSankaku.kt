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

import onlymash.flexbooru.entity.SankakuAuthor
import onlymash.flexbooru.entity.SankakuTag

data class PoolSankaku(
    val artist_tags: List<SankakuTag>,
    val author: SankakuAuthor,
    val cover_url: String,
    val created_at: String,
    val description: String,
    val description_en: String?,
    val description_ja: String?,
    val fav_count: Int,
    val id: Int,
    val is_active: Boolean,
    val is_favorited: Boolean,
    val is_public: Boolean,
    val is_rating_locked: Boolean,
    val locale: String,
    val name: String,
    val name_en: String?,
    val name_ja: String?,
    val parent_id: Int?,
    val post_count: Int,
    val rating: String,
    val tags: List<SankakuTag>?,
    val total_score: Int,
    val updated_at: String,
    val visible_post_count: Int,
    val vote_count: Int
) : PoolBase() {

    override fun getPoolId(): Int = id

    override fun getPoolName(): String = name

    override fun getPostCount(): Int = post_count

    override fun getPoolDate(): CharSequence = updated_at

    override fun getPoolDescription(): String = description

    override fun getCreatorId(): Int = author.id

    override fun getCreatorName(): String? = author.name

}