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

package onlymash.flexbooru.ui.viewmodel

import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.extension.ioMain
import onlymash.flexbooru.repository.browse.PostLoaderRepository

class FavPostViewModel(private val postLoader: PostLoaderRepository) : ViewModel() {

    private val posts = MediatorLiveData<MutableList<PostBase>>()

    @UiThread
    fun load(host: String, keyword: String, type: Int): LiveData<MutableList<PostBase>> {
        ioMain({
            postLoader.loadPostsLiveData(host, keyword, type)
        }) { data ->
            posts.addSource(data) {
                posts.postValue(it)
            }
        }
        return posts
    }
}