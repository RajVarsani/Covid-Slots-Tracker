package com.example.covidvaccineslotstracker.models

import java.io.Serializable

class Center(
    var center_id: Int = 711588,
    var name: String = "0A NZ SCHOOL 285 -286 KOSAD",
    var address: String = "H 3 AWAS KOSAD",
    var state_name: String = "Gujarat",
    var district_name: String = "Surat Corporation",
    var block_name: String = "North Zone",
    var pincode: Int = 394107,
    var lat: Int = 21,
    var long: Int = 72,
    var from: String = "10:00:00",
    var to: String = "16:00:00",
    var fee_type: String = "Free",
    var sessions :ArrayList<Session>
) : Serializable{
    override fun toString(): String {
        return "center_id : $center_id\n" +
                "name : $name\n" +
                "address : $address\n" +
                "state_name : $state_name\n" +
                "district_name : $district_name\n" +
                "block_name : $block_name\n" +
                "pincode : $pincode\n" +
                "lat : $lat\n" +
                "long : $long\n" +
                "from : $from\n" +
                "to : $to\n" +
                "fee_type : $fee_type\n" +
                "sessions : $sessions \n\n\n"
    }
}