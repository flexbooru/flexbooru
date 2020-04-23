/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.extension

import android.annotation.SuppressLint
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.ui.R
import com.google.android.material.bottomnavigation.BottomNavigationView

fun BottomNavigationView.setup(navController: NavController) {

    setOnNavigationItemSelectedListener { menuItem ->
        if (menuItem.itemId == navController.currentDestination?.id) {
            true
        } else {
            onNavDestinationSelected(menuItem.itemId, navController)
        }
    }
}

@SuppressLint("RestrictedApi")
private fun onNavDestinationSelected(
    @IdRes itemId: Int,
    navController: NavController): Boolean {

    val options = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setEnterAnim(R.anim.nav_default_enter_anim)
        .setExitAnim(R.anim.nav_default_exit_anim)
        .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
        .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
        .build()

    return try {
        val index = navController.backStack.indexOfFirst { it.destination.id == itemId }
        if (index >= 0) {
            navController.popBackStack(itemId, false)
        } else {
            navController.navigate(itemId, null, options)
        }
        true
    } catch (_: IllegalArgumentException) {
        false
    } catch (_: IllegalStateException) {
        false
    }
}