package onlymash.flexbooru

import android.app.Application
import com.squareup.leakcanary.LeakCanary

class App : Application() {
    companion object {
        lateinit var app: App
    }
    override fun onCreate() {
        super.onCreate()
        app = this
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this)
        }
    }
}