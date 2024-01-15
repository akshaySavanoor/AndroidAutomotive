package com.akshay.weatherapp.repository

import android.content.ContentValues.TAG
import android.util.Log
import androidx.car.app.CarContext
import androidx.lifecycle.MutableLiveData
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Utility
import com.akshay.weatherapp.common.Utility.Companion.showErrorMessage
import com.akshay.weatherapp.model.Location
import com.akshay.weatherapp.model.Place
import com.akshay.weatherapp.model.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class WeatherRepository {
    private val weatherData: MutableLiveData<WeatherResponse> = MutableLiveData()
    private val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    private val mError: MutableLiveData<String> = MutableLiveData()

    private val locations = listOf(
        Location(12.9716, 77.5946),   // Bangalore
        Location(12.2958, 76.6394),   // Mysore
        Location(15.3647, 75.1240),   // Hubli
        Location(12.8654, 74.8426),   // Mangalore
        Location(15.8497, 74.4977),   // Belgaum
        Location(17.3850, 76.8466),   // Gulbarga
        Location(15.3350, 76.4600),   // Hampi
        Location(15.9175, 75.6760),   // Badami
        Location(13.0072, 76.0960),   // Hassan
        Location(13.3409, 74.7421),   // Udupi
        Location(13.9299, 75.5681),   // Shimoga
        Location(16.8270, 75.7251),   // Bijapur
        Location(17.9226, 77.5175),   // Bidar
        Location(13.3153, 75.7754),   // Chikmagalur
        Location(15.3647, 75.1240),   // Dharwad
        Location(14.5479, 74.3184),   // Gokarna
        Location(13.1364, 78.1297),   // Kolar
        Location(16.2076, 77.3463),   // Raichur
        Location(13.3409, 77.1007),   // Tumkur
        Location(14.2226, 76.3985)    // Chitradurga
    )

    private val dummyGasStations: List<Place> = listOf(
        Place(name = "Gas Station A", distance = 1.2, isOpen = true),
        Place(name = "Coffee Shop B", distance = 3.5, isOpen = false),
        Place(name = "Restaurant C", distance = 5.8, isOpen = true),
        Place(name = "Grocery Store D", distance = 8.2, isOpen = true),
        Place(name = "Park E", distance = 10.0, isOpen = false),
        Place(name = "Museum X", distance = 15.5, isOpen = true),
        Place(name = "Bookstore Y", distance = 20.3, isOpen = false),
        Place(name = "Fitness Center Z", distance = 25.1, isOpen = true),
        Place(name = "Cinema P", distance = 30.7, isOpen = true),
        Place(name = "Pharmacy Q", distance = 35.2, isOpen = false),
        Place(name = "Bakery R", distance = 40.0, isOpen = true),
        Place(name = "Post Office S", distance = 45.6, isOpen = true),
        Place(name = "Cold gas Z", distance = 45.6, isOpen = true),
        Place(name = "Queens station W", distance = 45.6, isOpen = true),
        Place(name = "Boys station T", distance = 45.6, isOpen = true),
        Place(name = "Quick Gas B", distance = 45.6, isOpen = true),
        Place(name = "Star Gas", distance = 45.6, isOpen = true),
        Place(name = "Free Station", distance = 45.6, isOpen = true),
        Place(name = "Quick fill", distance = 45.6, isOpen = true),
        Place(name = "Instant fill", distance = 45.6, isOpen = true),
    )

    fun fetchWeatherData(carContext: CarContext, call: Call<WeatherResponse>) {
        if (!Utility.isDeviceOnLine(carContext)) {
            isLoading.value = false
            mError.value = carContext.getString(R.string.no_internet)
            return
        }

        isLoading.value = true
        call.clone().enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                isLoading.value = false
                try {
                    if (response.isSuccessful) {
                        if (response.body() == null) {
                            mError.value =
                                carContext.getString(R.string.failed_to_fetch_weather_data)
                            return
                        }
                        weatherData.value = response.body()
                    } else {
                        response.message().let { errorMessage ->
                            mError.value = carContext.getString(R.string.something_went_wrong)
                            Log.i(TAG, errorMessage)
                        }
                        showErrorMessage(carContext)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mError.value = carContext.getString(R.string.something_went_wrong)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                t.message?.let {
                    mError.value = carContext.getString(R.string.unknown_error)
                    isLoading.value = false
                    Log.e(TAG, it)
                }
                t.printStackTrace()
                showErrorMessage(carContext)
            }
        })
    }

    fun getWeatherData(): MutableLiveData<WeatherResponse> {
        return weatherData
    }

    fun getRandomLocation(): Location {
        val randomIndex = Random.nextInt(locations.size)
        return locations[randomIndex]
    }

    fun getGasStations(): List<Place> {
        return dummyGasStations
    }

    fun getAllCoordinates(): List<Location> {
        return locations
    }

    fun getLoadingStatus(): MutableLiveData<Boolean> {
        return isLoading
    }

    fun getErrorMessage(): MutableLiveData<String> {
        return mError
    }
}
