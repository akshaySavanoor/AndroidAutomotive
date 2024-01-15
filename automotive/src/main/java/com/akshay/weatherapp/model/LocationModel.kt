package com.akshay.weatherapp.model

data class Location(
    val latitude: Double,
    val longitude: Double
)

data class Place(
    val name: String,
    val distance: Double,
    val isOpen: Boolean
)





