package bd.du.bangla.shahittopotrika

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.*
import bd.du.bangla.shahittopotrika.data.repository.JournalRepository
import bd.du.bangla.shahittopotrika.worker.AppUpdateCheckWorker
import bd.du.bangla.shahittopotrika.worker.NewIssueCheckWorker
import java.util.concurrent.TimeUnit

class ShahittoPotrikaApplication : Application() {

    val repository by lazy { JournalRepository(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleNewIssueCheck()
        scheduleAppUpdateCheck()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(
                    NewIssueCheckWorker.CHANNEL_ID,
                    "নতুন সংখ্যা",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "সাহিত্য পত্রিকার নতুন সংখ্যা প্রকাশের বিজ্ঞপ্তি"
                }
            )
            nm.createNotificationChannel(
                NotificationChannel(
                    AppUpdateCheckWorker.CHANNEL_ID,
                    "অ্যাপ আপডেট",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "সাহিত্য পত্রিকা অ্যাপের নতুন সংস্করণের বিজ্ঞপ্তি"
                }
            )
        }
    }

    private fun scheduleNewIssueCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<NewIssueCheckWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NewIssueCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleAppUpdateCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<AppUpdateCheckWorker>(
            12, TimeUnit.HOURS   // check for updates twice a day
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            AppUpdateCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
