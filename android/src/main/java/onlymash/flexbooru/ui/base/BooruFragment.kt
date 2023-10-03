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

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.app.Settings.BOORU_UID_ACTIVATED_KEY
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.ui.viewmodel.BooruViewModel
import onlymash.flexbooru.ui.viewmodel.getBooruViewModel
import org.koin.android.ext.android.inject

abstract class BooruFragment<T: ViewBinding> : BindingFragment<T>(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val booruDao by inject<BooruDao>()

    private lateinit var booruViewModel: BooruViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        booruViewModel = getBooruViewModel(booruDao)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBaseViewCreated(view, savedInstanceState)
        booruViewModel.booru.observe(viewLifecycleOwner) {
            onBooruLoaded(it)
        }
        booruViewModel.loadBooru(Settings.activatedBooruUid)
    }

    abstract fun onBaseViewCreated(view: View, savedInstanceState: Bundle?)

    abstract fun onBooruLoaded(booru: Booru?)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == BOORU_UID_ACTIVATED_KEY) {
            booruViewModel.loadBooru(Settings.activatedBooruUid)
        }
    }
}