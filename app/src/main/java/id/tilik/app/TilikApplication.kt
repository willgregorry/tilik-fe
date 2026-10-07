package id.tilik.app

import android.app.Application
import id.tilik.app.data.session.SessionManager
import timber.log.Timber

class TilikApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
