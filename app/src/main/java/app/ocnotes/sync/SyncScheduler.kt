package app.ocnotes.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private const val UNIQUE_NAME = "ocnotes_sync"
    private const val MIN_INTERVAL_MINUTES = 15L // WorkManager minimum periodic interval

    // minutes <= 0 or enabled=false = never (cancels any scheduled work).
    fun schedule(context: Context, minutes: Long, enabled: Boolean = true, wifiOnly: Boolean = false) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled || minutes <= 0) {
            workManager.cancelUniqueWork(UNIQUE_NAME)
            return
        }
        val interval = maxOf(minutes, MIN_INTERVAL_MINUTES)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<SyncWorker>(interval, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
