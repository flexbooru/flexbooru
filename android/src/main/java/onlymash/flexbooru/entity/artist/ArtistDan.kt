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

package onlymash.flexbooru.entity.artist

import com.google.gson.annotations.SerializedName

data class ArtistDan(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("creator_id")
    val creatorId: Int,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("group_name")
    val groupName: String,
    @SerializedName("is_banned")
    val isBanned: Boolean,
    @SerializedName("urls")
    val urls: MutableList<ArtistUrlDan>?
) : ArtistBase() {
    override fun getArtistId(): Int = id
    override fun getArtistName(): String  = name
    override fun getArtistUrls(): MutableList<String> {
        val list: MutableList<String> = mutableListOf()
        urls?.forEach {
            list.add(it.url)
        }
        return list
    }
}

data class ArtistUrlDan(
    @SerializedName("id")
    val id: Int,
    @SerializedName("artist_id")
    val artistId: Int,
    @SerializedName("url")
    val url: String,
    @SerializedName("normalized_url")
    val normalizedUrl: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("is_active")
    val isActive: Boolean
)