package com.akshay.weatherapp.ui

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.akshay.weatherapp.R
import com.akshay.weatherapp.model.Coordinates
import com.akshay.weatherapp.model.WeatherResponse
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

class WeatherDetailsScreen(
    carContext: CarContext,
    private val weatherResponse: WeatherResponse,
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
        itemListBuilder.addItem(createCoordinateRow(weatherResponse.coord))
    }

    private fun addWeatherConditionTemplate() {
        val weatherList = weatherResponse.weather[0]
        itemListBuilder.addItem(createWeatherRow(weatherList.main, weatherList.description))
    }

    private fun addTemperatureTemplate() {
        itemListBuilder.apply {
            addItem(
                createWeatherRow(
                    TEMPERATURE,
                    formatTemperature(weatherResponse.main?.temp)
                )
            )
            addItem(
                createWeatherRow(
                    FEELS_LIKE,
                    formatTemperature(weatherResponse.main?.feelsLike)
                )
            )
            addItem(createWeatherRow(PRESSURE, "${weatherResponse.main?.pressure}hPA"))
            addItem(createWeatherRow(HUMIDITY, "${weatherResponse.main?.humidity}%"))
            addItem(
                createWeatherRow(
                    MINIMUM_TEMPERATURE,
                    formatTemperature(weatherResponse.main?.tempMin)
                )
            )
            addItem(
                createWeatherRow(
                    MAXIMUM_TEMPERATURE,
                    formatTemperature(weatherResponse.main?.tempMax)
                )
            )
            addItem(createWeatherRow(SEA_LEVEL, "${weatherResponse.main?.seaLevel ?: "_ "}hPa"))
            addItem(createWeatherRow(GROUND_LEVEL, "${weatherResponse.main?.grndLevel ?: "_ "}hPa"))
        }
    }

    private fun formatTemperature(temperature: Double?): String {
        return "${temperature?.let { kelvinToCelsius(it) } ?: "_ "} °C"
    }

    private fun addWindTemplate() {
        itemListBuilder.apply {
            addItem(createWeatherRow(WIND_SPEED, "${weatherResponse.wind?.speed} m/s"))
            addItem(createWeatherRow(WIND_DIRECTION, "${weatherResponse.wind?.deg} degrees"))
        }
    }

    private fun addCloudTemplate() {
        itemListBuilder.apply {
            addItem(createWeatherRow(CLOUD, "${weatherResponse.clouds?.all}%"))
        }
    }

    private fun addSystemInformationTemplate() {
        itemListBuilder.apply {
            addItem(createWeatherRow(COUNTRY_CODE, weatherResponse.sys.country))
            addItem(createWeatherRow(CITY_NAME, weatherResponse.name))
            addItem(createWeatherRow(SUN_RISE_TIME, "${weatherResponse.sys.sunrise} UTC"))
            addItem(createWeatherRow(SUN_SET_TIME, "${weatherResponse.sys.sunset} UTC"))
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