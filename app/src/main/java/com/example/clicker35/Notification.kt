package com.example.clicker35

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat


object NotificationHelper{
    private const val CHANNEL_ID = "offline_earnings_channel"

    fun createNotificationChannel(context: Context){
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Оффлайн доход",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомление о заработке в оффлайн"
        }

        val manager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun sendNotification(context: Context, earnings: String){
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_info_24)
            .setContentTitle("Вы заработали оффлайн!")
            .setContentText("За время отсутствия вы заработали $earnings очков.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val manager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }
}

//class OfflineEarningsWorker(
//    context: Context,
//    params: WorkerParameters
//):CoroutineWorker(context, params){
//
//}