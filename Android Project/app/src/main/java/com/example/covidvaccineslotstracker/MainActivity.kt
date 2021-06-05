package com.example.covidvaccineslotstracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.covidvaccineslotstracker.models.Session
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var reqQueue: RequestQueue
    private val baseUrl = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByPin?"


    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        reqQueue = Volley.newRequestQueue(this)


        val pref = this.getSharedPreferences("PINDATA", Context.MODE_PRIVATE)
        Log.e(
            "check",
            "Pin is ${
                pref.getInt(
                    "Pin",
                    0
                )
            } \n  " + "Time is ${
                pref.getInt(
                    "Time",
                    0
                )
            }  "
        )

        ArrayAdapter.createFromResource(
            this,
            R.array.age_filter_choices,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            ageFilterChoiceSpinner.adapter = adapter
        }

        ageFilterChoiceSpinner.setSelection(
            pref.getInt(
                "AgeF",
                0
            )
        )

        pinText.setText(
            pref.getInt(
                "Pin",
                0
            ).toString()
        )
        retryTimeText.setText(
            pref.getInt(
                "Time",
                0
            ).toString()
        )

        startBtn.setOnClickListener {
            if (isValidPin() || retryTimeText.text.toString().toFloat().toInt() >= 1) {


                val editor: SharedPreferences.Editor = pref.edit()
                editor.putInt("Pin", pinText.text.toString().toInt())
                editor.putInt("Time", retryTimeText.text.toString().toFloat().toInt())
                editor.putInt("AgeF", ageFilterChoiceSpinner.selectedItemPosition)
                editor.commit()


                startService(
                    Intent(
                        this, SlotsTracker::class.java
                    )
                )

            } else {
                Toast.makeText(this, "input not valid", Toast.LENGTH_SHORT).show()
            }
        }
        endBtn.setOnClickListener {

            stopService(
                Intent(
                    this, SlotsTracker::class.java
                )
            )


        }



        exploreCentersBtn.setOnClickListener {
            reqQueue.cancelAll("DataReq")

            if (isValidPin()) {


                val centerObjList = ArrayList<Session>(0)

                val url = baseUrl + "pincode=" + pinText.text.toString() + "&date=" + dateForData()
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

                        if (centerObjList.size > 0) {
                            val intentToSlotsPage = Intent(this, ExpandedSlotDetails::class.java)
                            intentToSlotsPage.putExtra("Data", centerObjList)

                            startActivity(intentToSlotsPage)
                        } else {
                            Toast.makeText(
                                this,
                                "An Error Occurred please try again after some time",
                                Toast.LENGTH_LONG
                            ).show()
                        }


                    },
                    { error ->
                        Log.e(
                            "ERROR", "An Error Occurred \n" +
                                    "Error : $error "
                        )

                        Toast.makeText(
                            this,
                            "PLease Wait, An Error occurred while loading centers data",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                )

                jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                    3000,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )

                jsonObjectRequest.tag = "DataReq"
                reqQueue.add(jsonObjectRequest)


            }
        }
    }


    private fun isValidPin(): Boolean {

        if (pinText.text != null) {
            if (pinText.text.toString().length != 6) {
                return false
            } else {
                val str = pinText.text.toString()
                for (i in 0..5) {
                    if (!str[i].isDigit()) {
                        return false
                    }
                }
            }
            return true

        } else {
            return false
        }


    }

    private fun dateForData(): String {

        if (currentHour() > 18) {
            return getTomorrowsDate()
        } else {
            return getTodaysDate()
        }

    }

    private fun currentHour(): Int {
        val simpleDateFormat = SimpleDateFormat("HH", Locale.ENGLISH)
        Log.e("crnt Hour", "Hour is ${simpleDateFormat.format(Date()).toInt()}")
        return simpleDateFormat.format(Date()).toInt()
    }


    private fun getTodaysDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return simpleDateFormat.format(Date())
    }

    private fun getTomorrowsDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return simpleDateFormat.format(
            System.currentTimeMillis() + 86400000
        )
    }


}