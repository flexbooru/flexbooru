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

package onlymash.flexbooru.entity.tag

/**
 * Search request tags param
 * */
data class SearchTag(
    var scheme: String,
    var host: String,
    var name: String,
    //count name date
    var order: String,
    // Moebooru General: 0, artist: 1, copyright: 3, character: 4, Circle: 5, Faults: 6
    // Danbooru General: 0, artist: 1, copyright: 3, character: 4, meta: 5
    // Danbooru1.x General: 0, artist: 1, copyright: 3, character: 4, model: 5, photo_set: 6
    var type: String,
    var limit: Int,
    var username: String = "",
    var auth_key: String = ""
)