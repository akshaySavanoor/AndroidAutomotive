package com.akshay.weatherapp.templates

import android.content.Intent
import android.provider.Settings
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.OnClickListener
import androidx.car.app.model.PlaceListMapTemplate
import androidx.car.app.model.Template
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Constants.Companion.FAVOURITE
import com.akshay.weatherapp.common.Constants.Companion.SEARCH
import com.akshay.weatherapp.common.Constants.Companion.SETTINGS
import com.akshay.weatherapp.common.TemplateUtility.createGenericAction
import com.akshay.weatherapp.common.TemplateUtility.getIconByResource
import com.akshay.weatherapp.common.Utility.Companion.showErrorMessage
import com.akshay.weatherapp.misc.SamplePlaces

/**
 * The **Place List template** is designed to present an ordered list of locations, augmented by a map
 * provided by the app library. This template is tailored for point-of-interest apps and showcases
 * locations with optional refresh functionality in the header.
 * Note: For navigation apps, an alternate template, Place List (navigation), is recommended.
 *
 * **Includes:**
 * - Header (in card) with an optional refresh button for users to request a list update (doesn't
 *   add to step count).
 * - Action strip (optional).
 * - Base map (full-screen, not drawn by apps).
 * - List rows within specified limits.
 * - Markers:
 *   - Map marker: On the map, labeled with text (up to 3 letters), an icon, or an image.
 *   - List marker (not shown): Corresponds to the map marker in the list with matching metadata
 *     and image or icon asset.
 *   - Anchor marker (optional): On the map, used to show the center of the search area.
 *
 * Apps can customize the background color of markers with any color. The color used for the map marker
 * is applied to the list marker.
 *
 * Place List (map) template UX requirements for app developers:
 * - MUST show duration or distance for each list item (except for container items).
 * - MUST associate an action with each list row; information-only rows are not allowed.
 * - MUST display only locations appropriate to the app type (e.g., parking spots for parking apps,
 *   charging stations for charging apps).
 * - SHOULD include at least one location or browsable list item (container) on the list.
 * - SHOULD show a corresponding marker on the map for each location on the list.
 * - SHOULD limit locations to those that are closest or most relevant.
 * - SHOULD consider supporting content refresh for the list, allowing users to update it after
 *   driving out of range of the original list.
 */

class MapTemplateExample(carContext: CarContext) : Screen(carContext) {
    private val samplePlaces = SamplePlaces.create(this)
    private var favouriteFlag = false

    override fun onGetTemplate(): Template {
        // Use icons (filled and unfilled) to ensure compatibility with color restrictions on the host.
        val starIcon = if (favouriteFlag) {
            R.drawable.ic_favorite_filled_white_24dp
        } else {
            R.drawable.ic_favorite_white_24dp
        }
        val actionStrip = ActionStrip.Builder()
            .run {
                addAction(
                    createGenericAction(title = carContext.getString(R.string.locations),
                        onClickListener = OnClickListener {
                            showErrorMessage(
                                carContext = carContext,
                                message = carContext.getString(R.string.locations)
                            )
                        })
                )
                addAction(createActionWithIcon(carContext, R.drawable.ic_search, SEARCH))
                addAction(createActionWithIcon(carContext, R.drawable.ic_settings, SETTINGS))
                addAction(createActionWithIcon(carContext, starIcon, FAVOURITE))
                build()
            }

        /**
         * - If you set `setCurrentLocationEnabled` to true, the app must have either the
         *   `ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION` permission.
         * - To display the anchor, it is necessary to set the anchor with location coordinates.
         * - Map templates can have a maximum of 4 action stripes.
         * - `setOnContentRefreshListener` will set a refresh icon at the top of the item list.
         *
         * If you are working on the Map template, ensure that you include the following permission
         * in your AndroidManifest.xml file: <uses-permission android:name="androidx.car.app.MAP_TEMPLATES" />
         *
         */
        return PlaceListMapTemplate.Builder()
            .run {
                setActionStrip(actionStrip)
//            .setCurrentLocationEnabled(true)
                setItemList(samplePlaces.getPlaceList())
                setOnContentRefreshListener {
                    showErrorMessage(
                        carContext,
                        carContext.getString(R.string.gas_station_info_updated)
                    )
                    invalidate()
                }
                setTitle(carContext.getString(R.string.gas_stations))
                setHeaderAction(Action.BACK)
                build()
            }
    }

    private fun createActionWithIcon(
        carContext: CarContext,
        iconResId: Int,
        iconType: String
    ): Action {
        val actionBuilder = Action.Builder()


        return actionBuilder
            .setOnClickListener {
                when (iconType) {
                    SEARCH -> screenManager.push(SearchTemplateExample(carContext))
                    SETTINGS -> openLocationSettings()
                    FAVOURITE -> {
                        favouriteFlag = !favouriteFlag
                        val toastMessage = if (favouriteFlag) {
                            carContext.getString(R.string.added_to_favourites)
                        } else {
                            carContext.getString(R.string.removed_from_favourites)
                        }
                        showErrorMessage(carContext, toastMessage)
                        invalidate()
                    }

                    else -> {
                        showErrorMessage(
                            carContext,
                            carContext.getString(R.string.something_went_wrong)
                        )
                    }
                }
            }
            .setIcon(
                getIconByResource(
                    icon = iconResId,
                    carContext = carContext
                )
            )
            .build()
    }

    private fun openLocationSettings() {
        val locationSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        locationSettingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        carContext.startActivity(locationSettingsIntent)
    }
}
