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
import onlymash.flexbooru.Constants
import onlymash.flexbooru.database.FlexbooruDatabase
import java.util.concurrent.Executor

/**
 *Load posts from database
 */
class PostLoader(private val db: FlexbooruDatabase,
                 private val ioExecutor: Executor) : PostLoaderRepository {

    private val uiHandler = Handler()

    override var postLoadedListener: PostLoadedListener? = null

    override var postLoadedLiveDataListener: PostLoadedLiveDataListener? = null

    override fun loadPosts(host: String, keyword: String, type: Int) {
        when (type) {
            Constants.TYPE_DANBOORU -> loadDanPosts(host, keyword)
            Constants.TYPE_MOEBOORU -> loadMoePosts(host, keyword)
            Constants.TYPE_DANBOORU_ONE -> loadDanOnePosts(host, keyword)
            Constants.TYPE_GELBOORU -> loadGelPosts(host, keyword)
        }
    }

    override fun loadPostsLiveData(host: String, keyword: String, type: Int) {
        when (type) {
            Constants.TYPE_DANBOORU -> loadDanPostsLiveData(host, keyword)
            Constants.TYPE_MOEBOORU -> loadMoePostsLiveData(host, keyword)
            Constants.TYPE_DANBOORU_ONE -> loadDanOnePostsLiveData(host, keyword)
            Constants.TYPE_GELBOORU -> loadGelPostsLiveData(host, keyword)
        }
    }

    /**
     *Load danbooru posts from database
     */
    private fun loadDanPosts(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postDanDao().getPostsRaw(host, keyword)
            uiHandler.post {
                postLoadedListener?.onDanItemsLoaded(posts)
            }
        }
    }

    /**
     *Load danbooru1.x posts from database
     */
    private fun loadDanOnePosts(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postDanOneDao().getPostsRaw(host, keyword)
            uiHandler.post {
                postLoadedListener?.onDanOneItemsLoaded(posts)
            }
        }
    }

    /**
     *Load moebooru posts from database
     */
    private fun loadMoePosts(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postMoeDao().getPostsRaw(host, keyword)
            uiHandler.post {
                postLoadedListener?.onMoeItemsLoaded(posts)
            }
        }
    }

    private fun loadGelPosts(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postGelDao().getPostsRaw(host, keyword)
            uiHandler.post {
                postLoadedListener?.onGelItemsLoaded(posts)
            }
        }
    }

    private fun loadGelPostsLiveData(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postGelDao().getPostsLiveData(host, keyword)
            uiHandler.post {
                postLoadedLiveDataListener?.onGelItemsLoaded(posts)
            }
        }
    }

    /**
     *Load danbooru posts(livedata) from database
     */
    private fun loadDanPostsLiveData(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postDanDao().getPostsLiveData(host, keyword)
            uiHandler.post {
                postLoadedLiveDataListener?.onDanItemsLoaded(posts)
            }
        }
    }

    /**
     *Load danbooru1.x posts(livedata) from database
     */
    private fun loadDanOnePostsLiveData(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postDanOneDao().getPostsLiveData(host, keyword)
            uiHandler.post {
                postLoadedLiveDataListener?.onDanOneItemsLoaded(posts)
            }
        }
    }

    /**
     *Load moebooru posts(livedata) from database
     */
    private fun loadMoePostsLiveData(host: String, keyword: String) {
        ioExecutor.execute {
            val posts = db.postMoeDao().getPostsLiveData(host, keyword)
            uiHandler.post {
                postLoadedLiveDataListener?.onMoeItemsLoaded(posts)
            }
        }
    }
}