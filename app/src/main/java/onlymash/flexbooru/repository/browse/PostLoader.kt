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

package onlymash.flexbooru.repository.browse

import android.os.Handler
import onlymash.flexbooru.database.FlexbooruDatabase
import java.util.concurrent.Executor

/**
 *Load posts from database
 */
class PostLoader(private val db: FlexbooruDatabase,
                 private val ioExecutor: Executor) {

    private var postLoadedListener: PostLoadedListener? = null

    //set post loader callback
    fun setPostLoadedListener(listener: PostLoadedListener?) {
        postLoadedListener = listener
    }

    private var postLoadedLiveDataListener: PostLoadedLiveDataListener? = null

    //set post loader(livedata) callback
    fun setPostLoadedLiveDataListener(listener: PostLoadedLiveDataListener?) {
        postLoadedLiveDataListener = listener
    }

    private val uiHandler = Handler()

    /**
     *Load danbooru posts from database
     */
    fun loadDanPosts(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postDanDao().getPostsRaw(host, keyword)
            uiHandler.post {
                postLoadedListener?.onDanItemsLoaded(posts)
            }
        }
    }

    /**
     *Load moebooru posts from database
     */
    fun loadMoePosts(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postMoeDao().getPostsRaw(host, keyword)
            uiHandler.post {
                postLoadedListener?.onMoeItemsLoaded(posts)
            }
        }
    }

    /**
     *Load danbooru posts(livedata) from database
     */
    fun loadDanPostsLiveData(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postDanDao().getPostsLiveData(host, keyword)
            uiHandler.post {
                postLoadedLiveDataListener?.onDanItemsLoaded(posts)
            }
        }
    }

    /**
     *Load moebooru posts(livedata) from database
     */
    fun loadMoePostsLiveData(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postMoeDao().getPostsLiveData(host, keyword)
            uiHandler.post {
                postLoadedLiveDataListener?.onMoeItemsLoaded(posts)
            }
        }
    }
}