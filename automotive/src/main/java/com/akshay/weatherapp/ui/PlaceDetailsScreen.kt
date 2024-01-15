package com.akshay.weatherapp.ui

import android.text.SpannableString
import android.text.Spanned
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Action.FLAG_PRIMARY
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarColor.PRIMARY
import androidx.car.app.model.CarColor.SECONDARY
import androidx.car.app.model.CarIcon
import androidx.car.app.model.ForegroundCarColorSpan
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import com.akshay.weatherapp.R
import com.akshay.weatherapp.model.WeatherResponse
import com.akshay.weatherapp.common.Constants.Companion.COORDINATES
import com.akshay.weatherapp.common.Utility.Companion.toIntent
import com.akshay.weatherapp.viewmodel.WeatherViewModel

class PlaceDetailsScreen(carContext: CarContext, private val weatherResponse: WeatherResponse) :
    Screen(carContext) {
    private val placesViewModel = WeatherViewModel()

    override fun onGetTemplate(): Template {
        val place = placesViewModel.getLocationData()

        val navigateAction = Action.Builder()
            .setTitle(carContext.getString(R.string.navigate))
            .setFlags(FLAG_PRIMARY)
            .setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(
                        carContext,
                        R.drawable.baseline_navigation_24
                    )

                )
                    .setTint(CarColor.BLUE) //Host may override the tint color
                    .build()
            )
            // Only certain Intent actions are supported by `startCarApp`. Check its documentation
            // for all of the details. To open another app that can handle navigating to a location
            // you must use the CarContext.ACTION_NAVIGATE action and not Intent.ACTION_VIEW like
            // you might on a phone.
            .setOnClickListener { carContext.startCarApp(place.toIntent(CarContext.ACTION_NAVIGATE)) }
            .build()

        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setIcon(
                        CarIcon.Builder(
                            IconCompat.createWithResource(
                                carContext,
                                R.drawable.ic_star
                            )
                        ).build()
                    )
                    .setOnClickListener {

                    }.build()
            )
            .build()
        val cityName = SpannableString(
            carContext.getString(
                R.string.city_with_country,
                weatherResponse.name,
                weatherResponse.sys.country
            )
        )
        cityName.setSpan(
            ForegroundCarColorSpan.create(PRIMARY),
            0,
            cityName.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val coordinates = SpannableString(
            carContext.getString(
                R.string.city_with_country,
                place.longitude.toString(),
                place.latitude.toString()
            )
        )
        coordinates.setSpan(
            ForegroundCarColorSpan.create(SECONDARY),
            0,
            coordinates.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return PaneTemplate.Builder(
            Pane.Builder()
                .addAction(navigateAction)
                .addRow(
                    Row.Builder()
                        .setTitle(carContext.getString(R.string.city))
                        .addText(cityName)
                        .build()
                )
                .addRow(
                    Row.Builder()
                        .setTitle(COORDINATES)
                        .addText(coordinates)
                        .build()
                ).addRow(
                    Row.Builder()
                        .setTitle(carContext.getString(R.string.description))
                        .addText(
                            carContext.getString(
                                R.string.common_city_desc,
                                weatherResponse.name
                            )
                        )
                        .build()
                ).build()
        )
            .setTitle(weatherResponse.name)
            .setHeaderAction(Action.BACK)
            .setActionStrip(actionStrip)
            .build()
    }
}