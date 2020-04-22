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

package onlymash.flexbooru.ui.base

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.isNightThemeDark
import onlymash.flexbooru.ui.activity.*

abstract class BaseActivity : AppCompatActivity() {

    private fun getThemeRes(): Int {
        return when (this) {
            is MainActivity -> {
                if (isNightThemeDark) {
                    R.style.AppTheme_NoActionBar_Scrim_Main
                } else {
                    R.style.AppTheme_Black_NoActionBar_Scrim_Main
                }
            }
            is SearchActivity -> {
                if (isNightThemeDark) {
                    R.style.AppTheme_NoActionBar_Scrim_NoAnimation
                } else {
                    R.style.AppTheme_Black_NoActionBar_Scrim_NoAnimation
                }
            }
            is AccountConfigActivity -> {
                if (isNightThemeDark) {
                    R.style.AppTheme_NoActionBar_Animation
                } else {
                    R.style.AppTheme_Black_NoActionBar_Animation
                }
            }
            is ScannerActivity -> {
                if (isNightThemeDark) {
                    R.style.AppTheme_Animation
                } else {
                    R.style.AppTheme_Black_Animation
                }
            }
            else -> {
                if (isNightThemeDark) {
                    R.style.AppTheme_ScrimNavBar_Animation
                } else {
                    R.style.AppTheme_Black_ScrimNavBar_Animation
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        if (this !is DetailActivity) {
            setTheme(getThemeRes())
        }
        super.onCreate(savedInstanceState)
    }
}