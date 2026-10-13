package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

class CosmoNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "cosmo_radar_alerts"
        const val CHANNEL_NAME = "航天前沿雷达订阅推送"
        const val CHANNEL_DESC = "实时推送命中关键词的最新航天重大进展与火箭发射动态"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = 0xFF00E5FF.toInt()
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun sendSpaceAlertNotification(
        notificationId: Int,
        keyword: String,
        articleTitle: String,
        articleSnippet: String,
        articleId: String
    ): Boolean {
        if (!hasNotificationPermission()) {
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_ARTICLE_ID", articleId)
            putExtra("EXTRA_KEYWORD", keyword)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationTitle = "🚀 关键词命中: 【$keyword】"
        val notificationContent = "$articleTitle\n$articleSnippet"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(0xFF00E5FF.toInt())
            .setContentTitle(notificationTitle)
            .setContentText(articleTitle)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$articleTitle\n\n$articleSnippet\n\n点击立即查看完整航天前沿技术解析")
                    .setSummaryText("星穹航天雷达推送")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            return true
        } catch (_: SecurityException) {
            return false
        }
    }
}
