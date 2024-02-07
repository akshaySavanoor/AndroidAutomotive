package com.akshay.weatherapp.templates

import android.location.Location
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.CarLocation
import androidx.car.app.model.ItemList
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Place
import androidx.car.app.model.PlaceListMapTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Constants
import com.akshay.weatherapp.common.RepositoryUtils
import com.akshay.weatherapp.common.RepositoryUtils.setUpObserversAndCallApi
import com.akshay.weatherapp.common.TemplateUtility.goToHome
import com.akshay.weatherapp.common.Utility
import com.akshay.weatherapp.model.WeatherResponseModel
import com.akshay.weatherapp.misc.PlaceDetailsScreen

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

class PlaceListMapExample(carContext: CarContext) : Screen(carContext),
    DefaultLifecycleObserver {

    private var weatherResponseModelData: WeatherResponseModel? = null
    private var currentLocation: Location? = null
    private var mIsLoading = true
    private var errorMessage: String? = null

    init {
        lifecycle.addObserver(this)
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

    private val currentLocationCallback: (Location) -> Unit = { location ->
        if (location != currentLocation) {
            currentLocation = location
            invalidate()
        }
    }

    override fun onGetTemplate(): Template {
        setUpObserversAndCallApi(
            carContext,
            this,
            loadingCallback,
            errorCallback,
            weatherDataCallback,
            currentLocationCallback
        )

        val placeDetailRow = Row.Builder()
        weatherResponseModelData?.let {
            placeDetailRow.addText("${it.name}  ·  ${it.sys.country}")
        }

        val onClickListener: () -> Unit = {
            weatherResponseModelData?.let {
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
            return builder.run {
                setLoading(true)
                setTitle(carContext.getString(R.string.my_location))
                setHeaderAction(Action.BACK)
                build()
            }
        }

        errorMessage?.let {
            return MessageTemplate.Builder(it).run {
                setTitle(Constants.PLACE_LIST_MAP_TEMPLATE)
                setIcon(CarIcon.ERROR)
                setHeaderAction(Action.BACK)
                setActionStrip(goToHome(carContext, this@PlaceListMapExample))
                addAction(RepositoryUtils.getRetryAction(carContext, this@PlaceListMapExample))
                build()
            }
        }

        builder.setItemList(
            ItemList.Builder()
                .addItem(
                    placeDetailRow.run {
                        setTitle(carContext.getString(R.string.browse_place_details))
                        setBrowsable(true)
                        setOnClickListener(onClickListener)
                        build()
                    }
                )
                .build()
        )
            .setTitle(carContext.getString(R.string.my_location))
            .setHeaderAction(Action.BACK)

        currentLocation?.let {
            builder.setAnchor(
                Place.Builder(CarLocation.create(it)).build()
            )
        }
        return builder.build()
    }
}
