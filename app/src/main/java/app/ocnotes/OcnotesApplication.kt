package app.ocnotes

import android.app.Application
import app.ocnotes.di.AppContainer
import app.ocnotes.sync.SyncScheduler

class OcnotesApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Network image loading for raw-HTML <img> in the note view (Coil + OkHttp).
        coil3.SingletonImageLoader.setSafe { ctx ->
            coil3.ImageLoader.Builder(ctx)
                .components { add(coil3.network.okhttp.OkHttpNetworkFetcherFactory()) }
                .build()
        }
        container = AppContainer(this)
        SyncScheduler.schedule(this, 15)
    }
}
