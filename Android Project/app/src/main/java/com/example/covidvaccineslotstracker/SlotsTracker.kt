package com.example.covidvaccineslotstracker

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.covidvaccineslotstracker.models.Session
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class SlotsTracker : Service() {


    private lateinit var pendingIntent: PendingIntent

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel


    private var pincode: Int = 0
    private var retryInterval: Long = 5000
    private lateinit var mHandler: Handler
    private val runnableForImageSwap = object : Runnable {
        override fun run() {
            Log.d("Handlers", "Handler Called")
            loadData(getTodaysDate(), 0)
            loadData(getTomorrowsDate(), 1)
            mHandler.postDelayed(this, retryInterval)
        }

    }
    private lateinit var reqQueue: RequestQueue
    private val baseUrl = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByPin?"


    override fun onCreate() {

        mHandler = Handler(Looper.getMainLooper())
        mHandler.postDelayed(runnableForImageSwap, 0)
        reqQueue = Volley.newRequestQueue(this)


    }

    override fun onStart(intent: Intent?, startId: Int) {


        Toast.makeText(this, "Process Started", Toast.LENGTH_LONG).show()

        val intentTOHomePage =
            Intent(this, MainActivity::class.java)
        pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intentTOHomePage,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                "app is running",
                "notification to alert user app is running in background",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
//                            notificationChannel.lightColor = Color.BLACK
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            val builder = Notification.Builder(this, "notify slot available")
                .setContentTitle("App is Running")
                .setContentText("Click for more info")
                .setSmallIcon(R.drawable.ic_logo)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)


            startForeground(123456, builder.build())


        } else {


            val builder = Notification.Builder(this)
                .setContentTitle(" Slot(s) Available")
                .setContentText("Center :  ")
                .setSmallIcon(R.drawable.ic_logo)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)



            notificationManager.notify(123456, builder.build())

        }

        val pref = this.getSharedPreferences("PINDATA", Context.MODE_PRIVATE)

        pincode = pref.getInt(
            "Pin",
            0
        )

        retryInterval = (pref.getInt(
            "Time",
            5
        ) * 1000).toLong()

        reqQueue = Volley.newRequestQueue(this)

        mHandler = Handler(Looper.getMainLooper())
        mHandler.post(runnableForImageSwap)


    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun loadData(date: String, dayChkIndex: Int) {

        val centerObjList = ArrayList<Session>(0)
//        val centerObjList = ArrayList<Center>(0)

        val url = baseUrl + "pincode=" + pincode.toString() + "&date=" + date
        Log.e(
            "Data Checking", "URL is $url"
        )

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->


                val tempSessionJSONArrayObj = response.getJSONArray("sessions")



                for (j in 0 until tempSessionJSONArrayObj.length()) {

                    val tempSessionJSONbj = tempSessionJSONArrayObj.getJSONObject(j)

                    centerObjList.add(
                        Session(
                            tempSessionJSONbj.getInt("center_id"),
                            tempSessionJSONbj.getString("name"),
                            tempSessionJSONbj.getString("address"),
                            tempSessionJSONbj.getString("state_name"),
                            tempSessionJSONbj.getString("district_name"),
                            tempSessionJSONbj.getString("block_name"),
                            tempSessionJSONbj.getInt("pincode"),
                            tempSessionJSONbj.getString("from"),
                            tempSessionJSONbj.getString("to"),
                            tempSessionJSONbj.getInt("lat"),
                            tempSessionJSONbj.getInt("long"),
                            tempSessionJSONbj.getString("fee_type"),
                            tempSessionJSONbj.getString("session_id"),
                            tempSessionJSONbj.getString("date"),
                            tempSessionJSONbj.getInt("available_capacity_dose1"),
                            tempSessionJSONbj.getInt("available_capacity_dose2"),
                            tempSessionJSONbj.getInt("available_capacity"),
                            tempSessionJSONbj.getString("fee"),
                            tempSessionJSONbj.getInt("min_age_limit"),
                            tempSessionJSONbj.getString("vaccine"),
                            ArrayList(0)
                        )
                    )

                    val tempSlotsJSOnArrayObj = tempSessionJSONbj.getJSONArray("slots")

                    for (k in 0 until tempSlotsJSOnArrayObj.length()) {
                        centerObjList[j].slots.add(tempSlotsJSOnArrayObj.getString(k))
                    }
                }



                Log.e("Data Received", "Parsed data is : $centerObjList ")


//                checking
//                for (i in 0 until centerObjList.size) {
//                    centerObjList[i].available_capacity = (0..1).random()
//                }
//                Log.e("Data Received", "modified data is : $centerObjList ")


                for (i in 0 until centerObjList.size) {
                    if (centerObjList[i].available_capacity > 0) {
                        val intentToExpandedSlotDetails =
                            Intent(this, ExpandedSlotDetails::class.java)
                        intentToExpandedSlotDetails.putExtra("Data", centerObjList)
                        pendingIntent =
                            PendingIntent.getActivity(
                                this,
                                0,
                                intentToExpandedSlotDetails,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )

                        Log.e("Notification", "Reached")

                        notificationManager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            notificationChannel = NotificationChannel(
                                "notify slot available",
                                "notification to notify user slot is availble",
                                NotificationManager.IMPORTANCE_HIGH
                            )
                            notificationChannel.enableLights(true)
//                            notificationChannel.lightColor = Color.BLACK
                            notificationChannel.enableVibration(false)
                            notificationManager.createNotificationChannel(notificationChannel)

                            val builder = Notification.Builder(this, "notify slot available")
                                .setContentTitle(centerObjList[i].available_capacity.toString() + " Slot(s) Available")
                                .setContentText("Center : ${centerObjList[i].name} ")
                                .setSmallIcon(R.drawable.ic_logo)
                                .setPriority(Notification.PRIORITY_DEFAULT)
                                .setStyle(
                                    Notification.BigTextStyle().bigText(
                                        "Center : ${centerObjList[i].name}\n" +
                                                "Address : ${centerObjList[i].address}\n" +
                                                "Available capacity for dose 1 :${centerObjList[i].available_capacity_dose1}\n" +
                                                "Available capacity for dose 2 :${centerObjList[i].available_capacity_dose2}"
                                    )
                                )
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)


                            notificationManager.notify(dayChkIndex * 1000 + i, builder.build())


                        } else {

                            val builder = Notification.Builder(this)
                                .setContentTitle(centerObjList[i].available_capacity.toString() + " Slot(s) Available")
                                .setContentText("Center : ${centerObjList[i].name} ")
                                .setSmallIcon(R.drawable.ic_logo)
                                .setPriority(Notification.PRIORITY_DEFAULT)
                                .setStyle(
                                    Notification.BigTextStyle().bigText(
                                        "Center : ${centerObjList[i].name}\n" +
                                                "Address : ${centerObjList[i].address}\n" +
                                                "Available capacity for dose 1 :${centerObjList[i].available_capacity_dose1}\n" +
                                                "Available capacity for dose 2 :${centerObjList[i].available_capacity_dose2}"
                                    )
                                )
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)

                            notificationManager.notify(dayChkIndex * 1000 + i, builder.build())

                        }


