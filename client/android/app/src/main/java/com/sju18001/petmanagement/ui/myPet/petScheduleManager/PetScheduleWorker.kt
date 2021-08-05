package com.sju18001.petmanagement.ui.myPet.petScheduleManager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sju18001.petmanagement.R

class PetScheduleWorker(context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams
) {
    companion object{
        const val CHANNEL_NAME = "일정 알림"
        const val CHANNEL_ID = "PET_SCHEDULE"
        const val NOTIFICATION_ID = 1000
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        // 알림 채널
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        channel.description = "반려동물의 일정 알림 기능입니다."
        
        // 채널 등록
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        
        // 알림 빌드
        val memo = inputData.getString("MEMO")
        val contentText = if(memo.isNullOrEmpty()) "밥 줄 시간이에요!" else memo

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_pets_24)
            .setContentTitle(CHANNEL_NAME)
            .setContentText(contentText)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        
        // 알림 띄우기
        with(NotificationManagerCompat.from(applicationContext)){
            notify(NOTIFICATION_ID, builder.build())
        }

        return Result.success()
    }
}