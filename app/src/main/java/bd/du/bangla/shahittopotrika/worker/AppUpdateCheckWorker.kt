package bd.du.bangla.shahittopotrika.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import bd.du.bangla.shahittopotrika.BuildConfig
import bd.du.bangla.shahittopotrika.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

/**
 * Periodic worker that checks the GitHub releases API for a newer app version.
 * If a newer version exists, it posts a notification that opens the release page.
 */
class AppUpdateCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val latestRelease = fetchLatestRelease() ?: return@withContext Result.success()
            val latestTag   = latestRelease.getString("tag_name").trimStart('v')
            val currentVer  = BuildConfig.VERSION_NAME
            val releaseUrl  = latestRelease.getString("html_url")
            val releaseName = latestRelease.optString("name", "নতুন সংস্করণ $latestTag")

            if (isNewerVersion(latestTag, currentVer)) {
                sendUpdateNotification(releaseName, latestTag, releaseUrl)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun fetchLatestRelease(): JSONObject? {
        val url = "https://api.github.com/repos/izobayer/shahitto-potrika-app/releases/latest"
        val conn = URL(url).openConnection()
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
        conn.connectTimeout = 10_000
        conn.readTimeout    = 10_000
        val json = conn.getInputStream().bufferedReader().readText()
        return JSONObject(json)
    }

    /**
     * Simple semantic version comparison.
     * Returns true if [remote] > [local].
     */
    private fun isNewerVersion(remote: String, local: String): Boolean {
        fun parts(v: String) = v.split(".").map { it.toIntOrNull() ?: 0 }
        val r = parts(remote)
        val l = parts(local)
        for (i in 0 until maxOf(r.size, l.size)) {
            val rv = r.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }
            if (rv > lv) return true
            if (rv < lv) return false
        }
        return false
    }

    private fun sendUpdateNotification(name: String, version: String, url: String) {
        val nm = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "অ্যাপ আপডেট",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "নতুন সংস্করণ পাওয়া গেছে" }
            )
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("অ্যাপ আপডেট পাওয়া গেছে! 🎉")
            .setContentText("সংস্করণ $version — $name")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("সাহিত্য পত্রিকা অ্যাপের নতুন সংস্করণ $version পাওয়া গেছে। ডাউনলোড করতে ট্যাপ করুন।")
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID      = "app_update_channel"
        const val NOTIFICATION_ID = 1002
        const val WORK_NAME       = "app_update_check"
    }
}
