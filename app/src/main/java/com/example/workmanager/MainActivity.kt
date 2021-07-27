package com.example.workmanager

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.work.*
import androidx.core.net.toUri
import com.example.workmanager.databinding.ActivityMainBinding
import com.example.workmanager.ImageDownloadWorker
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var activityHomeBinding: ActivityMainBinding
    private val workManager by lazy {
        WorkManager.getInstance(applicationContext)
    }

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresStorageNotLow(true)
        .setRequiresBatteryNotLow(true)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        activityHomeBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityHomeBinding.root)
        activityHomeBinding.tvWorkInfo.visibility = View.GONE

        requestStoragePermissions()

        activityHomeBinding.btnImageDownload.setOnClickListener {
            showLottieAnimation()
            activityHomeBinding.downloadLayout.visibility = View.GONE
            createOneTimeWorkRequest()
            //createPeriodicWorkRequest()
            //createDelayedWorkRequest()
        }

        activityHomeBinding.btnQueryWork.setOnClickListener {
            queryWorkInfo()
        }

    }

    private fun createOneTimeWorkRequest() {
        val imageWorker = OneTimeWorkRequestBuilder<ImageDownloadWorker>()
            .setConstraints(constraints)
            .addTag("imageWork")
            .build()

        workManager.enqueueUniqueWork(
            "oneTimeImageDownload",
            ExistingWorkPolicy.KEEP,
            imageWorker
        )

        observeWork(imageWorker.id)
    }

    private fun createPeriodicWorkRequest() {
        val imageWorker = PeriodicWorkRequestBuilder<ImageDownloadWorker>(
            15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag("imageWork")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "periodicImageDownload",
            ExistingPeriodicWorkPolicy.KEEP,
            imageWorker
        )

        observeWork(imageWorker.id)
    }

    private fun createDelayedWorkRequest() {
        val imageWorker = OneTimeWorkRequestBuilder<ImageDownloadWorker>()
            .setConstraints(constraints)
            .setInitialDelay(30, TimeUnit.SECONDS)
            .addTag("imageWork")
            .build()

        workManager.enqueueUniqueWork(
            "delayedImageDownload",
            ExistingWorkPolicy.KEEP,
            imageWorker
        )

        observeWork(imageWorker.id)
    }

    private fun observeWork(id: UUID) {
        workManager.getWorkInfoByIdLiveData(id)
            .observe(this, observeWorkInfo)
    }

    private fun queryWorkInfo() {
        val workQuery = WorkQuery.Builder
            .fromTags(listOf("imageWork"))
            .addStates(listOf(WorkInfo.State.SUCCEEDED))
            .addUniqueWorkNames(
                listOf("oneTimeImageDownload", "delayedImageDownload", "periodicImageDownload")
            )
            .build()

        workManager.getWorkInfosLiveData(workQuery)
            .observe(this, observeWorkInfoList)
    }

    private val observeWorkInfo = Observer<WorkInfo> { info ->
        if (info != null && info.state.isFinished) {
            hideLottieAnimation()
            activityHomeBinding.downloadLayout.visibility = View.VISIBLE
            val uriResult = info.outputData.getString("IMAGE_URI")
            if (uriResult != null) showDownloadedImage(uriResult.toUri())
        }
    }

    private val observeWorkInfoList = Observer<List<WorkInfo>> { workInfoList ->
        activityHomeBinding.tvWorkInfo.visibility = View.VISIBLE
        activityHomeBinding.tvWorkInfo.text =
            resources.getQuantityString(R.plurals.text_work_desc, workInfoList.size, workInfoList.size)
    }

    private fun requestStoragePermissions() {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        }

    private fun showLottieAnimation() {
        activityHomeBinding.animationView.visibility = View.VISIBLE
        activityHomeBinding.animationView.playAnimation()

    }

    private fun hideLottieAnimation() {
        activityHomeBinding.animationView.visibility = View.GONE
        activityHomeBinding.animationView.cancelAnimation()

    }

    private fun showDownloadedImage(resultUri: Uri?) {
        activityHomeBinding.completeLayout.visibility = View.VISIBLE
        activityHomeBinding.downloadLayout.visibility = View.GONE
        hideLottieAnimation()
        activityHomeBinding.imgDownloaded.setImageURI(resultUri)
    }
}