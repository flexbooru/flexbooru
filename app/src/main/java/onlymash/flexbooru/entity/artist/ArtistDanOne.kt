/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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

data class ArtistDanOne(
    val id: Int,
    val name: String,
    val updater_id: Int,
    val version: Int,
    val is_active: Boolean,
    val urls: MutableList<String>?,
    val group_name: String?,
    val other_name: String?
) : BaseArtist() {
    override fun getArtistId(): Int = id
    override fun getArtistName(): String  = name
    override fun getArtistUrls(): MutableList<String> = urls ?: mutableListOf()
}