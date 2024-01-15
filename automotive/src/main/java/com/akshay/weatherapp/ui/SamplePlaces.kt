package com.akshay.weatherapp.ui

import android.text.SpannableString
import android.text.Spanned
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.constraints.ConstraintManager
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarColor.BLUE
import androidx.car.app.model.CarColor.GREEN
import androidx.car.app.model.CarColor.PRIMARY
import androidx.car.app.model.CarColor.RED
import androidx.car.app.model.CarColor.SECONDARY
import androidx.car.app.model.CarColor.YELLOW
import androidx.car.app.model.CarIcon
import androidx.car.app.model.CarLocation
import androidx.car.app.model.Distance
import androidx.car.app.model.DistanceSpan
import androidx.car.app.model.ForegroundCarColorSpan
import androidx.car.app.model.ItemList
import androidx.car.app.model.Metadata
import androidx.car.app.model.Place
import androidx.car.app.model.PlaceMarker
import androidx.car.app.model.Row
import androidx.car.app.versioning.CarAppApiLevels
import androidx.core.graphics.drawable.IconCompat
import com.akshay.weatherapp.R
import com.akshay.weatherapp.model.Location
import com.akshay.weatherapp.common.Utility.Companion.toIntent
import com.akshay.weatherapp.viewmodel.WeatherViewModel
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class SamplePlaces(placeDataScreen: Screen) {

    private val placesViewModel = WeatherViewModel()
    private val placeCoordinates = placesViewModel.getAllCoordinatesData()
    private val gasStationData = placesViewModel.getGasStations()
    private val currentContext: CarContext = placeDataScreen.carContext

    companion object {
        fun create(placeDataScreen: Screen): SamplePlaces {
            return SamplePlaces(placeDataScreen)
        }
    }

    fun getPlaceList(): ItemList {
        val itemListBuilder = ItemList.Builder()
            .setNoItemsMessage(currentContext.getString(R.string.no_places_to_show))

        val listLimit = calculateListLimit()

        for (index in 0 until listLimit) {
            val markerIconColor = listOf(RED, YELLOW, GREEN, PRIMARY, SECONDARY)
            val randomIndex = Random.nextInt(markerIconColor.size)
            itemListBuilder.addItem(
                createLocationData(
                    gasStationData[index],
                    placeCoordinates[index],
                    markerIconColor[randomIndex]
                )
            )
        }

        return itemListBuilder.build()
    }

    private fun calculateListLimit(): Int {
        var listLimit = 6
        if (currentContext.carAppApiLevel > CarAppApiLevels.LEVEL_1) {
            listLimit =
                max(
                    listLimit,
                    currentContext.getCarService(ConstraintManager::class.java)
                        .getContentLimit(ConstraintManager.CONTENT_LIMIT_TYPE_LIST)
                )
        }
        return min(listLimit, gasStationData.size)
    }

    private fun createLocationData(
        currentPlace: com.akshay.weatherapp.model.Place,
        location: Location,
        carColor: CarColor
    ): Row {
        val status = currentContext.getString(
            if (currentPlace.isOpen) R.string.available else R.string.closed
        )

        val statusWithDesc =
            SpannableString(currentContext.getString(R.string.distance_with_availability, status))
        statusWithDesc.setSpan(
            DistanceSpan.create(
                Distance.create(
                    currentPlace.distance,
                    Distance.UNIT_KILOMETERS
                )
            ),
            0,
            1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        statusWithDesc.setSpan(
            ForegroundCarColorSpan.create(BLUE),
            0,
            1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val colorSpanStart = if (currentPlace.isOpen) 2 else 2
        val colorSpanEnd = if (currentPlace.isOpen) 11 else 8

        val colorSpan =
            ForegroundCarColorSpan.create(if (currentPlace.isOpen) GREEN else RED)

        statusWithDesc.setSpan(
            colorSpan,
            colorSpanStart,
            colorSpanEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return Row.Builder()
            .setTitle(currentPlace.name)
            .addText(statusWithDesc)
            .setOnClickListener {
                currentContext.startCarApp(location.toIntent(CarContext.ACTION_NAVIGATE))
            }
            .setBrowsable(true)
            .setMetadata(createPlaceMetadata(location, carColor))
            .build()
    }

    private fun createPlaceMetadata(location: Location, carColor: CarColor): Metadata {
        return Metadata.Builder()
            .setPlace(
                Place.Builder(CarLocation.create(location.latitude, location.longitude))
                    .setMarker(
                        PlaceMarker.Builder()
                            .setIcon(
                                CarIcon.Builder(
                                    IconCompat.createWithResource(
                                        currentContext,
                                        R.drawable.ic_fuel
                                    )
                                )
                                    .setTint(carColor)
                                    .build(),
                                PlaceMarker.TYPE_IMAGE
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }
}



