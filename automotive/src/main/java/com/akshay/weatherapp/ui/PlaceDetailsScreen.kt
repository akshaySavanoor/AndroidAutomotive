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
import com.akshay.weatherapp.common.Constants.Companion.COORDINATES
import com.akshay.weatherapp.common.Utility.Companion.showToast
import com.akshay.weatherapp.common.Utility.Companion.toIntent
import com.akshay.weatherapp.model.WeatherResponseModel
import com.akshay.weatherapp.viewmodel.LocationViewModel

class PlaceDetailsScreen(carContext: CarContext, private val weatherResponseModel: WeatherResponseModel) :
    Screen(carContext) {
    private val placesViewModel = LocationViewModel()
    private var mIsFavorite: Boolean = false
    val place = placesViewModel.getLocationData()

    override fun onGetTemplate(): Template {

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
                                if (mIsFavorite) R.drawable.ic_favorite_filled_white_24dp
                                else R.drawable.ic_favorite_white_24dp
                            )
                        ).build()
                    )
                    .setOnClickListener {
                        showToast(
                            carContext,
                            if (mIsFavorite) carContext.getString(R.string.removed_from_favourites)
                            else carContext.getString(R.string.added_to_favourites),
                        )
                        mIsFavorite = !mIsFavorite
                        invalidate()
                    }
                    .build()
            )
            .build()
        val cityName = SpannableString(
            carContext.getString(
                R.string.city_with_country,
                weatherResponseModel.name,
                weatherResponseModel.sys.country
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
                                weatherResponseModel.name
                            )
                        )
                        .build()
                ).build()
        )
            .setTitle(weatherResponseModel.name)
            .setHeaderAction(Action.BACK)
            .setActionStrip(actionStrip)
            .build()
    }
}