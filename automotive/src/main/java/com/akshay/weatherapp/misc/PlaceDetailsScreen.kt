package com.akshay.weatherapp.misc

import android.text.SpannableString
import android.text.Spanned
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Action.FLAG_PRIMARY
import androidx.car.app.model.CarColor.PRIMARY
import androidx.car.app.model.CarColor.SECONDARY
import androidx.car.app.model.ForegroundCarColorSpan
import androidx.car.app.model.OnClickListener
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.versioning.CarAppApiLevels
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Constants.Companion.COORDINATES
import com.akshay.weatherapp.common.TemplateUtility.createGenericAction
import com.akshay.weatherapp.common.TemplateUtility.createGenericActionStrip
import com.akshay.weatherapp.common.TemplateUtility.getIconByResource
import com.akshay.weatherapp.common.Utility.Companion.showErrorMessage
import com.akshay.weatherapp.common.Utility.Companion.toIntent
import com.akshay.weatherapp.model.WeatherResponseModel
import com.akshay.weatherapp.viewmodel.LocationViewModel

class PlaceDetailsScreen(
    carContext: CarContext,
    private val weatherResponseModel: WeatherResponseModel
) :
    Screen(carContext) {
    private val placesViewModel = LocationViewModel()
    private var mIsFavorite: Boolean = false
    val place = placesViewModel.getLocationData()

    private fun createRowItem(title: String, additionalInfo: SpannableString): Row {
        return Row.Builder()
            .setTitle(title)
            .addText(additionalInfo)
            .build()
    }

    override fun onGetTemplate(): Template {

        val navigateAction = createGenericAction(
            title = carContext.getString(R.string.navigate),
            flag = if (carContext.carAppApiLevel >= CarAppApiLevels.LEVEL_4) FLAG_PRIMARY else null,
            icon = getIconByResource(
                icon = R.drawable.baseline_navigation_24,
                carContext = carContext
            ),
            onClickListener = OnClickListener {
                carContext.startCarApp(place.toIntent(CarContext.ACTION_NAVIGATE))
            }
        )
        // Only certain Intent actions are supported by `startCarApp`. Check its documentation
        // for all of the details. To open another app that can handle navigating to a location
        // you must use the CarContext.ACTION_NAVIGATE action and not Intent.ACTION_VIEW like
        // you might on a phone.

        val actionStrip =
            createGenericActionStrip(
                createGenericAction(
                    icon = getIconByResource(
                        if (mIsFavorite) R.drawable.ic_favorite_filled_white_24dp
                        else R.drawable.ic_favorite_white_24dp, carContext
                    ),
                    onClickListener = OnClickListener {
                        showErrorMessage(
                            carContext,
                            if (mIsFavorite) carContext.getString(R.string.removed_from_favourites)
                            else carContext.getString(R.string.added_to_favourites),
                        )
                        mIsFavorite = !mIsFavorite
                        invalidate()
                    }
                )
            )

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

        val paneItem = Pane.Builder().apply {
            addAction(navigateAction)
            addRow(createRowItem(carContext.getString(R.string.city), cityName))
            addRow(createRowItem(COORDINATES, coordinates))
            addRow(
                Row.Builder()
                    .setTitle(carContext.getString(R.string.description))
                    .addText(
                        carContext.getString(
                            R.string.common_city_desc,
                            weatherResponseModel.name
                        )
                    )
                    .build()
            )
        }

        return PaneTemplate.Builder(paneItem.build()).run {
            setTitle(weatherResponseModel.name)
            setHeaderAction(Action.BACK)
            setActionStrip(actionStrip)
            build()
        }
    }
}