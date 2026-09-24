package id.tilik.app

import android.app.Application
import timber.log.Timber

class TilikApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
