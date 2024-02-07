package com.akshay.weatherapp.templates

import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.ExperimentalCarApi
import androidx.car.app.model.*
import androidx.car.app.model.Action.APP_ICON
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Constants
import com.akshay.weatherapp.common.Constants.Companion.COORDINATES
import com.akshay.weatherapp.common.Constants.Companion.GRID_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.LOADING_GRID_ITEM
import com.akshay.weatherapp.common.RepositoryUtils.getRetryAction
import com.akshay.weatherapp.common.RepositoryUtils.setUpObserversAndCallApi
import com.akshay.weatherapp.common.TemplateUtility.createGenericAction
import com.akshay.weatherapp.common.TemplateUtility.createGenericActionStrip
import com.akshay.weatherapp.common.TemplateUtility.getIconCompatByResource
import com.akshay.weatherapp.common.TemplateUtility.goToHome
import com.akshay.weatherapp.common.Utility
import com.akshay.weatherapp.common.Utility.Companion.getColoredString
import com.akshay.weatherapp.model.WeatherResponseModel
import com.akshay.weatherapp.ui.WeatherDetailsScreen

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

    private var weatherResponseModelData: WeatherResponseModel? = null
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
        when (title) {
            LOADING_GRID_ITEM -> {
                return gridListBuilder.run {
                    if (!loadingActionFlag) {
                        setTitle(title) //Titles don't support colored text
                        setImage(CarIcon.Builder(icon).build())
                            .setOnClickListener {
                                handler.removeCallbacksAndMessages(null)
                                loadingActionFlag = !loadingActionFlag
                                invalidate()
                            }
                    } else {
                        setTitle(title)
                        setLoading(true)
                    }
                    build()
                }
            }

            COORDINATES -> {
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
        }
        return gridListBuilder
            .apply {
                setTitle(title) //set short title to avoid truncation of grid item title
                setImage(
                    CarIcon.Builder(icon).build(),
                    GridItem.IMAGE_TYPE_LARGE
                ) //Keep large image for reducing driver distraction
                setOnClickListener {
                    weatherResponseModelData?.let {
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

    private val loadingCallback: (Boolean) -> Unit = { isLoading ->
        mIsLoading = isLoading
        if (isLoading) {
            invalidate()
        }
    }

    private val errorCallback: (String?) -> Unit = { errorData ->
        errorMessage = errorData
        invalidate()
    }

    private val weatherDataCallback: (WeatherResponseModel?) -> Unit = { weatherResponse ->
        weatherResponseModelData = weatherResponse
        mIsLoading = false
        errorMessage = null
        invalidate()
    }

    @OptIn(ExperimentalCarApi::class)
    override fun onGetTemplate(): Template {
        setUpObserversAndCallApi(
            carContext,
            this,
            loadingCallback,
            errorCallback,
            weatherDataCallback
        )
        handler.postDelayed({
            loadingActionFlag = false
            invalidate()
        }, delayForLoader)

        val coordinatesIcon = getIconCompatByResource(R.drawable.ic_coordinate, carContext)
        val weatherIcon = getIconCompatByResource(R.drawable.ic_weather, carContext)
        val temperatureIcon = getIconCompatByResource(R.drawable.ic_temperature, carContext)
        val systemInformationIcon = getIconCompatByResource(R.drawable.ic_system, carContext)
        val windIcon = getIconCompatByResource(R.drawable.ic_wind, carContext)
        val cloudIcon = getIconCompatByResource(R.drawable.ic_clouds, carContext)
        val loaderRefreshIcon = getIconCompatByResource(R.drawable.loader_refresh, carContext)

        val gridItemCoordinates = createGridItem(
            title = COORDINATES,
            icon = coordinatesIcon
        )
        val gridItemWeather = createGridItem(
            title = Constants.WEATHER_CONDITION,
            icon = weatherIcon
        )
        val gridItemTemperature = createGridItem(
            title = Constants.TEMPERATURE,
            icon = temperatureIcon
        )
        val gridItemWind = createGridItem(
            title = Constants.WIND,
            icon = windIcon
        )
        val gridItemCloud = createGridItem(
            title = Constants.CLOUD,
            icon = cloudIcon
        )
        val gridItemSystemInfo =
            createGridItem(
                title = Constants.SYSTEM_INFORMATION,
                icon = systemInformationIcon,
            )
        val loadingGridItem = createGridItem(
            title = LOADING_GRID_ITEM,
            icon = loaderRefreshIcon
        )

        val gridBuilder = GridTemplate.Builder()
            .setActionStrip(createGenericActionStrip(createGenericAction(
                title = carContext.getString(R.string.exit),
                onClickListener = OnClickListener { screenManager.pop() }
            ))
            ) // Action buttons (up to 2, except for templates with maps, which allow up to 4)

        if (mIsLoading) {
            return gridBuilder
                .run {
                    setLoading(true)
                    setHeaderAction(Action.BACK)
                    setTitle(GRID_TEMPLATE)
                    build()
                }
        }

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
                .run {
                    setTitle(GRID_TEMPLATE)
                    setIcon(CarIcon.ERROR)
                    setHeaderAction(Action.BACK)
                    setActionStrip(
                        goToHome(
                            carContext = carContext,
                            screen = this@GridTemplateExample
                        )
                    )
                    addAction(
                        getRetryAction(
                            carContext = carContext,
                            screen = this@GridTemplateExample
                        )
                    )
                    build()
                }
        }

        return gridBuilder
            /**
             * This action will be displayed as a floating action button.
             * Should contain supported Action types, or valid CarIcon and background CarColor,
             * should not exceed the maximum number of allowed actions for the template.
             */
            .run {
                setHeaderAction(APP_ICON)
                setSingleList(gridList)
                setTitle(GRID_TEMPLATE)
                setHeaderAction(Action.BACK) // Optional header action
                // Action strip with an action button
                build()
            }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        handler.removeCallbacksAndMessages(null)
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