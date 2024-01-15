package com.akshay.weatherapp.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(

    @SerializedName("coord") var coord: Coordinates,
    @SerializedName("weather") var weather: List<Weather> = listOf(),
    @SerializedName("base") var base: String? = null,
    @SerializedName("main") var main: Main? = Main(),
    @SerializedName("visibility") var visibility: Int? = null,
    @SerializedName("wind") var wind: Wind? = Wind(),
    @SerializedName("rain") var rain: Rain? = Rain(),
    @SerializedName("clouds") var clouds: Clouds? = Clouds(),
    @SerializedName("dt") var dt: Int? = null,
    @SerializedName("sys") var sys: Sys,
    @SerializedName("timezone") var timezone: Int? = null,
    @SerializedName("id") var id: Int? = null,
    @SerializedName("name") var name: String,
    @SerializedName("cod") var cod: Int? = null

)

data class Coordinates(

    @SerializedName("lon") var lon: Double,
    @SerializedName("lat") var lat: Double

)

data class Weather(

    @SerializedName("id") var id: Int,
    @SerializedName("main") var main: String,
    @SerializedName("description") var description: String,
    @SerializedName("icon") var icon: String?

)

data class Main(

    @SerializedName("temp") var temp: Double? = null,
    @SerializedName("feels_like") var feelsLike: Double? = null,
    @SerializedName("temp_min") var tempMin: Double? = null,
    @SerializedName("temp_max") var tempMax: Double? = null,
    @SerializedName("pressure") var pressure: Int? = null,
    @SerializedName("humidity") var humidity: Int? = null,
    @SerializedName("sea_level") var seaLevel: Int? = null,
    @SerializedName("grnd_level") var grndLevel: Int? = null

)

data class Wind(

    @SerializedName("speed") var speed: Double? = null,
    @SerializedName("deg") var deg: Int? = null,
    @SerializedName("gust") var gust: Double? = null

)

data class Rain(

    @SerializedName("1h") var oneHour: Double? = null

)

data class Clouds(

    @SerializedName("all") var all: Int? = null

)

data class Sys(

    @SerializedName("type") var type: Int? = null,
    @SerializedName("id") var id: Int? = null,
    @SerializedName("country") var country: String,
    @SerializedName("sunrise") var sunrise: Int,
    @SerializedName("sunset") var sunset: Int

)


