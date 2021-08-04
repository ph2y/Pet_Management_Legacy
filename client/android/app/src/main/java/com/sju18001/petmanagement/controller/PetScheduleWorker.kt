package com.sju18001.petmanagement.controller

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class PetScheduleWorker(context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams
) {
    override fun doWork(): Result {
        Log.e("DID", "IT!")

        return Result.success()
    }
}