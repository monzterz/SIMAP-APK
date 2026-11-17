package com.arffsnia.simap.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arffsnia.simap.SimapApp

/** Sinkron otomatis antrian pengecekan yang belum terkirim. */
class SyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return try {
            (applicationContext as SimapApp).checkRepo.syncAll()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}