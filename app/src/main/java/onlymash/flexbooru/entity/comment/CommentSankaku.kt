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

package onlymash.flexbooru.entity.comment

data class CommentSankaku(
    val body: String,
    val created_at: String,
    val creator: String,
    val creator_avatar: String?,
    val creator_avatar_rating: String?,
    val creator_id: Int,
    val id: Int,
    val post_id: Int,
    val score: Int
) : CommentBase() {

    var scheme = "https"
    var host = ""

    fun getAvatarUrl() = checkUrl(creator_avatar ?: "")

    private fun checkUrl(url: String): String {
        var u = url
        if (u.contains("""\/""")) {
            u = u.replace("""\/""", "/")
        }
        return when {
            u.startsWith("http") -> u
            u.startsWith("//") -> "$scheme:$u"
            u.startsWith("/") -> "$scheme://$host$u"
            else -> u
        }
    }

    override fun getPostId(): Int = post_id

    override fun getCommentId(): Int = id

    override fun getCommentBody(): String = body

    override fun getCommentDate(): CharSequence = created_at

    override fun getCreatorId(): Int = creator_id

    override fun getCreatorName(): String = creator

}