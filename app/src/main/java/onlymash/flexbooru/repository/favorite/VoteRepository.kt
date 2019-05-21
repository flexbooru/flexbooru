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

package onlymash.flexbooru.repository.favorite

import onlymash.flexbooru.entity.post.PostDan
import onlymash.flexbooru.entity.post.PostDanOne
import onlymash.flexbooru.entity.Vote
import onlymash.flexbooru.entity.VoteDan
import onlymash.flexbooru.entity.VoteMoe
import onlymash.flexbooru.entity.VoteSankaku
import onlymash.flexbooru.entity.post.PostGel
import onlymash.flexbooru.entity.post.PostSankaku
import onlymash.flexbooru.extension.NetResult

interface VoteRepository {
    suspend fun voteMoePost(vote: Vote): NetResult<VoteMoe>
    suspend fun addDanFav(vote: Vote, post: PostDan): NetResult<VoteDan>
    suspend fun removeDanFav(vote: Vote, postFav: PostDan): NetResult<VoteDan>
    suspend fun addDanOneFav(vote: Vote, post: PostDanOne): NetResult<VoteDan>
    suspend fun removeDanOneFav(vote: Vote, postFav: PostDanOne): NetResult<VoteDan>
    suspend fun addSankakuFav(vote: Vote, post: PostSankaku): NetResult<VoteSankaku>
    suspend fun removeSankakuFav(vote: Vote, postFav: PostSankaku): NetResult<VoteSankaku>
    suspend fun addGelFav(vote: Vote, post: PostGel): NetResult<Boolean>
    suspend fun removeGelFav(vote: Vote, postFav: PostGel): NetResult<Boolean>
}