package onlymash.flexbooru

import android.app.Application
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import moe.shizuku.preference.PreferenceManager
import onlymash.flexbooru.glide.GlideApp

class App : Application() {
    companion object {
        lateinit var app: App
    }
    val sp: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(app) }
    override fun onCreate() {
        super.onCreate()
        app = this
        DrawerImageLoader.init(drawerImageLoader)
    }
    private val drawerImageLoader = object : AbstractDrawerImageLoader() {
        override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
            GlideApp.with(imageView.context).load(uri).placeholder(placeholder).into(imageView)
        }
        override fun cancel(imageView: ImageView) {
            Glide.with(imageView.context).clear(imageView)
        }
    }
}