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

package onlymash.flexbooru.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Keys.POST_QUERY
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.data.database.HistoryManager
import onlymash.flexbooru.data.model.common.History
import onlymash.flexbooru.extension.setupInsets
import onlymash.flexbooru.ui.base.PathActivity

class SearchActivity : PathActivity() {

    companion object {
        fun startSearch(context: Context, query: String) {
            HistoryManager.createHistory(
                History(booruUid = activatedBooruUid, query = query))
            context.startActivity(Intent(context, SearchActivity::class.java)
                .putExtra(POST_QUERY, query))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setupInsets { }
    }
}