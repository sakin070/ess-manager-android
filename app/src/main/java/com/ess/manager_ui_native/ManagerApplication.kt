package com.ess.manager_ui_native

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.*
import com.ess.manager_ui_native.database.LotteryCardDatabase
import java.util.concurrent.TimeUnit

class ManagerApplication : Application() {

    override fun onCreate() {
        Log.d(TAG, "onCreate: Application")
        super.onCreate()
        val sendLogsWorkRequest =
            PeriodicWorkRequestBuilder<NetworkWorker>(15, TimeUnit.MINUTES).setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "sendNetworkRequests", ExistingPeriodicWorkPolicy.KEEP, sendLogsWorkRequest
        )
    }

    companion object{
        const val TAG = "ManagerApplication"
    }
}

