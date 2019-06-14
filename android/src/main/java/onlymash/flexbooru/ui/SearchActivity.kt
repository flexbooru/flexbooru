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

package onlymash.flexbooru.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.commitNow
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.ui.fragment.PostFragment

class SearchActivity : BaseActivity() {

    companion object {
        fun startActivity(context: Context, keyword: String) {
            context.startActivity(
                Intent(context, SearchActivity::class.java)
                    .putExtra(Constants.KEYWORD_KEY, keyword))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setExitSharedElementCallback(sharedElementCallback)
        val keyword = intent?.extras?.getString(Constants.KEYWORD_KEY) ?: ""
        val uid = Settings.activeBooruUid
        val booru = BooruManager.getBooruByUid(uid) ?: return
        if (savedInstanceState != null) return
        supportFragmentManager.commitNow(allowStateLoss = true) {
            replace(R.id.fragment_post_container, PostFragment.newInstance(
                booru = booru,
                keyword = keyword,
                user = UserManager.getUserByBooruUid(uid),
                postType = PostFragment.POST_SEARCH
            ))
        }
    }

    internal var sharedElement: View? = null

    private val sharedElementCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
            if (names == null || sharedElements == null) return
            names.clear()
            sharedElements.clear()
            sharedElement?.let { view ->
                view.transitionName?.let { name ->
                    names.add(name)
                    sharedElements[name] = view
                }
            }
        }
    }
}
