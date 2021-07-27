package com.example.workmanager

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

// 1. The application class extends Configuration.Provider. This allows you to provide your own
// custom WorkManager configuration.
class WorkManagerApp : Application(), Configuration.Provider {

    // 2. You provide your own implementation of getWorkManagerConfiguration(), in which you
    // provide your custom WorkManager initialization. You also specify the minimum logging level.
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        // 3. Last, you initialize WorkManager with a custom configuration you’ve created.
        WorkManager.initialize(this, workManagerConfiguration)
        //Build and run the app. The functionality does not change. Now, you’re using on-demand
        // initialization for your WorkManager.
    }
}