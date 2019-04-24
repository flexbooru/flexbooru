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

package onlymash.flexbooru.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.ui.fragment.*

class NavPagerAdapter(fragmentManager: FragmentManager,
                      private val booru: Booru, private val user: User?) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> PostFragment.newInstance(booru = booru, user = user)
            1 -> PopularFragment.newInstance(booru = booru, user = user)
            2 -> PoolFragment.newInstance(booru = booru, user = user)
            3 -> TagFragment.newInstance(booru = booru, user = user)
            else -> ArtistFragment.newInstance(booru = booru, user = user)
        }
    }

    override fun getCount(): Int = 5

}