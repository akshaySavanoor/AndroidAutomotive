package com.akshay.weatherapp.misc

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Constants.Companion.CITY_NAME
import com.akshay.weatherapp.common.Constants.Companion.CLOUD
import com.akshay.weatherapp.common.Constants.Companion.COORDINATES
import com.akshay.weatherapp.common.Constants.Companion.COUNTRY_CODE
import com.akshay.weatherapp.common.Constants.Companion.FEELS_LIKE
import com.akshay.weatherapp.common.Constants.Companion.GROUND_LEVEL
import com.akshay.weatherapp.common.Constants.Companion.HUMIDITY
import com.akshay.weatherapp.common.Constants.Companion.LABEL_LATITUDE
import com.akshay.weatherapp.common.Constants.Companion.LABEL_LONGITUDE
import com.akshay.weatherapp.common.Constants.Companion.MAXIMUM_TEMPERATURE
import com.akshay.weatherapp.common.Constants.Companion.MINIMUM_TEMPERATURE
import com.akshay.weatherapp.common.Constants.Companion.PRESSURE
import com.akshay.weatherapp.common.Constants.Companion.SEA_LEVEL
import com.akshay.weatherapp.common.Constants.Companion.SUN_RISE_TIME
import com.akshay.weatherapp.common.Constants.Companion.SUN_SET_TIME
import com.akshay.weatherapp.common.Constants.Companion.SYSTEM_INFORMATION
import com.akshay.weatherapp.common.Constants.Companion.TEMPERATURE
import com.akshay.weatherapp.common.Constants.Companion.WEATHER_CONDITION
import com.akshay.weatherapp.common.Constants.Companion.WEATHER_INFORMATION
import com.akshay.weatherapp.common.Constants.Companion.WIND
import com.akshay.weatherapp.common.Constants.Companion.WIND_DIRECTION
import com.akshay.weatherapp.common.Constants.Companion.WIND_SPEED
import com.akshay.weatherapp.common.Utility.Companion.kelvinToCelsius
import com.akshay.weatherapp.common.Utility.Companion.unixTimeToTimeWithAMPM
import com.akshay.weatherapp.model.Coordinates
import com.akshay.weatherapp.model.WeatherResponseModel

class WeatherDetailsScreen(
    carContext: CarContext,
    private val weatherResponseModel: WeatherResponseModel,
    private val weatherDataType: String
) : Screen(carContext) {

    private val itemListBuilder = ItemList.Builder()
        .setNoItemsMessage(carContext.getString(R.string.no_data_found))

    override fun onGetTemplate(): Template {
        when (weatherDataType) {
            COORDINATES -> addCoordinateTemplate()
            WEATHER_CONDITION -> addWeatherConditionTemplate()
            TEMPERATURE -> addTemperatureTemplate()
            WIND -> addWindTemplate()
            CLOUD -> addCloudTemplate()
            SYSTEM_INFORMATION -> addSystemInformationTemplate()
        }

        return ListTemplate.Builder()
            .setTitle(WEATHER_INFORMATION)
            .setHeaderAction(Action.BACK)
            .setSingleList(itemListBuilder.build())
            .build()
    }

    private fun addCoordinateTemplate() {
        itemListBuilder.addItem(createCoordinateRow(weatherResponseModel.coord))
    }

    private fun addWeatherConditionTemplate() {
        val weatherList = weatherResponseModel.weather[0]
        itemListBuilder.addItem(createWeatherRow(weatherList.main, weatherList.description))
    }

    private fun addTemperatureTemplate() {
        itemListBuilder.apply {
            addItem(
                createWeatherRow(
                    TEMPERATURE,
                    formatTemperature(weatherResponseModel.main?.temp)
                )
            )
            addItem(
                createWeatherRow(
                    FEELS_LIKE,
                    formatTemperature(weatherResponseModel.main?.feelsLike)
                )
            )
            addItem(createWeatherRow(PRESSURE, "${weatherResponseModel.main?.pressure}hPA"))
            addItem(createWeatherRow(HUMIDITY, "${weatherResponseModel.main?.humidity}%"))
            addItem(
                createWeatherRow(
                    MINIMUM_TEMPERATURE,
                    formatTemperature(weatherResponseModel.main?.tempMin)
                )
            )
            addItem(
                createWeatherRow(
                    MAXIMUM_TEMPERATURE,
                    formatTemperature(weatherResponseModel.main?.tempMax)
                )
            )
            addItem(createWeatherRow(SEA_LEVEL, "${weatherResponseModel.main?.seaLevel ?: "_ "}hPa"))
            addItem(createWeatherRow(GROUND_LEVEL, "${weatherResponseModel.main?.grndLevel ?: "_ "}hPa"))
        }
    }

    private fun formatTemperature(temperature: Double?): String {
        return "${temperature?.let { kelvinToCelsius(it) } ?: "_ "} °C"
    }

    private fun addWindTemplate() {
        itemListBuilder.apply {
            addItem(createWeatherRow(WIND_SPEED, "${weatherResponseModel.wind?.speed} m/s"))
            addItem(createWeatherRow(WIND_DIRECTION, "${weatherResponseModel.wind?.deg} degrees"))
        }
    }

    private fun addCloudTemplate() {
        itemListBuilder.apply {
            addItem(createWeatherRow(CLOUD, "${weatherResponseModel.clouds?.all}%"))
        }
    }

    private fun addSystemInformationTemplate() {
        itemListBuilder.apply {
            addItem(createWeatherRow(COUNTRY_CODE, weatherResponseModel.sys.country))
            addItem(createWeatherRow(CITY_NAME, weatherResponseModel.name))
            addItem(createWeatherRow(SUN_RISE_TIME, unixTimeToTimeWithAMPM(weatherResponseModel.sys.sunrise)))
            addItem(createWeatherRow(SUN_SET_TIME, unixTimeToTimeWithAMPM(weatherResponseModel.sys.sunset)))
        }
    }

    private fun createCoordinateRow(coordinates: Coordinates): Row {
        return Row.Builder()
            .setTitle(COORDINATES)
            .addText("${LABEL_LATITUDE}: ${coordinates.lat}, ${LABEL_LONGITUDE}: ${coordinates.lon}")
            .build()
    }

    private fun createWeatherRow(title: String, value: String): Row {
        return Row.Builder()
            .setTitle(title)
            .addText(value)
            .build()
    }
}