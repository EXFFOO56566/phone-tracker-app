package com.softexpoitmaps.findmyphone.business

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener

import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception
import java.util.*


@Suppress("DEPRECATION")
class locationservice:Service() {
    private var longitude: Double = 0.toDouble()
    private var latitude: Double = 0.toDouble()


    override fun onBind(p0: Intent?): IBinder? {

        return null
    }



    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


    if ((FirebaseAuth.getInstance().currentUser) != null) {
        onTaskRemoved(intent)
        val f = FirebaseAuth.getInstance().currentUser!!

        createNotificationChannel()
    val lm = getSystemService(LOCATION_SERVICE) as LocationManager

        val intent:Intent=Intent(this, MainActivity::class.java)
        val pending:PendingIntent= PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

           val  notification:Notification = NotificationCompat.Builder(this,
               "com.softexpoit.findmyphone.business").setContentTitle("Find My Phone")
                 .setContentText("MyMap - Find My Phone Running On This Device ")
                 .setSmallIcon(
                     R.drawable.logo_map1)
                 .setContentIntent(pending).build()

             startForeground(101660, notification)

if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
      if( lm.isLocationEnabled &&  lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
try {
    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
        1000L,
        1.0f,
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                try {
                    val locations = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    longitude = locations!!.longitude
                    latitude = locations.latitude

                    val ref =
                        FirebaseDatabase.getInstance().reference.child("users").child(f.uid)

                    val userHashmap = HashMap<String, Any>()



                    userHashmap["latitudes"] = "$latitude"
                    userHashmap["longitudes"] = "$longitude"

                    ref.updateChildren(userHashmap).addOnCompleteListener { tasks ->
                        if (tasks.isSuccessful) {


                        } else {

                        }
                    }


                } catch (e: Exception) {

                }


            }

        })


}catch (e:Exception){

}




       }
    

    }








        return START_STICKY


    }



return START_NOT_STICKY




    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationchannel: NotificationChannel = NotificationChannel("com.softexpoit.findmyphone.business",
            "Foreground",
            NotificationManager.IMPORTANCE_LOW)
            notificationchannel.setSound(null,null)
        val manager: NotificationManager =getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(notificationchannel)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val service: PendingIntent = PendingIntent.getService(getApplicationContext(), 1001, Intent(
            getApplicationContext(),
            locationservice::class.java), PendingIntent.FLAG_ONE_SHOT)

        val alarmManager: AlarmManager =  getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, service)


    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        if ((FirebaseAuth.getInstance().currentUser) != null) {
            startForegroundService(Intent(this, locationservice::class.java))
        }



        }









}