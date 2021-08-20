package com.think.emicalculator

import android.content.Context
import androidx.work.Operation.SUCCESS
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.think.emicalculator.locationutils.LocationUtils

class TrackLocationWorker (
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {



    override fun doWork(): Result {
        return try {
            //if(LocationUtils.isLocationEnabled(context)){
                LocationUtils.getUserLocation(context)
            //}
           // else LocationUtils.locationSetup(context)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}