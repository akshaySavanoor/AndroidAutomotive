package com.akshay.weatherapp.templates

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.CarLocation
import androidx.car.app.model.ItemList
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Place
import androidx.car.app.model.PlaceListMapTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.akshay.weatherapp.HomeScreen
import com.akshay.weatherapp.R
import com.akshay.weatherapp.app_secrets.ApiKey
import com.akshay.weatherapp.common.Constants.Companion.NAVIGATION_TEMPLATE
import com.akshay.weatherapp.common.Utility
import com.akshay.weatherapp.model.WeatherResponse
import com.akshay.weatherapp.service.RetrofitInstance
import com.akshay.weatherapp.ui.MyLocationListener
import com.akshay.weatherapp.ui.PlaceDetailsScreen
import com.akshay.weatherapp.ui.RequestPermissionScreen
import com.akshay.weatherapp.viewmodel.WeatherViewModel
import retrofit2.Call


class NavigationTemplateExample(carContext: CarContext) : Screen(carContext),
    DefaultLifecycleObserver {

    private val weatherViewModel = WeatherViewModel()
    private var weatherResponseData: WeatherResponse? = null

    private lateinit var myLocationListener: MyLocationListener
    private var locationManager: LocationManager? = null
    private var hasPermissionLocation: Boolean = false
    private var currentLocation: Location? = null
    private var call: Call<WeatherResponse>? = null

    private var mIsLoading = true
    private var errorMessage: String? = null

    init {
        lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        weatherViewModel.apply {
            weatherData.observe(this@NavigationTemplateExample) { weatherResponse ->
                weatherResponseData = weatherResponse
                invalidate()
            }

            isLoading.observe(this@NavigationTemplateExample) {
                mIsLoading = it
                if (it) {
                    invalidate()
                }
            }
            mError.observe(this@NavigationTemplateExample) {
                errorMessage = it
                invalidate()
            }
        }

        myLocationListener = MyLocationListener { location ->
            if (location.latitude != currentLocation?.latitude && location.longitude != currentLocation?.longitude) {
                currentLocation = location
                call =
                    RetrofitInstance.weatherApiService.getCurrentWeather(
                        location.latitude,
                        location.longitude,
                        ApiKey.API_KEY
                    )
                weatherViewModel.fetchWeatherData(
                    carContext,
                    call ?: weatherViewModel.getDefaultCall()
                )
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
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                myLocationListener
            )
        } else {
            screenManager.push(RequestPermissionScreen(carContext, currentScreen = NAVIGATION_TEMPLATE))
        }
    }

    override fun onGetTemplate(): Template {

        val placeDetailRow = Row.Builder()
        weatherResponseData?.let {
            placeDetailRow.addText("${it.name}  ·  ${it.sys.country}")
        }

        val onClickListener: () -> Unit = {
            weatherResponseData?.let {
                screenManager.push(PlaceDetailsScreen(carContext, it))
            } ?: run {
                Utility.showErrorMessage(
                    carContext,
                    carContext.getString(R.string.loading_status)
                )
            }
        }

        val builder = PlaceListMapTemplate.Builder()
        if (mIsLoading) {
            return builder
                .setLoading(true)
                .setTitle(carContext.getString(R.string.my_location))
                .setHeaderAction(Action.BACK)
                .build()
        }

        val goToHome = Action.Builder()
            .setTitle(carContext.getString(R.string.home))
            .setOnClickListener {
                screenManager.push(HomeScreen(carContext))
            }
            .build()

        val retryAction = Action.Builder()
            .setTitle(carContext.getString(R.string.retry))
            .setFlags(Action.FLAG_PRIMARY)
            .setOnClickListener {
                weatherViewModel.fetchWeatherData(
                    carContext,
                    call ?: weatherViewModel.getDefaultCall()
                )
                mIsLoading = true
                invalidate()
            }.build()

        errorMessage?.let {
            return MessageTemplate.Builder(it)
                .setTitle(NAVIGATION_TEMPLATE)
                .setIcon(CarIcon.ERROR)
                .setHeaderAction(Action.BACK)
                .setActionStrip(
                    ActionStrip.Builder()
                        .addAction(goToHome)
                        .build()
                )
                .addAction(
                    retryAction
                )
                .build()
        }

        builder.setItemList(
            ItemList.Builder()
                .addItem(
                    placeDetailRow
                        .setTitle(carContext.getString(R.string.browse_place_details))
                        .setBrowsable(true)
                        .setOnClickListener(onClickListener)
                        .build()
                )
                .build()
        )
            .setTitle(carContext.getString(R.string.my_location))
            .setHeaderAction(Action.BACK)
            .setCurrentLocationEnabled(hasPermissionLocation)

        currentLocation?.let {
            builder.setAnchor(
                Place.Builder(CarLocation.create(it)).build()
            )
        }

        return builder.build()
    }
}