//                        val builder = NotificationCompat.Builder(this, "notify slot available")
//                            .setContentTitle(centerObjList[i].available_capacity.toString() + " Slot(s) Available")
//                            .setContentText("Center : ${centerObjList[i].name} ")
//                            .setSmallIcon(R.drawable.ic_logo)
//                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                            .setStyle(
//                                NotificationCompat.BigTextStyle().bigText(
//                                    "Center : ${centerObjList[i].name}\n" +
//                                            "Address : ${centerObjList[i].address}\n" +
//                                            "Available capacity for dose 1 :${centerObjList[i].available_capacity_dose1}\n" +
//                                            "Available capacity for dose 2 :${centerObjList[i].available_capacity_dose2}"
//                                )
//                            )
//                            .setContentIntent(pendingIntent)
//                            .setAutoCancel(true)
//
//                        notificationManager =
//                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                        notificationManager.notify(dayChkIndex * 1000 + i, builder.build())

                    }
                }
            },
            { error ->
                Log.e(
                    "ERROR", "An Error Occurred \n" +
                            "Error : $error "
                )

                Toast.makeText(this, "Error occurred while loading covid data", Toast.LENGTH_SHORT)
                    .show()
            }
        )

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            retryInterval.toInt(),
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        reqQueue.add(jsonObjectRequest)


    }


    override fun onDestroy() {
        mHandler.removeCallbacks(runnableForImageSwap)

        Toast.makeText(this, "Process has been stopped", Toast.LENGTH_SHORT).show()

//        var notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.cancel(1)


        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(123456)
        super.onDestroy()
    }

    fun getTodaysDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return simpleDateFormat.format(Date())
    }

    fun getTomorrowsDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return simpleDateFormat.format(
            System.currentTimeMillis() + 86400000
        )
    }


}


