package com.arffsnia.simap

import android.app.Application
import com.arffsnia.simap.data.SessionManager
import com.arffsnia.simap.data.api.ApiClient
import com.arffsnia.simap.data.api.ApiService
import com.arffsnia.simap.data.db.OfflineDb

class SimapApp : Application() {

    companion object {
        lateinit var instance: SimapApp
            private set
    }

    lateinit var session: SessionManager
        private set
    lateinit var api: ApiService
        private set
    lateinit var db: OfflineDb
        private set

    val authRepo by lazy { com.arffsnia.simap.data.repo.AuthRepository(api, session) }
    val assetRepo by lazy { com.arffsnia.simap.data.repo.AssetRepository(api, db) }
    val checkRepo by lazy { com.arffsnia.simap.data.repo.CheckRepository(api, db) }
    val adminRepo by lazy { com.arffsnia.simap.data.repo.AdminRepository(api) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        session = SessionManager(this)
        api = ApiClient.create(session)
        db = OfflineDb(this)
        scheduleAutoSync()
    }

    private fun scheduleAutoSync() {
        val req = androidx.work.PeriodicWorkRequestBuilder<com.arffsnia.simap.data.sync.SyncWorker>(
            30, java.util.concurrent.TimeUnit.MINUTES
        ).build()
        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "simap-auto-sync",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            req,
        )
    }
}