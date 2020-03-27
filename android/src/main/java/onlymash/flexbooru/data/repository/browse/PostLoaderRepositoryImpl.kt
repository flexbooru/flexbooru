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

package onlymash.flexbooru.data.repository.browse

import androidx.lifecycle.LiveData
import onlymash.flexbooru.data.database.dao.PostDao
import onlymash.flexbooru.data.model.common.Post

/**
 *Load posts from database
 */
class PostLoaderRepositoryImpl(private val postDao: PostDao) : PostLoaderRepository {

    override suspend fun loadPosts(booruUid: Long, query: String): List<Post> =
        postDao.getPostsRaw(booruUid, query)

    override suspend fun loadPostsLiveData(booruUid: Long, query: String): LiveData<List<Post>> =
        postDao.getPostsLiveData(booruUid, query)
}