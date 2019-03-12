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
 * Comment request param
 * */
data class CommentAction(
    var scheme: String = "",
    var host: String = "",
    var limit: Int = 10,
    var body: String = "",
    var comment_id: Int = -1,
    var post_id: Int = -1,
    var query: String = "",
    //moebooru danbooru: do_not_bump_post Set to 1 if you do not want the post to be bumped to the top of the comment listing
    var anonymous: Int = 0,
    var username: String = "",
    var auth_key: String = ""
)