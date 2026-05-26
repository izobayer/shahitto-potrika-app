package bd.du.bangla.shahittopotrika.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import bd.du.bangla.shahittopotrika.MainActivity
import bd.du.bangla.shahittopotrika.R
import bd.du.bangla.shahittopotrika.ShahittoPotrikaApplication
import kotlinx.coroutines.flow.first

class NewIssueCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as ShahittoPotrikaApplication
        val prefs = app.repository.prefs

        val notifEnabled = prefs.notificationsEnabled.first()
        if (!notifEnabled) return Result.success()

        return try {
            val result = app.repository.refreshCurrentIssue()
            result.fold(
                onSuccess = { issue ->
                    if (issue != null) {
                        val lastSeenId = prefs.lastSeenIssueId.first()
                        if (lastSeenId.isNotBlank() && issue.id != lastSeenId) {
                            sendNotification(issue.title)
                        }
                        prefs.setLastSeenIssueId(issue.id)
                    }
                    Result.success()
                },
                onFailure = { Result.retry() }
            )
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(issueTitle: String) {
        val nm = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "নতুন সংখ্যা",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "সাহিত্য পত্রিকার নতুন সংখ্যা প্রকাশ" }
            )
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("নতুন সংখ্যা প্রকাশিত! 📖")
            .setContentText(issueTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(issueTitle))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID      = "new_issue_channel"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME       = "new_issue_check"
    }
}
