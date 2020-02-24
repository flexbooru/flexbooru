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

package onlymash.flexbooru.entity.tag

import com.google.gson.annotations.SerializedName

data class TagDan(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("post_count")
    val postCount: Int,
    @SerializedName("related_tags")
    val relatedTags: String,
    @SerializedName("related_tags_updated_at")
    val relatedTagsUpdatedAt: String,
    @SerializedName("category")
    val category: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("is_locked")
    val isLocked: Boolean
) : TagBase() {
    override fun getTagId(): Int = id
    override fun getTagName(): String = name
}