package app.ocnotes.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.ocnotes.OcnotesApplication

// Periodic sync worker: drains pending edits and pulls updates.
class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as OcnotesApplication).container
        return try {
            container.repository.sync()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
