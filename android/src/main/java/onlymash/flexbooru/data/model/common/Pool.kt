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

package onlymash.flexbooru.data.model.common

data class Pool(
    val booruType: Int = -1,
    val scheme: String = "https",
    val host: String,
    val id: Int,
    val name: String,
    val count: Int,
    val date: CharSequence,
    val description: String,
    val creatorId: Int = -1,
    val creatorName: String? = null,
    val creatorAvatar: String? = null
)