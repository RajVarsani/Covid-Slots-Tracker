package com.example.covidvaccineslotstracker.models

import java.io.Serializable

class Session(

    var center_id: Int,
    var name: String,
    var address: String,
    var state_name: String,
    var district_name: String,
    var block_name: String,
    var pincode: Int,
    var from: String,
    var to: String,
    var lat: Int,
    var long: Int,
    var fee_type: String,
    var session_id: String,
    var date: String,
    var available_capacity_dose1: Int,
    var available_capacity_dose2: Int,
    var available_capacity: Int,
    var fee: String,
    var min_age_limit: Int,
    var vaccine: String,
    var slots: ArrayList<String> = ArrayList<String>(0)

) : Serializable {
    override fun toString(): String {
        return "center_id : $center_id \n" +
                "name : $name \n" +
                "address : $address \n" +
                "state_name : $state_name \n" +
                "district_name : $district_name \n" +
                "block_name : $block_name \n" +
                "pincode : $pincode \n" +
                "from : $from \n" +
                "to : $to \n" +
                "lat : $lat \n" +
                "long : $long \n" +
                "fee_type : $fee_type \n" +
                "session_id : $session_id \n" +
                "date : $date \n" +
                "available_capacity_dose1 : $available_capacity_dose1 \n" +
                "available_capacity_dose2 : $available_capacity_dose2 \n" +
                "available_capacity : $available_capacity \n" +
                "fee : $fee \n" +
                "min_age_limit : $min_age_limit \n" +
                "vaccine : $vaccine \n" +
                "slots : $slots \n"
    }
}