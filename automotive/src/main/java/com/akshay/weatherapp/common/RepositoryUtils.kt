package com.akshay.weatherapp.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.core.content.ContextCompat
import com.akshay.weatherapp.R
import com.akshay.weatherapp.app_secrets.ApiKey
import com.akshay.weatherapp.model.WeatherResponseModel
import com.akshay.weatherapp.service.RetrofitInstance
import com.akshay.weatherapp.ui.MyLocationListener
import com.akshay.weatherapp.viewmodel.LocationViewModel
import retrofit2.Call

object RepositoryUtils {

    private val locationViewModel by lazy { LocationViewModel() }
    private lateinit var myLocationListener: MyLocationListener
    private var locationManager: LocationManager? = null
    private var hasPermissionLocation: Boolean = false
    private var currentLocation: Location? = null
    private var call: Call<WeatherResponseModel>? = null

    private fun isLocationChanged(newLocation: Location): Boolean {
        return currentLocation?.let { it.latitude != newLocation.latitude || it.longitude != newLocation.longitude }
            ?: true
    }

    private fun handlePermissionCallback(
        approved: List<String>,
        rejected: List<String>,
        currentScreen: Screen,
        carContext: CarContext
    ) {
        when {
            approved.isNotEmpty() -> {
                Utility.showToast(carContext, carContext.getString(R.string.approved))
                currentScreen.invalidate()
            }

            rejected.isNotEmpty() -> {
                Utility.showToast(carContext, carContext.getString(R.string.rejected))
                currentScreen.screenManager.pop()
            }
        }
    }

    fun setUpObserversAndCallApi(
        carContext: CarContext,
        currentScreen: Screen,
        loadingStatus: (Boolean) -> Unit,
        errorStatus: (String?) -> Unit,
        weatherResponseData: (WeatherResponseModel?) -> Unit,
        currentLocationData: ((Location) -> Unit)? = null
    ) {
        locationViewModel.apply {

            isLoading.observe(currentScreen) {
                loadingStatus(it)
            }

            mError.observe(currentScreen) {
                errorStatus(it)
            }

            weatherData.observe(currentScreen) { weatherResponse ->
                weatherResponseData(weatherResponse)
            }

        }

        myLocationListener = MyLocationListener { location ->
            currentLocationData?.let {
                it(location)
            }
            if (isLocationChanged(location)) {
                currentLocation = location
                call = RetrofitInstance.weatherApiService.getCurrentWeather(
                    location.latitude,
                    location.longitude,
                    ApiKey.API_KEY
                )
                call?.let { locationViewModel.fetchWeatherData(carContext, it) }
            }
        }

        locationManager = carContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasPermissionLocation =
            ContextCompat.checkSelfPermission(
                carContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        carContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) ==
                    PackageManager.PERMISSION_GRANTED

        if (hasPermissionLocation) {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                myLocationListener
            )
        } else {
            Utility.requestPermission(carContext) { approved, rejected ->
                handlePermissionCallback(approved, rejected, currentScreen, carContext)
            }
        }
    }

    fun getRetryAction(carContext: CarContext, screen: Screen): Action {
        return Action.Builder()
            .setTitle(carContext.getString(R.string.retry))
            .setFlags(Action.FLAG_PRIMARY)
            .setOnClickListener {
                locationViewModel.fetchWeatherData(
                    carContext,
                    call ?: locationViewModel.getDefaultCall()
                )
                screen.invalidate()
            }.build()
    }
}