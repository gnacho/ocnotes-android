package app.ocnotes

import android.app.Application
import app.ocnotes.di.AppContainer
import app.ocnotes.sync.SyncScheduler

class OcnotesApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        SyncScheduler.schedule(this, 15)
    }
}
