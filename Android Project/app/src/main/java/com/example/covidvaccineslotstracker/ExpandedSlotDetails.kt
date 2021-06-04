package com.example.covidvaccineslotstracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.covidvaccineslotstracker.models.Session
import kotlinx.android.synthetic.main.activity_expanded_slot_details.*

class ExpandedSlotDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expanded_slot_details)

        val receivedData: ArrayList<Session> = intent.extras!!.get("Data") as ArrayList<Session>

        val slotsListAdapter = SlotsListRecyclerViewAdapter(receivedData)
        slotsDataRecyclerView.layoutManager = LinearLayoutManager(this)
        slotsDataRecyclerView.adapter = slotsListAdapter

    }
}