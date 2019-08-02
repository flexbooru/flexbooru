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

package onlymash.flexbooru.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

open class BottomNavigationSnackbarBehavior @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
): CoordinatorLayout.Behavior<BottomNavigationView>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: BottomNavigationView,
                                 dependency: View): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            updateSnackbar(child, dependency)
        }
        return super.layoutDependsOn(parent, child, dependency)
    }

    private fun updateSnackbar(child: View, snackbarLayout: Snackbar.SnackbarLayout) {
        val params = snackbarLayout.layoutParams
        if (params is CoordinatorLayout.LayoutParams) {
            params.anchorId = child.id
            params.anchorGravity = Gravity.TOP
            params.gravity = Gravity.TOP
            snackbarLayout.layoutParams = params
        }
    }
}