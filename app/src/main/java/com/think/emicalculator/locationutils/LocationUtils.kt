package com.think.emicalculator.locationutils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.work.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.think.emicalculator.R
import com.think.emicalculator.TrackLocationWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


object LocationUtils {


    const val LOCATION_WORK_TAG = "LOCATION_WORK_TAG"
    const val LOCATION_ENABLED_REQUEST = "102"
    val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

    var location:Location?=null
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    fun showGPSNotEnabledDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.enable_gps))
            .setMessage(context.getString(R.string.required_for_this_app))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.enable_now)) { _, _ ->
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .show()
    }

    fun locationRequest(): LocationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 3 * 1000
        fastestInterval = 5 * 1000
    }

    fun trackLocation(context: Context) {

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val locationWorker = PeriodicWorkRequestBuilder<TrackLocationWorker>(15, TimeUnit.MINUTES).addTag(
            LOCATION_WORK_TAG
        ).setConstraints(constraints)
            // setting a backoff on case the work needs to retry
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(
            LOCATION_WORK_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            locationWorker
        )
    }


    private fun getDateAndTime():String{
        val date = Date()
        return  formatter.format(date)

    }
    @SuppressLint("MissingPermission")
    fun getUserLocation(context: Context)
    {
        LocationServices.getFusedLocationProviderClient(context)
            .lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                if (location != null){
                    this.location=location
                    val database = FirebaseDatabase.getInstance()
                    val fileNo=getFileNo(context = context)!!
                    if(fileNo != "NO_FILE") {

                        val myRef = database.reference.child(fileNo)

                        val timeAndDate = getDateAndTime()
                        myRef.child("Location $timeAndDate")
                            .setValue("${location.latitude}  ${location.longitude}")
                        println("Location ${location.latitude}  ${location.longitude}")
                    }
                    //  myRef.setValue(" cool" )

                }
                // saveLocation(Location(0, location.latitude, location.longitude, System.currentTimeMillis()))
            }
    }

    fun getSharedPreferences(context:Context):SharedPreferences=context.getSharedPreferences("com.think.emicalculator",Context.MODE_PRIVATE)

    fun writeToSharedPreferences(context:Context,fileNo:String){
        val prefrence= getSharedPreferences(context)
        with (prefrence.edit()) {
            putString("FILE_NO", fileNo)
            apply()
        }

    }

    fun getFileNo(context:Context):String?{
        val prefrence= getSharedPreferences(context)
        return  prefrence.getString("FILE_NO", "NO_FILE")
    }

    fun getUserLocation():Location?{
        return this.location
    }

}
