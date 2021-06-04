package com.example.covidvaccineslotstracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.covidvaccineslotstracker.models.Session


class SlotsListRecyclerViewAdapter(private val dataSet: ArrayList<Session>) :
    RecyclerView.Adapter<SlotsListRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.slotsDetails)
        val container: ConstraintLayout = view.findViewById(R.id.slotsDetailsContainer)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.slot_details_recycler_view_list_layout, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.textView.text = dataSet[position].toString()
        if (dataSet[position].available_capacity == 0) {
            viewHolder.container.setBackgroundResource(R.drawable.slot_item_bg_not_awailble)
        }

    }

    override fun getItemCount() = dataSet.size

}
