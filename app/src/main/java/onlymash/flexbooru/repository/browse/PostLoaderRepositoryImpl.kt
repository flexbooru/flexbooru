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

package onlymash.flexbooru.repository.browse

import androidx.lifecycle.LiveData
import onlymash.flexbooru.Constants
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.entity.post.PostBase

/**
 *Load posts from database
 */
class PostLoaderRepositoryImpl(private val db: FlexbooruDatabase) : PostLoaderRepository {

    @Suppress("UNCHECKED_CAST")
    override suspend fun loadPosts(host: String, keyword: String, type: Int): MutableList<PostBase> {
        return when (type) {
            Constants.TYPE_DANBOORU -> db.postDanDao().getPostsRaw(host, keyword) as MutableList<PostBase>
            Constants.TYPE_MOEBOORU -> db.postMoeDao().getPostsRaw(host, keyword) as MutableList<PostBase>
            Constants.TYPE_DANBOORU_ONE -> db.postDanOneDao().getPostsRaw(host, keyword) as MutableList<PostBase>
            Constants.TYPE_GELBOORU -> db.postGelDao().getPostsRaw(host, keyword) as MutableList<PostBase>
            else -> db.postSankakuDao().getPostsRaw(host, keyword) as MutableList<PostBase>
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun loadPostsLiveData(host: String, keyword: String, type: Int): LiveData<MutableList<PostBase>> {
        return when (type) {
            Constants.TYPE_DANBOORU -> db.postDanDao().getPostsLiveData(host, keyword) as LiveData<MutableList<PostBase>>
            Constants.TYPE_MOEBOORU -> db.postMoeDao().getPostsLiveData(host, keyword) as LiveData<MutableList<PostBase>>
            Constants.TYPE_DANBOORU_ONE -> db.postDanOneDao().getPostsLiveData(host, keyword) as LiveData<MutableList<PostBase>>
            Constants.TYPE_SANKAKU -> db.postSankakuDao().getPostsLiveData(host, keyword) as LiveData<MutableList<PostBase>>
            else -> db.postGelDao().getPostsLiveData(host, keyword) as LiveData<MutableList<PostBase>>
        }
    }
}