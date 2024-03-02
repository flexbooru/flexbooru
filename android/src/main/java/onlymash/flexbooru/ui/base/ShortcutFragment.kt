/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

package onlymash.flexbooru.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import onlymash.flexbooru.app.Keys
import onlymash.flexbooru.data.database.dao.PostDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.ui.viewmodel.ShortcutViewModel
import onlymash.flexbooru.ui.viewmodel.getShortcutViewModel
import org.koin.android.ext.android.inject

abstract class ShortcutFragment<T: ViewBinding> : BooruFragment<T>() {

    private val postDao by inject<PostDao>()

    private var postId = -1

    private lateinit var shortcutViewModel: ShortcutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            postId = getInt(Keys.POST_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        shortcutViewModel = getShortcutViewModel(postDao)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onBooruLoaded(booru: Booru?) {
        if (booru == null) {
            return
        }
        shortcutViewModel.loadPost(booru.uid, postId).observe(viewLifecycleOwner) {
            onPostLoaded(it)
        }
    }

    abstract fun onPostLoaded(post: Post?)
}