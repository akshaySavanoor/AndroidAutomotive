package com.akshay.weatherapp.templates

import android.text.Spannable
import android.text.SpannableString
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.constraints.ConstraintManager
import androidx.car.app.model.Action
import androidx.car.app.model.Action.FLAG_PRIMARY
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Distance
import androidx.car.app.model.DistanceSpan
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.versioning.CarAppApiLevels
import androidx.core.graphics.drawable.IconCompat
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Utility.Companion.getColoredString
import com.akshay.weatherapp.common.Utility.Companion.showToast
import com.akshay.weatherapp.common.Utility.Companion.toIntent
import com.akshay.weatherapp.viewmodel.LocationViewModel

/**
 * Pane Template:
 * The Pane template is designed for presenting detailed information with prominent actions.
 * Ideal for non-editable metadata, such as location and reservation details, allowing actions based on the data.
 * For navigation apps, a version without an image and including a map can be implemented using the Map template.
 *
 * Components:
 * - Header with an optional action strip
 * - Up to 2 buttons (optional; apps can use action strip buttons instead), with one optionally designated as primary
 * - Up to 4 non-actionable rows (1 row is mandatory)
 * - Optional large image
 *
 * Usage:
 * - Highlight actions and information effectively
 * - Suitable for quick actions based on detailed data
 * - For quick messages with less detail, prefer the Message template
 * - For long messages, opt for the Long Message template
 *
 * UX Requirements:
 * App developers:
 * - MUST include at least one row of information
 * - SHOULD designate a primary action when providing 2 actions
 * - SHOULD make navigation the primary action when included as one of the actions
 * - MAY include up to 4 rows of information and 2 actions
 */

class PaneTemplateExample(carContext: CarContext) : Screen(carContext) {
    private val locationViewModel = LocationViewModel()
    private val randomPlace = locationViewModel.getLocationData()

    /**
     * CAUTION: Rows cannot have more than 2 addText lines.
     * ERROR: java.lang.IllegalArgumentException - The number of lines of texts for the row exceeded the supported max of 2.
     * Note: If two images are set, no errors will occur, but the last image will be displayed.
     *
     * CAUTION: Row's in the pane template cannot have toggle or onClick listener
     * ERROR: java.lang.IllegalArgumentException: A click listener is not allowed on the row
     *
     * Note: Text in a line won't wrap; instead, it will be ellipsized at the end.(for 2 additional text)
     */

    private fun createRow(index: Int): Row {
        val opensCloses = carContext.getString(R.string.opens_closes)
        val noPetrol = carContext.getString(R.string.no_petrol)
        val fuelIcon =
            CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_fuel)).build()

        return when (index) {
            0 -> Row.Builder()
                .run {
                    setTitle(opensCloses)
                    addText(
                        getColoredString(
                            carContext.getString(R.string.available_with_distance),
                            0,
                            9,
                            CarColor.GREEN
                        )
                    )
                    addText(SpannableString(" ").apply {
                        setSpan(
                            DistanceSpan.create(
                                Distance.create(Math.random() * 100, Distance.UNIT_KILOMETERS)
                            ), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                        )
                    })
                    setImage(fuelIcon)
                    build()

                }

            1 -> Row.Builder()
                .run {
                    setTitle(opensCloses)
                    addText(
                        getColoredString(
                            carContext.getString(R.string.closed_with_distance),
                            0,
                            6,
                            CarColor.RED
                        )
                    )
                    addText(
                        getColoredString(
                            noPetrol,
                            0,
                            noPetrol.length,
                            CarColor.SECONDARY
                        )
                    )
                    setImage(fuelIcon)
                    build()
                }

            else -> Row.Builder()
                .run {
                    setTitle(carContext.getString(R.string.gas_station, index + 1))
                    addText(carContext.getString(R.string.optional_desc_1, index + 1))
                    build()
                }
        }
    }

    override fun onGetTemplate(): Template {
        val listLimit = 4

        // If we have too many list items then we can adjust the item limit according to the car constraints.
        var adjustedListLimit = listLimit
        if (carContext.carAppApiLevel > CarAppApiLevels.LEVEL_1) {
            adjustedListLimit = carContext.getCarService(ConstraintManager::class.java)
                .getContentLimit(ConstraintManager.CONTENT_LIMIT_TYPE_PANE)
        }
        /**
         * Optionally, set a large image outside of the rows.
         * Note: If two images are set, no errors will occur, but the last image will be displayed.
         */
        val paneBuilder = Pane.Builder()
            .setImage(
                CarIcon.Builder(
                    IconCompat.createWithResource(
                        carContext,
                        R.drawable.large_image
                    )
                ).build()
            )

        for (i in 0 until adjustedListLimit) {
            paneBuilder.addRow(createRow(i))
        }

        val primaryActionBuilder = Action.Builder()
            .apply {
                setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(
                        carContext,
                        R.drawable.ic_navigation
                    )
                ).build()
            )
                setTitle(carContext.getString(R.string.navigate))
                setOnClickListener {
                    carContext.startCarApp(randomPlace.toIntent(CarContext.ACTION_NAVIGATE))
                }
            }

        if (carContext.carAppApiLevel >= CarAppApiLevels.LEVEL_4) {
            primaryActionBuilder.setFlags(FLAG_PRIMARY)
        }

        /**
         * CAUTION: The number of addAction calls on the pane should not exceed the supported maximum of 2.
         * ERROR: java.lang.IllegalArgumentException - The number of actions on the pane exceeded the supported max of 2.
         */
        paneBuilder.apply {
            addAction(primaryActionBuilder.build())
            addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.browse_place_details))
                    .setOnClickListener { screenManager.push(MapTemplateExample(carContext)) }
                    .build()
            )
        }

        val callAction = Action.Builder()
            .run {
                setTitle(carContext.getString(R.string.call))
                setIcon(
                    CarIcon.Builder(
                        IconCompat.createWithResource(carContext, R.drawable.ic_call)
                    ).run {
                        setTint(CarColor.BLUE) //Tint can be skipped by the host
                        build()
                    }
                )
                setOnClickListener {
                    showToast(carContext, carContext.getString(R.string.unable_to_call))
                }
                build()
            }

        return PaneTemplate.Builder(paneBuilder.build())
            .run {
                setHeaderAction(Action.BACK)
                setActionStrip(
                    ActionStrip.Builder()
                        .run {
                            addAction(callAction)
                            build()
                        }
                )
                setTitle(carContext.getString(R.string.pane_template))
                build()
            }
    }
}