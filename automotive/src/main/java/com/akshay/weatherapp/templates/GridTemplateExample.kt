package com.akshay.weatherapp.templates

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.ExperimentalCarApi
import androidx.car.app.model.*
import androidx.car.app.model.Action.APP_ICON
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.akshay.weatherapp.R
import com.akshay.weatherapp.app_secrets.ApiKey
import com.akshay.weatherapp.common.Constants
import com.akshay.weatherapp.common.Constants.Companion.COORDINATES
import com.akshay.weatherapp.common.Constants.Companion.GRID_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.LOADING_GRID_ITEM
import com.akshay.weatherapp.common.Utility
import com.akshay.weatherapp.common.Utility.Companion.getColoredString
import com.akshay.weatherapp.common.Utility.Companion.goToHome
import com.akshay.weatherapp.common.Utility.Companion.printBuildTypeUsingReflection
import com.akshay.weatherapp.model.WeatherResponse
import com.akshay.weatherapp.service.RetrofitInstance
import com.akshay.weatherapp.ui.MyLocationListener
import com.akshay.weatherapp.ui.WeatherDetailsScreen
import com.akshay.weatherapp.viewmodel.WeatherViewModel
import retrofit2.Call

/**
 * Displays a grid layout with items suitable for users relying primarily on images for selections.
 *
 * Includes:
 * - Header with an optional action strip.
 * - Grid items with icons or large-size images.
 * - Primary text (mandatory) and optional secondary text for each grid item.
 * - Optional floating action button.
 *
 * Note: There is a limit on the number of grid items (not for less than 6). Use ConstraintManager API
 * to retrieve the specific item limit for a given vehicle.
 *
 * primaryTextLengthLimit Limit for the length of primary text to avoid truncation.
 * secondaryTextLengthLimit Limit for the length of secondary text to avoid truncation.
 *
 * UX Requirements:
 * - Developers SHOULD limit the length of primary and secondary text strings.
 * - Developers SHOULD have an action associated with each grid item.
 * - Developers SHOULD clearly indicate item state (e.g., selected/unselected) through variations in image, icon, or text.
 * - Developers SHOULD NOT include both an [action strip](https://developers.google.com/cars/design/create-apps/apps-for-drivers/components/action-strip) and a [floating action button](https://developers.google.com/cars/design/create-apps/apps-for-drivers/components/fab) simultaneously.
 * - Developers MAY show a loading spinner instead of the icon or image for a grid item during an associated action in progress.
 *
 * [ConstraintManager](https://developer.android.com/training/cars/apps#constraint-manager) API for retrieving specific item limits for a given vehicle.
 */
