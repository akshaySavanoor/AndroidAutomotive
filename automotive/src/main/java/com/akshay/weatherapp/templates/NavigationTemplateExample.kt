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
import com.akshay.weatherapp.R
import com.akshay.weatherapp.app_secrets.ApiKey
import com.akshay.weatherapp.common.Constants.Companion.NAVIGATION_TEMPLATE
import com.akshay.weatherapp.common.Utility
import com.akshay.weatherapp.common.Utility.Companion.goToHome
import com.akshay.weatherapp.model.WeatherResponse
import com.akshay.weatherapp.service.RetrofitInstance
import com.akshay.weatherapp.ui.MyLocationListener
import com.akshay.weatherapp.ui.PlaceDetailsScreen
import com.akshay.weatherapp.viewmodel.WeatherViewModel
import retrofit2.Call

/**
 * The Place List template is designed for navigation apps and presents an ordered list
 * of locations overlaid on a full-screen map drawn by the app. This template is distinct
 * from the Place List (map) variant tailored for navigation apps.
 *
 * Includes:
 * - Header (in card) with an optional refresh button for users to request a list update
 *   (Note: Refresh button action does not contribute to step count).
 * - Action strip (optional).
 * - Base map (full-screen, not drawn by apps).
 * - Optional map action strip with up to 4 buttons for map interactivity.
 * - List rows within defined limits.
 * - Markers linking list items with map locations.
 *
 * Place List (navigation) template UX requirements for app developers:
 *
 * MUST:
 * - Show duration or distance for each list item (except for container items).
 * - Associate an action with each list row; information-only rows are not allowed.
 *
 * SHOULD:
 * - Include at least one location or browsable (container).
 * - Include only information relevant to app capabilities, avoiding unrelated data like
 *   "favorite friends."
 * - Limit locations to those that are closest or most relevant.
 * - Show a corresponding marker on the map for each location on the list.
 * - Use a font size of at least 24dp Roboto or an equivalent for map markers.
 * - Consider supporting content refresh for the list when supporting map interactions.
 *
 * Developers are encouraged to adhere to these guidelines to ensure a consistent and
 * user-friendly experience within the Place List (navigation) template.
 */

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

    private fun setUpObserversAndCallApi() {
        weatherViewModel.apply {

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

            weatherData.observe(this@NavigationTemplateExample) { weatherResponse ->
                weatherResponseData = weatherResponse
                errorMessage = null
                mIsLoading = false
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
            Utility.requestPermission(carContext) { approved, rejected ->
                if (approved.isNotEmpty()) {
                    Utility.showToast(carContext, carContext.getString(R.string.approved))
                    invalidate()

                } else if (rejected.isNotEmpty()) {
                    Utility.showToast(carContext, carContext.getString(R.string.rejected))
                    screenManager.pop()
                }
            }
        }
    }

    override fun onGetTemplate(): Template {
        setUpObserversAndCallApi()

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

        val retryAction = Action.Builder()
            .setTitle(carContext.getString(R.string.retry))
            .setFlags(Action.FLAG_PRIMARY)
            .setOnClickListener {
                weatherViewModel.fetchWeatherData(
                    carContext,
                    call ?: weatherViewModel.getDefaultCall()
                )
                invalidate()
            }.build()

        errorMessage?.let {
            return MessageTemplate.Builder(it)
                .setTitle(NAVIGATION_TEMPLATE)
                .setIcon(CarIcon.ERROR)
                .setHeaderAction(Action.BACK)
                .setActionStrip(
                    ActionStrip.Builder()
                        .addAction(goToHome(carContext, this))
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
