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

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.ui.activity.*
import onlymash.flexbooru.ui.helper.isNightEnable

abstract class BaseActivity : AppCompatActivity() {

    private fun getThemeRes(): Int {
        return when (this) {
            is MainActivity -> R.style.AppTheme_Black_NoActionBar_Scrim_Main
            is SearchActivity -> R.style.AppTheme_Black_NoActionBar_Scrim_NoAnimation
            is AccountConfigActivity -> R.style.AppTheme_Black_NoActionBar_Animation
            is ScannerActivity -> R.style.AppTheme_Black_Animation
            else -> R.style.AppTheme_Black_ScrimNavBar_Animation
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        if (this !is DetailActivity) {
            if (resources.configuration.isNightEnable() && !Settings.isNightThemeDark) {
                setTheme(getThemeRes())
            }
        }
        super.onCreate(savedInstanceState)
    }
}