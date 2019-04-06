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

package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.repository.browse.PostLoadedLiveDataListener
import onlymash.flexbooru.repository.browse.PostLoaderRepository

class FavPostViewModel(private val postLoader: PostLoaderRepository) : ViewModel() {

    val postsDan: MediatorLiveData<MutableList<PostDan>> = MediatorLiveData()

    val postsDanOne: MediatorLiveData<MutableList<PostDanOne>> = MediatorLiveData()

    val postsMoe: MediatorLiveData<MutableList<PostMoe>> = MediatorLiveData()

    val postsGel: MediatorLiveData<MutableList<PostGel>> = MediatorLiveData()

    val postsSankaku: MediatorLiveData<MutableList<PostSankaku>> = MediatorLiveData()

    private val postLoadedLiveDataListener = object : PostLoadedLiveDataListener {
        override fun onDanOneItemsLoaded(posts: LiveData<MutableList<PostDanOne>>) {
            postsDanOne.addSource(posts) {
                postsDanOne.postValue(it)
            }
        }
        override fun onDanItemsLoaded(posts: LiveData<MutableList<PostDan>>) {
            postsDan.addSource(posts) {
                postsDan.postValue(it)
            }
        }
        override fun onMoeItemsLoaded(posts: LiveData<MutableList<PostMoe>>) {
            postsMoe.addSource(posts) {
                postsMoe.postValue(it)
            }
        }
        override fun onGelItemsLoaded(posts: LiveData<MutableList<PostGel>>) {
            postsGel.addSource(posts) {
                postsGel.postValue(it)
            }
        }

        override fun onSankakuItemsLoaded(posts: LiveData<MutableList<PostSankaku>>) {
            postsSankaku.addSource(posts) {
                postsSankaku.postValue(it)
            }
        }
    }

    init {
        postLoader.postLoadedLiveDataListener = postLoadedLiveDataListener
    }

    fun loadFav(host: String, keyword: String, type: Int) {
        postLoader.loadPostsLiveData(host, keyword, type)
    }
}