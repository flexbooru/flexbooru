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

package onlymash.flexbooru.entity.comment

/**
 * Danbooru response data class
 * */
data class CommentDan(
    val id: Int,
    val created_at: String,
    val post_id: Int,
    val creator_id: Int,
    val body: String,
    val score: Int,
    val updated_at: String,
    val updater_id: Int,
    val do_not_bump_post: Boolean,
    val is_deleted: Boolean,
    val is_sticky: Boolean,
    val creator_name: String,
    val updater_name: String
)