class GridTemplateExample(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {
    private val exitAction = Action.Builder()
        .setTitle(carContext.getString(R.string.exit))
        .setOnClickListener {
            carContext.finishCarApp()
        }
        .build()

    private val weatherViewModel = WeatherViewModel()
    private var weatherResponseData: WeatherResponse? = null

    private lateinit var myLocationListener: MyLocationListener
    private var locationManager: LocationManager? = null
    private var hasPermissionLocation: Boolean = false
    private var currentLocation: Location? = null
    private var call: Call<WeatherResponse>? = null

    private var mIsLoading = true
    private var errorMessage: String? = null
    private var loadingActionFlag = true
    private val delayForLoader = 3000L
    private val handler = Handler(Looper.getMainLooper())

    init {
        lifecycle.addObserver(this)
    }
    /**
     * Represents a grid item with an image and an optional title.
     *
     * Note: There is a limit on the number of grid items allowed to be shown, but the limit will not be less than 6 and may be higher in some vehicles.
     * To retrieve the item limit for a given vehicle refer constraintManager
     */
    private fun createGridItem(title: String, icon: IconCompat): GridItem {
        val gridListBuilder = GridItem.Builder()
        //We can add only one extra text to the grid item even if we add multiple text it will be ignored.
        //we can add loader to the grid items.
        if (title == LOADING_GRID_ITEM && !loadingActionFlag) {
            return gridListBuilder.apply {
                setTitle(title) //Titles doesn't support colored text
                setImage(CarIcon.Builder(icon).build())
                    .setOnClickListener {
                        handler.removeCallbacksAndMessages(null)
                        loadingActionFlag = !loadingActionFlag
                        invalidate()
                    }
            }.build()
        } else if (title == LOADING_GRID_ITEM) {
            return gridListBuilder.apply {
                setTitle(title)
                setLoading(true)
            }.build()
        }
        if (title == COORDINATES) {
            gridListBuilder
                .setText(
                    getColoredString(
                        carContext.getString(R.string.lon_lat),
                        0,
                        8,
                        CarColor.GREEN
                    )
                )
        }
        return gridListBuilder
            .apply {
                setTitle(title) //set short title to avoid truncation of grid item title
                setImage(
                    CarIcon.Builder(icon).build(),
                    GridItem.IMAGE_TYPE_LARGE
                ) //Keep large image for reducing driver distraction
                setOnClickListener {
                    weatherResponseData?.let {
                        screenManager.push(WeatherDetailsScreen(carContext, it, title))

                    } ?: run {
                        Utility.showErrorMessage(
                            carContext,
                            carContext.getString(R.string.failed_to_fetch_weather_data)
                        )
                    }
                } // Don't use grid item for showing only information
            }
            .build()
    }

    private fun setUpObserversAndCallApi() {
        weatherViewModel.apply {

            isLoading.observe(this@GridTemplateExample) {
                mIsLoading = it
                if (it) {
                    invalidate()
                }
            }

            mError.observe(this@GridTemplateExample) {
                errorMessage = it
                invalidate()
            }

            weatherData.observe(this@GridTemplateExample) { weatherResponse ->
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

    @OptIn(ExperimentalCarApi::class)
    override fun onGetTemplate(): Template {
        printBuildTypeUsingReflection()
        setUpObserversAndCallApi()
        handler.postDelayed({
            loadingActionFlag = false
            invalidate()
        }, delayForLoader)

        val coordinatesIcon = IconCompat.createWithResource(carContext, R.drawable.ic_coordinate)
        val weatherIcon = IconCompat.createWithResource(carContext, R.drawable.ic_weather)
        val temperatureIcon = IconCompat.createWithResource(carContext, R.drawable.ic_temperature)
        val systemInformationIcon = IconCompat.createWithResource(carContext, R.drawable.ic_system)
        val windIcon = IconCompat.createWithResource(carContext, R.drawable.ic_wind)
        val cloudIcon = IconCompat.createWithResource(carContext, R.drawable.ic_clouds)
        val loaderRefreshIcon = IconCompat.createWithResource(carContext, R.drawable.loader_refresh)

        val gridItemCoordinates = createGridItem(
            COORDINATES, coordinatesIcon
        )
        val gridItemWeather = createGridItem(
            Constants.WEATHER_CONDITION, weatherIcon
        )
        val gridItemTemperature = createGridItem(
            Constants.TEMPERATURE, temperatureIcon
        )
        val gridItemWind = createGridItem(Constants.WIND, windIcon)
        val gridItemCloud = createGridItem(Constants.CLOUD, cloudIcon)
        val gridItemSystemInfo =
            createGridItem(
                Constants.SYSTEM_INFORMATION, systemInformationIcon,
            )
        val loadingGridItem = createGridItem(LOADING_GRID_ITEM, loaderRefreshIcon)

        val gridBuilder = GridTemplate.Builder()
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(exitAction)
                    .build()
            ) // Action buttons (up to 2, except for templates with maps, which allow up to 4)

        if (mIsLoading) {
            return gridBuilder
                .setLoading(true)
                .setHeaderAction(Action.BACK)
                .setTitle(GRID_TEMPLATE)
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

        val gridList = ItemList.Builder()
            .apply {
                addItem(gridItemCoordinates)
                addItem(gridItemWeather)
                addItem(gridItemTemperature)
                addItem(gridItemCloud)
                addItem(gridItemWind)
                addItem(gridItemSystemInfo)
                addItem(loadingGridItem)
            }
            .build()

        errorMessage?.let {
            return MessageTemplate.Builder(it)
                .setTitle(GRID_TEMPLATE)
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

        return gridBuilder
            /**
             * This action will be displayed as a floating action button.
             * Should contain supported Action types, or valid CarIcon and background CarColor,
             * should not exceed the maximum number of allowed actions for the template.
             */
            .setHeaderAction(APP_ICON)
            .setSingleList(gridList)
            .setTitle(GRID_TEMPLATE)
            .setHeaderAction(Action.BACK) // Optional header action
            // Action strip with an action button
            .build()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        handler.removeCallbacksAndMessages(null)
    }
}