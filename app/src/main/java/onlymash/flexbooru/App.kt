package onlymash.flexbooru

import android.app.Application

class App : Application() {
    companion object {
        lateinit var app: App
    }

    val serviceLocator by lazy { ServiceLocator.instance(app) }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}