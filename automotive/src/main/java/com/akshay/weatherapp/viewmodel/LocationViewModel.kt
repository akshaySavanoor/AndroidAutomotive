package com.akshay.weatherapp.viewmodel

import androidx.car.app.CarContext
import androidx.lifecycle.ViewModel
import com.akshay.weatherapp.app_secrets.ApiKey
import com.akshay.weatherapp.model.Location
import com.akshay.weatherapp.model.Place
import com.akshay.weatherapp.model.WeatherResponseModel
import com.akshay.weatherapp.repository.LocationRepository
import com.akshay.weatherapp.service.RetrofitInstance
import retrofit2.Call

class LocationViewModel : ViewModel() {
    private val repository = LocationRepository()

    private val _weatherData = repository.getWeatherData()
    val weatherData = _weatherData

    private val _isLoading = repository.getLoadingStatus()
    val isLoading = _isLoading

    private val _mError = repository.getErrorMessage()
    val mError = _mError

    fun getDefaultCall(): Call<WeatherResponseModel> {
        val randomLocation = repository.getRandomLocation()
        return RetrofitInstance.weatherApiService.getCurrentWeather(
            randomLocation.latitude,
            randomLocation.longitude,
            ApiKey.API_KEY
        )
    }

    fun fetchWeatherData(carContext: CarContext, call: Call<WeatherResponseModel>) {
        repository.fetchWeatherData(carContext, call)
    }

    fun getLocationData(): Location {
        return repository.getRandomLocation()
    }

    fun getGasStations(): List<Place> {
        return repository.getGasStations()
    }

    fun getAllCoordinatesData(): List<Location> {
        return repository.getAllCoordinates()
    }
}