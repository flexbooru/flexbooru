package onlymash.flexbooru

import android.app.Application
import android.content.SharedPreferences
import com.squareup.leakcanary.LeakCanary
import moe.shizuku.preference.PreferenceManager

class App : Application() {
    companion object {
        lateinit var app: App
    }
    val sp: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(app) }
    override fun onCreate() {
        super.onCreate()
        app = this
//        if (!LeakCanary.isInAnalyzerProcess(this)) {
//            LeakCanary.install(this)
//        }
    }
}