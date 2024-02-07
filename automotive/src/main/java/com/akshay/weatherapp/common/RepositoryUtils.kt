package com.akshay.weatherapp.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Action.FLAG_PRIMARY
import androidx.car.app.model.OnClickListener
import androidx.core.content.ContextCompat
import com.akshay.weatherapp.R
import com.akshay.weatherapp.app_secrets.ApiKey
import com.akshay.weatherapp.common.TemplateUtility.createGenericAction
import com.akshay.weatherapp.common.Utility.Companion.showErrorMessage
import com.akshay.weatherapp.model.WeatherResponseModel
import com.akshay.weatherapp.service.RetrofitInstance
import com.akshay.weatherapp.misc.MyLocationListener
import com.akshay.weatherapp.viewmodel.LocationViewModel
import retrofit2.Call

object RepositoryUtils {

    private val locationViewModel by lazy { LocationViewModel() }
    private var currentLocation: Location? = null
    private var call: Call<WeatherResponseModel>? = null

    private fun isLocationChanged(newLocation: Location): Boolean {
        return currentLocation?.run { latitude != newLocation.latitude || longitude != newLocation.longitude }
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
                showErrorMessage(carContext, carContext.getString(R.string.approved))
                currentScreen.invalidate()
            }

            rejected.isNotEmpty() -> {
                showErrorMessage(carContext, carContext.getString(R.string.rejected))
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
        val onLoadingStatusChanged: (Boolean) -> Unit = loadingStatus
        val onErrorStatusChanged: (String?) -> Unit = errorStatus
        val onWeatherDataReceived: (WeatherResponseModel?) -> Unit = weatherResponseData
        val onCurrentLocationReceived: ((Location) -> Unit)? = currentLocationData

        locationViewModel.apply {
            isLoading.observe(currentScreen, onLoadingStatusChanged)

            mError.observe(currentScreen, onErrorStatusChanged)

            weatherData.observe(currentScreen, onWeatherDataReceived)
        }

        val locationManager =
            carContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val hasPermissionLocation = ContextCompat.checkSelfPermission(
            carContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    carContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (hasPermissionLocation) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                MyLocationListener { location ->
                    onCurrentLocationReceived?.invoke(location)

                    if (isLocationChanged(location)) {
                        currentLocation = location
                        val call = RetrofitInstance.weatherApiService.getCurrentWeather(
                            location.latitude,
                            location.longitude,
                            ApiKey.API_KEY
                        )
                        call.let { locationViewModel.fetchWeatherData(carContext, it) }
                    }
                }
            )
        } else {
            Utility.requestPermission(carContext) { approved, rejected ->
                handlePermissionCallback(approved, rejected, currentScreen, carContext)
            }
        }
    }

    fun getRetryAction(carContext: CarContext, screen: Screen): Action {
        return createGenericAction(
            title = carContext.getString(R.string.retry),
            flag = FLAG_PRIMARY,
            onClickListener = OnClickListener {
                locationViewModel.fetchWeatherData(
                    carContext,
                    call ?: locationViewModel.getDefaultCall()
                )
                screen.invalidate()
            }
        )
    }
}