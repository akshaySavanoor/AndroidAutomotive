package com.akshay.weatherapp.repository

import android.content.ContentValues.TAG
import android.util.Log
import androidx.annotation.StringRes
import androidx.car.app.CarContext
import androidx.lifecycle.MutableLiveData
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Utility
import com.akshay.weatherapp.model.Location
import com.akshay.weatherapp.model.Place
import com.akshay.weatherapp.model.WeatherResponseModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class LocationRepository {
    private val weatherData: MutableLiveData<WeatherResponseModel?> = MutableLiveData()
    private val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    private val mError: MutableLiveData<String> = MutableLiveData()

    private val locations: List<Location> = listOf(
        12.9716 to 77.5946,
        12.2958 to 76.6394,
        15.3647 to 75.1240,
        12.8654 to 74.8426,
        15.8497 to 74.4977,
        17.3850 to 76.8466,
        15.3350 to 76.4600,
        15.9175 to 75.6760,
        13.0072 to 76.0960,
        13.3409 to 74.7421,
        13.9299 to 75.5681,
        16.8270 to 75.7251,
        17.9226 to 77.5175,
        13.3153 to 75.7754,
        15.3647 to 75.1240,
        14.5479 to 74.3184,
        13.1364 to 78.1297,
        16.2076 to 77.3463,
        13.3409 to 77.1007,
        14.2226 to 76.3985
    ).map { (latitude, longitude) -> Location(latitude, longitude) }


    private val dummyGasStations: List<Place> = mutableListOf<Place>().apply {
        add(Place(name = "Gas Station A", distance = 1.2, isOpen = true))
        add(Place(name = "Coffee Shop B", distance = 3.5, isOpen = false))
        add(Place(name = "Restaurant C", distance = 5.8, isOpen = true))
        add(Place(name = "Grocery Store D", distance = 8.2, isOpen = true))
        add(Place(name = "Park E", distance = 10.0, isOpen = false))
        add(Place(name = "Museum X", distance = 15.5, isOpen = true))
        add(Place(name = "Bookstore Y", distance = 20.3, isOpen = false))
        add(Place(name = "Fitness Center Z", distance = 25.1, isOpen = true))
        add(Place(name = "Cinema P", distance = 30.7, isOpen = true))
        add(Place(name = "Pharmacy Q", distance = 35.2, isOpen = false))
        add(Place(name = "Bakery R", distance = 40.0, isOpen = true))
        add(Place(name = "Post Office S", distance = 45.6, isOpen = true))
        add(Place(name = "Cold gas Z", distance = 45.6, isOpen = true))
        add(Place(name = "Queens station W", distance = 45.6, isOpen = true))
        add(Place(name = "Boys station T", distance = 45.6, isOpen = true))
        add(Place(name = "Quick Gas B", distance = 45.6, isOpen = true))
        add(Place(name = "Star Gas", distance = 45.6, isOpen = true))
        add(Place(name = "Free Station", distance = 45.6, isOpen = true))
        add(Place(name = "Quick fill", distance = 45.6, isOpen = true))
        add(Place(name = "Instant fill", distance = 45.6, isOpen = true))
    }

    fun fetchWeatherData(carContext: CarContext, call: Call<WeatherResponseModel>) {
        if (!Utility.isDeviceOnLine(carContext)) {
            handleError(carContext, R.string.no_internet)
            return
        }

        isLoading.value = true
        call.clone().enqueue(object : Callback<WeatherResponseModel> {
            override fun onResponse(
                call: Call<WeatherResponseModel>,
                response: Response<WeatherResponseModel>
            ) {
                isLoading.value = false
                if (response.isSuccessful) {
                    handleSuccessfulResponse(carContext, response.body())
                } else {
                    handleError(carContext, R.string.something_went_wrong)
                    Log.i(TAG, response.message())
                }
            }

            override fun onFailure(call: Call<WeatherResponseModel>, t: Throwable) {
                t.printStackTrace()
                isLoading.value = false
                handleError(carContext, R.string.unknown_error)
            }
        })
    }

    private fun handleSuccessfulResponse(carContext: CarContext, body: WeatherResponseModel?) {
        body?.let {
            weatherData.value = body
        }?: kotlin.run {
            mError.value = carContext.getString(R.string.failed_to_fetch_weather_data)
        }
    }

    private fun handleError(carContext: CarContext, @StringRes errorMessageResId: Int) {
        mError.value = carContext.getString(errorMessageResId)
    }

    fun getWeatherData(): MutableLiveData<WeatherResponseModel?> {
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
