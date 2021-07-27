package com.example.workmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.coroutines.delay

class ImageDownloadWorker(private val context: Context,
                          private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

        override suspend fun doWork(): Result {
            //0. Here, you call setForeground() from CoroutineWorker to mark your work as long-running
            // or important.
            setForeground(createForegroundInfo())

            //1. The image downloads quickly. To simulate a near real-world situation, you add a delay
            // of 10,000 milliseconds so the work can take time
            delay(10000)
            //2. You get the URI from the extension function you added
            val savedUri = context.getUriFromUrl()
            //3. Last, once you have the URI, you return a successful response to notify that your work has
            // finished without failure. workDataOf() converts a list of pairs to a Data object. A Data
            // object is a set of key/value pairs used as inputs/outputs for ListenableWorker‘s. IMAGE_URI
            // is a key for identifying the result. You’re going to use it to get the value from this worker.
            return Result.success(workDataOf("IMAGE_URI" to savedUri.toString()))
        }

        private fun createForegroundInfo(): ForegroundInfo {
            // 1. This is a PendingIntent you’ll use to cancel the work.
            val intent = WorkManager.getInstance(applicationContext)
                .createCancelPendingIntent(id)

            // 2. This is the actual notification with an icon, title and cancel action. The cancel
            // action is necessary for canceling work.
            val notification = NotificationCompat.Builder(
                applicationContext, "workDownload")
                .setContentTitle("Downloading Your Image")
                .setTicker("Downloading Your Image")
                .setSmallIcon(R.drawable.notification_action_background)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_delete, "Cancel Download", intent)

            // 3. You’re creating a notification channel for Android versions above Oreo. Then, you return
            // ForegroundInfo, which you use to update your ongoing notification.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel(notification, "workDownload")
            }

            return ForegroundInfo(1, notification.build())
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createChannel(
            notificationBuilder: NotificationCompat.Builder,
            id: String
        ) {

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE)

            val channel = NotificationChannel(
                id,
                "WorkManagerApp",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.description = "WorkManagerApp Notifications"
            notificationManager.createNotificationChannel(channel)
        }

        /**
         * Requesting Diagnostic Information from WorkManager:

        WorkManager provides a way to get the following information:
        1. Requests that have been completed in the past 24 hours
        2. Requests that have been scheduled
        3. Running work requests

        This information is available for debug builds. To get this information, run this command on your terminal:
        adb shell am broadcast -a "androidx.work.diagnostics.REQUEST_DIAGNOSTICS" -p "com.raywenderlich.android.workmanager"

        Documentation: https://developer.android.com/topic/libraries/architecture/workmanager
         */
    }
