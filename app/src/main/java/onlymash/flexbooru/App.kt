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

package onlymash.flexbooru

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import onlymash.flexbooru.glide.GlideApp

class App : Application() {
    companion object {
        lateinit var app: App
    }
    val sp: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(app) }
    val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private val drawerImageLoader = object : AbstractDrawerImageLoader() {
        override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
            GlideApp.with(imageView.context).load(uri).placeholder(placeholder).into(imageView)
        }
        override fun cancel(imageView: ImageView) {
            Glide.with(imageView.context).clear(imageView)
        }
    }
    override fun onCreate() {
        super.onCreate()
        app = this
        AppCompatDelegate.setDefaultNightMode(Settings.instance().nightMode)
        DrawerImageLoader.init(drawerImageLoader)
    }
}