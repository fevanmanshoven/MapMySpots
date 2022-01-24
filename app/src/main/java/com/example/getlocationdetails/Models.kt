package com.example.getlocationdetails

data class LocationModel(
    var locId : Long= 0L,
    var lat : String = "",
    var lon : String = "",
    var alt : String = "",
    var speed: String = "",
    var accuracy: String = "",
    var address: String = "",
)