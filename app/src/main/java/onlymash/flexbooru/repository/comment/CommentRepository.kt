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

package onlymash.flexbooru.repository.comment

import androidx.lifecycle.MutableLiveData
import onlymash.flexbooru.entity.comment.*
import onlymash.flexbooru.repository.Listing

interface CommentRepository {
    val commentState: MutableLiveData<CommentState>

    fun getMoeComments(commentAction: CommentAction): Listing<CommentMoe>
    fun createMoeComment(commentAction: CommentAction)
    fun destroyMoeComment(commentAction: CommentAction)

    fun getDanComments(commentAction: CommentAction): Listing<CommentDan>
    fun createDanComment(commentAction: CommentAction)
    fun destroyDanComment(commentAction: CommentAction)

    fun getDanOneComments(commentAction: CommentAction): Listing<CommentDanOne>
    fun createDanOneComment(commentAction: CommentAction)
    fun destroyDanOneComment(commentAction: CommentAction)

    fun getGelComments(commentAction: CommentAction): Listing<CommentGel>
}