//        For scrapped API

//                Log.e(
//                    "Data Checking", "URL is $pincode" +
//                            "response is $response"
//                )

//                val centersJSONList = response.getJSONArray("centers")
//                for (i in 0 until centersJSONList.length()) {
//                    val tempCenterJOSNObj = centersJSONList.getJSONObject(i)
//
//
//                    centerObjList.add(
//                        Center(
//                            tempCenterJOSNObj.getInt("center_id"),
//                            tempCenterJOSNObj.getString("name"),
//                            tempCenterJOSNObj.getString("address"),
//                            tempCenterJOSNObj.getString("state_name"),
//                            tempCenterJOSNObj.getString("district_name"),
//                            tempCenterJOSNObj.getString("block_name"),
//                            tempCenterJOSNObj.getInt("pincode"),
//                            tempCenterJOSNObj.getInt("lat"),
//                            tempCenterJOSNObj.getInt("long"),
//                            tempCenterJOSNObj.getString("from"),
//                            tempCenterJOSNObj.getString("to"),
//                            tempCenterJOSNObj.getString("fee_type"),
//                            ArrayList(0)
//                        )
//                    )
//
//
//                    val tempSessionJSONArrayObj = tempCenterJOSNObj.getJSONArray("sessions")
//
//                    for (j in 0 until tempSessionJSONArrayObj.length()) {
//
//                        val tempSessionJSONbj = tempSessionJSONArrayObj.getJSONObject(j)
//
//                        centerObjList[i].sessions.add(
//                            Session(
//                                tempSessionJSONbj.getString("session_id"),
//                                tempSessionJSONbj.getString("date"),
//                                tempSessionJSONbj.getInt("available_capacity"),
//                                tempSessionJSONbj.getInt("min_age_limit"),
//                                tempSessionJSONbj.getString("vaccine"),
//                                ArrayList(0),
//                                tempSessionJSONbj.getInt("available_capacity_dose1"),
//                                tempSessionJSONbj.getInt("available_capacity_dose2"),
//                            )
//                        )
//
//                        val tempSlotsJSOnArrayObj = tempSessionJSONbj.getJSONArray("slots")
//
//                        for (k in 0 until tempSlotsJSOnArrayObj.length()) {
//                            centerObjList[i].sessions[j].slots.add(tempSlotsJSOnArrayObj.getString(k))
//                        }
//                    }
//                }


//                Session Public API


//                for (i in 0 until centersJSONList.length()) {
//                    val tempCenterJOSNObj = centersJSONList.getJSONObject(i)
//
//
//                    centerObjList.add(
//                        Center(
//                            tempCenterJOSNObj.getInt("center_id"),
//                            tempCenterJOSNObj.getString("name"),
//                            tempCenterJOSNObj.getString("address"),
//                            tempCenterJOSNObj.getString("state_name"),
//                            tempCenterJOSNObj.getString("district_name"),
//                            tempCenterJOSNObj.getString("block_name"),
//                            tempCenterJOSNObj.getInt("pincode"),
//                            tempCenterJOSNObj.getInt("lat"),
//                            tempCenterJOSNObj.getInt("long"),
//                            tempCenterJOSNObj.getString("from"),
//                            tempCenterJOSNObj.getString("to"),
//                            tempCenterJOSNObj.getString("fee_type"),
//                            ArrayList(0)
//                        )
//                    )
//